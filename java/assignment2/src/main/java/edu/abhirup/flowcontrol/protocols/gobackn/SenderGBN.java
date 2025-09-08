package edu.abhirup.flowcontrol.protocols.gobackn;


import edu.abhirup.flowcontrol.analysis.Analysis;
import edu.abhirup.flowcontrol.common.Packet;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class SenderGBN {
    private final Socket socket;
    private final String name;
    private final String receiverName;
    private final String fileName;
    private final long senderAddress;
    private final long receiverAddress;

    private static final int WINDOW_SIZE = 7;
    private static final int SEQ_NUM_SPACE = 8; // Corresponds to MAX_WINDOW_SIZE + 1
    private static final int PACKET_SIZE = 46;
    private static final long TIMEOUT = 2000; // in milliseconds

    private final Packet[] window = new Packet[SEQ_NUM_SPACE];
    private final long[] packetTimers = new long[SEQ_NUM_SPACE];
    private volatile int base = 0;
    private volatile int nextSeqNum = 0;
    private volatile boolean endOfFile = false;

    private final ReentrantLock lock = new ReentrantLock();
    private final ScheduledExecutorService timerExecutor = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> timer;

    public SenderGBN(Socket socket, String name, long senderAddress, String receiverName, long receiverAddress, String fileName) {
        this.socket = socket;
        this.name = name;
        this.senderAddress = senderAddress;
        this.receiverName = receiverName;
        this.receiverAddress = receiverAddress;
        this.fileName = fileName;
    }

    private void startTimer() {
        stopTimer();
        timer = timerExecutor.schedule(this::handleTimeout, TIMEOUT, TimeUnit.MILLISECONDS);
    }

    private void stopTimer() {
        if (timer != null && !timer.isDone()) {
            timer.cancel(false);
        }
    }

    private void handleTimeout() {
        lock.lock();
        try {
            System.out.println("Timeout occurred. Resending window.");
            for (int i = base; i != nextSeqNum; i = (i + 1) % SEQ_NUM_SPACE) {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(window[i].toBinaryString(PACKET_SIZE));
                System.out.println("Packet " + window[i].getSeqNo() + " RESENT");
            }
            startTimer();
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

        // ACK Listener Thread
        Thread ackListener = new Thread(() -> {
            try {
                String serverResponse;
                while ((serverResponse = in.readLine()) != null) {
                    if ("end".equals(serverResponse)) break;
                    Packet ackPacket = Packet.build(serverResponse);
                    if (ackPacket.getType() == 1 && !ackPacket.hasError()) {
                        lock.lock();
                        try {
                            // Check if ACK is within the current window
                            int ackNo = ackPacket.getSeqNo();
                            if ((base < nextSeqNum && base < ackNo && ackNo <= nextSeqNum) ||
                                (nextSeqNum < base && (base < ackNo || ackNo <= nextSeqNum))) {

                                System.out.println("Received ACK for: " + (ackNo - 1 + SEQ_NUM_SPACE) % SEQ_NUM_SPACE);
                                while (base != ackNo) {
                                    long now = System.currentTimeMillis();
                                    rttStore.add((double) (now - packetTimers[base]));
                                    System.out.println("Packet " + base + " has reached successfully.");
                                    base = (base + 1) % SEQ_NUM_SPACE;
                                }

                                if (base == nextSeqNum) {
                                    stopTimer();
                                } else {
                                    startTimer();
                                }
                            }
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("ACK listener error: " + e.getMessage());
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
                            packetTimers[nextSeqNum] = System.currentTimeMillis();

                            out.println(packet.toBinaryString(PACKET_SIZE));
                            System.out.println("\nPacket " + nextSeqNum + " Sent to Channel");
                            pktCount++;
                            totalPktCount++;

                            if (base == nextSeqNum) {
                                startTimer();
                            }
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
                    break; // All packets sent and acknowledged
                }
                 Thread.sleep(10); // Small sleep to prevent busy-waiting
            }
        } catch (InterruptedException e) {
             Thread.currentThread().interrupt();
        } finally {
            // Wait for the last ACKs
            while(base != nextSeqNum) {
                try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
            stopTimer();
            timerExecutor.shutdownNow();
            out.println("end");
            
            try {
                ackListener.join(1000);
            } catch (InterruptedException e) {
                 Thread.currentThread().interrupt();
            }

            long totalTime = System.currentTimeMillis() - startTime;
            Analysis.generateReport(name, receiverName, "GoBackN_report.txt", pktCount, totalPktCount, totalTime / 1000.0, rttStore);
        }
    }
}