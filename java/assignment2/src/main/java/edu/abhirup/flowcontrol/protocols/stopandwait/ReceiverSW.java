package edu.abhirup.flowcontrol.protocols.stopandwait;


import edu.abhirup.flowcontrol.common.Packet;
import java.io.*;
import java.net.Socket;

public class ReceiverSW {
    private final Socket socket;
    private final String name;
    private final String fileName;
    private final long senderAddress;
    private final long receiverAddress;
    
    private int expectedSeqNo = 0;

    public ReceiverSW(Socket socket, String name, long senderAddress, long receiverAddress, String fileName) {
        this.socket = socket;
        this.name = name;
        this.senderAddress = senderAddress;
        this.receiverAddress = receiverAddress;
        this.fileName = fileName;
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
                System.out.println("\nPacket Received");

                if (!packet.hasError()) {
                    System.out.println("NO ERROR FOUND");
                    if (packet.getSeqNo() == expectedSeqNo) {
                        fileWriter.write(packet.getSegmentData());
                        sendAck(out, expectedSeqNo);
                        System.out.println("ACK Sent By Receiver for packet " + expectedSeqNo + "\n");
                        expectedSeqNo = (expectedSeqNo + 1) % 2;
                    } else {
                         // Resend ACK for the last correctly received packet
                        sendAck(out, (expectedSeqNo + 1) % 2); 
                        System.out.println("Duplicate packet. ACK Resent.");
                    }
                } else {
                    System.out.println("Packet Dropped due to error.");
                }
            }
        }
    }

    private void sendAck(PrintWriter out, int ackNo) {
        Packet ackPacket = new Packet(receiverAddress, senderAddress, 1, (ackNo + 1)%2, "acknowledgement");
        out.println(ackPacket.toBinaryString(15));
    }
}