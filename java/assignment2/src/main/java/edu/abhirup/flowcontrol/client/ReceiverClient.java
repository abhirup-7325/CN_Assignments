package edu.abhirup.flowcontrol.client;


import edu.abhirup.flowcontrol.protocols.gobackn.ReceiverGBN;
import edu.abhirup.flowcontrol.protocols.selectiverepeat.ReceiverSR;
import edu.abhirup.flowcontrol.protocols.stopandwait.ReceiverSW;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ReceiverClient {
    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 9999;

        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            System.out.println(in.readLine()); // "You are now connected..."
            System.out.print(in.readLine()); // "Client Name: "
            String name = scanner.nextLine();
            out.println(name);
            long receiverAddress = Long.parseLong(in.readLine());

            System.out.println("\nSelect Protocol-----");
            System.out.println("1. Stop-and-Wait\n2. Go-Back-N\n3. Selective Repeat");
            System.out.print("Enter choice: ");
            int protocol = scanner.nextInt();
            scanner.nextLine();

            System.out.println("\nWaiting to receive data...");
            
            while (true) {
                String senderPortStr = in.readLine();
                if (senderPortStr == null) break;
                
                long senderAddress = Long.parseLong(senderPortStr);
                System.out.println("Receiving data from sender at port " + senderAddress);

                String fileName = "";
                switch (protocol) {
                    case 1:
                        fileName = "StopWait_rec.txt";
                        new ReceiverSW(socket, name, senderAddress, receiverAddress, fileName).startReceiving();
                        break;
                    case 2:
                        fileName = "GoBackN_rec.txt";
                         new ReceiverGBN(socket, name, senderAddress, receiverAddress, fileName).startReceiving();
                        break;
                    case 3:
                        fileName = "SelectiveRepeat_rec.txt";
                        new ReceiverSR(socket, name, senderAddress, receiverAddress, fileName).startReceiving();
                        break;
                }
                System.out.println("File saved to " + fileName + ". Ready for next transmission.");
            }
        } catch (IOException e) {
            System.out.println("Disconnected from server.");
        }
    }
}