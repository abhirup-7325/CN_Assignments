package edu.abhirup.flowcontrol.protocols.stopandwait;


import edu.abhirup.flowcontrol.common.Packet;
import edu.abhirup.flowcontrol.analysis.Analysis;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class SenderSW {

    private final Socket socket;
    private final String name;
    private final String receiverName;
    private final String fileName;
    private final long senderAddress;
    private final long receiverAddress;
    
    private volatile boolean ackReceived = false;
    private volatile int seqNo = 0;
    private Packet recentPacket;
    private final Object lock = new Object();
    
    private final int PACKET_SIZE = 46;
    private final int TIMEOUT = 2; // seconds

    public SenderSW(Socket socket, String name, long senderAddress, String receiverName, long receiverAddress, String fileName) {
        this.socket = socket;
        this.name = name;
        this.senderAddress = senderAddress;
        this.receiverName = receiverName;
        this.receiverAddress = receiverAddress;
        this.fileName = fileName;
    }

    public void transmit() throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        long startTime = System.currentTimeMillis();
        List<Double> rttStore = new ArrayList<>();
        int pktCount = 0;
        int totalPktCount = 0;

        // Start ACK listener thread
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> ackListenerFuture = executor.submit(() -> {
            try {
                String serverResponse;
                while ((serverResponse = in.readLine()) != null) {
                     if ("end".equals(serverResponse)) {
                        break;
                    }
                    Packet ackPacket = Packet.build(serverResponse);
                    if (ackPacket.getType() == 1 && !ackPacket.hasError()) {
                        if (ackPacket.getSeqNo() == (seqNo + 1) % 2) {
                             synchronized(lock) {
                                ackReceived = true;
                                lock.notifyAll();
                             }
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("ACK listener stopped.");
            }
        });


        try (BufferedReader fileReader = new BufferedReader(new FileReader(fileName))) {
            char[] buffer = new char[PACKET_SIZE];
            int charsRead;
            while ((charsRead = fileReader.read(buffer)) != -1) {
                String dataFrame = new String(buffer, 0, charsRead);
                seqNo = pktCount % 2;
                recentPacket = new Packet(senderAddress, receiverAddress, 0, seqNo, dataFrame);

                ackReceived = false;
                while (!ackReceived) {
                    out.println(recentPacket.toBinaryString(PACKET_SIZE));
                    System.out.println("\nPacket " + (pktCount + 1) + " Sent To Channel");
                    totalPktCount++;
                    long sentTime = System.currentTimeMillis();

                    synchronized(lock) {
                        try {
                            lock.wait(TIMEOUT * 1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    
                    if (ackReceived) {
                        long receiveTime = System.currentTimeMillis();
                        rttStore.add((double)(receiveTime - sentTime));
                        System.out.println("PACKET " + (pktCount + 1) + " HAS REACHED SUCCESSFULLY\n");
                        pktCount++;
                    } else {
                        System.out.println("Timeout for packet " + (pktCount + 1) + ". Resending.");
                    }
                }
            }
        } finally {
            out.println("end");
            ackListenerFuture.cancel(true);
            executor.shutdown();
            
            long totalTime = System.currentTimeMillis() - startTime;
            Analysis.generateReport(name, receiverName, "StopWait_report.txt", pktCount, totalPktCount, totalTime/1000.0, rttStore);
        }
    }
}