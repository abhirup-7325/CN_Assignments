package edu.abhirup.flowcontrol.protocols.gobackn;

import edu.abhirup.flowcontrol.common.Packet;
import java.io.*;
import java.net.Socket;

public class ReceiverGBN {
    private final Socket socket;
    private final String fileName;
    private final long senderAddress;
    private final long receiverAddress;

    private static final int SEQ_NUM_SPACE = 8;
    private int expectedSeqNum = 0;

    public ReceiverGBN(Socket socket, String name, long senderAddress, long receiverAddress, String fileName) {
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
                    if (packet.getSeqNo() == expectedSeqNum) {
                        fileWriter.write(packet.getSegmentData());
                        System.out.println("Packet " + expectedSeqNum + " accepted.");
                        
                        // Send cumulative ACK
                        expectedSeqNum = (expectedSeqNum + 1) % SEQ_NUM_SPACE;
                        sendAck(out, expectedSeqNum);
                        System.out.println("ACK " + expectedSeqNum + " Sent By Receiver\n");
                    } else {
                        // Discard out-of-order packet and resend ACK for last correct packet
                        System.out.println("Out-of-order packet discarded. Expected " + expectedSeqNum);
                        sendAck(out, expectedSeqNum);
                        System.out.println("ACK " + expectedSeqNum + " Resent.");
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