package edu.abhirup.flowcontrol.protocols.selectiverepeat;


import edu.abhirup.flowcontrol.analysis.Analysis;
import edu.abhirup.flowcontrol.common.Packet;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class SenderSR {
    private final Socket socket;
    private final String name;
    private final String receiverName;
    private final String fileName;
    private final long senderAddress;
    private final long receiverAddress;

    private static final int WINDOW_SIZE = 8;
    private static final int SEQ_NUM_SPACE = 16;
    private static final int PACKET_SIZE = 46;
    private static final long TIMEOUT = 2000; // in milliseconds

    private final Packet[] window = new Packet[SEQ_NUM_SPACE];
    private final boolean[] ackedPackets = new boolean[SEQ_NUM_SPACE];
    private final ScheduledExecutorService timerExecutor = Executors.newScheduledThreadPool(WINDOW_SIZE);
    private final ScheduledFuture<?>[] timers = new ScheduledFuture<?>[SEQ_NUM_SPACE];

    private volatile int base = 0;
    private volatile int nextSeqNum = 0;
    private volatile boolean endOfFile = false;

    private final ReentrantLock lock = new ReentrantLock();

    public SenderSR(Socket socket, String name, long senderAddress, String receiverName, long receiverAddress, String fileName) {
        this.socket = socket;
        this.name = name;
        this.senderAddress = senderAddress;
        this.receiverName = receiverName;
        this.receiverAddress = receiverAddress;
        this.fileName = fileName;
    }
    
    private void startTimer(int seqNum) {
        stopTimer(seqNum);
        timers[seqNum] = timerExecutor.schedule(() -> handleTimeout(seqNum), TIMEOUT, TimeUnit.MILLISECONDS);
    }

    private void stopTimer(int seqNum) {
        if (timers[seqNum] != null && !timers[seqNum].isDone()) {
            timers[seqNum].cancel(false);
        }
    }

    private void handleTimeout(int seqNum) {
        lock.lock();
        try {
            System.out.println("Timeout for packet " + seqNum + ". Resending.");
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            if (window[seqNum] != null) {
                out.println(window[seqNum].toBinaryString(PACKET_SIZE));
                startTimer(seqNum); // Restart timer for the resent packet
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void transmit() throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        long startTime = System.currentTimeMillis();
        List<Double> rttStore = new ArrayList<>();
        int pktCount = 0;
        int totalPktCount = 0;

        // ACK/NAK Listener Thread
        Thread ackListener = new Thread(() -> {
            try {
                String serverResponse;
                while ((serverResponse = in.readLine()) != null) {
                     if ("end".equals(serverResponse)) break;
                    Packet packet = Packet.build(serverResponse);
                    if (!packet.hasError()) {
                        int seqNum = packet.getSeqNo();
                        lock.lock();
                        try {
                            // Check if it's within the window
                             if ((base <= seqNum && seqNum < nextSeqNum) || (nextSeqNum < base && (base <= seqNum || seqNum < nextSeqNum))) {
                                if (packet.getType() == 1) { // ACK
                                    System.out.println("Received ACK for packet " + seqNum);
                                    stopTimer(seqNum);
                                    ackedPackets[seqNum] = true;
                                    
                                    // Slide window
                                    while(ackedPackets[base]) {
                                        ackedPackets[base] = false;
                                        window[base] = null;
                                        base = (base + 1) % SEQ_NUM_SPACE;
                                        System.out.println("Window slided. New base is " + base);
                                    }
                                } else if (packet.getType() == 2) { // NAK
                                    System.out.println("Received NAK for packet " + seqNum + ". Resending immediately.");
                                    handleTimeout(seqNum);
                                }
                             }
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            } catch (IOException e) {
                 System.err.println("Listener error: " + e.getMessage());
            }
        });
        ackListener.start();
        
        // Data Sending Logic
        try (BufferedReader fileReader = new BufferedReader(new FileReader(fileName))) {
            char[] buffer = new char[PACKET_SIZE];
            int charsRead;
            while (true) {
                lock.lock();
                try {
                     while ((nextSeqNum - base + SEQ_NUM_SPACE) % SEQ_NUM_SPACE < WINDOW_SIZE && !endOfFile) {
                        if ((charsRead = fileReader.read(buffer)) != -1) {
                            String dataFrame = new String(buffer, 0, charsRead);
                            Packet packet = new Packet(senderAddress, receiverAddress, 0, nextSeqNum, dataFrame);
                            window[nextSeqNum] = packet;
                            ackedPackets[nextSeqNum] = false;
                            
                            out.println(packet.toBinaryString(PACKET_SIZE));
                            System.out.println("\nPacket " + nextSeqNum + " Sent to Channel");
                            startTimer(nextSeqNum);
                            pktCount++;
                            totalPktCount++;
                            
                            nextSeqNum = (nextSeqNum + 1) % SEQ_NUM_SPACE;
                        } else {
                            endOfFile = true;
                            break;
                        }
                    }
                } finally {
                    lock.unlock();
                }

                if (endOfFile && base == nextSeqNum) {
                    break;
                }
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // Finalization
            while(base != nextSeqNum) {
                try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
            timerExecutor.shutdownNow();
            out.println("end");
            try {
                ackListener.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            long totalTime = System.currentTimeMillis() - startTime;
            Analysis.generateReport(name, receiverName, "SelectiveRepeat_report.txt", pktCount, totalPktCount, totalTime / 1000.0, rttStore);
        }
    }
}