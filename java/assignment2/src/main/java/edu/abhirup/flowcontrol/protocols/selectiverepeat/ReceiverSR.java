package edu.abhirup.flowcontrol.protocols.selectiverepeat;


import edu.abhirup.flowcontrol.common.Packet;
import java.io.*;
import java.net.Socket;

public class ReceiverSR {
    private final Socket socket;
    private final String fileName;
    private final long senderAddress;
    private final long receiverAddress;

    private static final int WINDOW_SIZE = 8;
    private static final int SEQ_NUM_SPACE = 16;

    private final Packet[] receiveBuffer = new Packet[SEQ_NUM_SPACE];
    private final boolean[] receivedPackets = new boolean[SEQ_NUM_SPACE];
    private int receiveBase = 0;

    public ReceiverSR(Socket socket, String name, long senderAddress, long receiverAddress, String fileName) {
        this.socket = socket;
        this.fileName = fileName;
        this.senderAddress = senderAddress;
        this.receiverAddress = receiverAddress;
    }

    public void startReceiving() throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(fileName, true))) {
            String receivedData;
            while ((receivedData = in.readLine()) != null) {
                if ("end".equals(receivedData)) {
                    System.out.println("Transmission finished.");
                    break;
                }

                Packet packet = Packet.build(receivedData);
                System.out.println("\nPacket " + packet.getSeqNo() + " Received");

                if (!packet.hasError()) {
                    System.out.println("NO ERROR FOUND");
                    int seqNum = packet.getSeqNo();

                    // Check if packet is within the receive window
                    int upperBound = (receiveBase + WINDOW_SIZE) % SEQ_NUM_SPACE;
                    if ((receiveBase <= seqNum && seqNum < upperBound) || (upperBound < receiveBase && (receiveBase <= seqNum || seqNum < upperBound))) {
                        sendAck(out, seqNum);
                        System.out.println("ACK for packet " + seqNum + " sent.");

                        if (!receivedPackets[seqNum]) {
                            receiveBuffer[seqNum] = packet;
                            receivedPackets[seqNum] = true;
                        }

                        // Deliver buffered in-order packets
                        while(receivedPackets[receiveBase]) {
                            System.out.println("Delivering packet " + receiveBase + " to application.");
                            fileWriter.write(receiveBuffer[receiveBase].getSegmentData());
                            receivedPackets[receiveBase] = false;
                            receiveBuffer[receiveBase] = null;
                            receiveBase = (receiveBase + 1) % SEQ_NUM_SPACE;
                        }
                    } else {
                        System.out.println("Packet " + seqNum + " is outside window. Discarded.");
                        // Send ACK for it anyway in case sender's window has advanced
                        sendAck(out, seqNum);
                    }
                } else {
                    System.out.println("Packet Dropped due to error.");
                }
            }
        }
    }
    
    private void sendAck(PrintWriter out, int ackNo) {
        Packet ackPacket = new Packet(receiverAddress, senderAddress, 1, ackNo, "acknowledgement");
        out.println(ackPacket.toBinaryString(15));
    }
}