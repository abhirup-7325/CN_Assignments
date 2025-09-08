package edu.abhirup.flowcontrol.client;


import edu.abhirup.flowcontrol.protocols.gobackn.SenderGBN;
import edu.abhirup.flowcontrol.protocols.selectiverepeat.SenderSR;
import edu.abhirup.flowcontrol.protocols.stopandwait.SenderSW;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class SenderClient {
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
            long senderAddress = Long.parseLong(in.readLine());

            System.out.println("\nSelect Protocol-----");
            System.out.println("1. Stop-and-Wait\n2. Go-Back-N\n3. Selective Repeat");
            System.out.print("Enter choice: ");
            int protocol = scanner.nextInt();
            scanner.nextLine(); // consume newline

            while (true) {
                System.out.println("\nInput options-----\n1. Send data\n2. Close");
                System.out.print("Enter option: ");
                int choice = scanner.nextInt();
                scanner.nextLine();

                if (choice == 1) {
                    out.println("request for sending");
                    String serverResponse = in.readLine();
                    if ("No client is available".equals(serverResponse)) {
                        System.out.println(serverResponse);
                        continue;
                    }

                    String[] receivers = serverResponse.split(",");
                    System.out.println("Available clients-----");
                    for (int i = 0; i < receivers.length; i++) {
                        System.out.println((i + 1) + ". " + receivers[i]);
                    }
                    System.out.print("\nYour choice: ");
                    int receiverChoice = scanner.nextInt() - 1;
                    scanner.nextLine();

                    out.println(receiverChoice);
                    long receiverAddress = Long.parseLong(in.readLine());
                    String receiverName = receivers[receiverChoice];
                    String fileName = "data.txt"; // Ensure this file exists

                    System.out.println("Starting transmission to " + receiverName);
                    
                    switch (protocol) {
                        case 1:
                            new SenderSW(socket, name, senderAddress, receiverName, receiverAddress, fileName).transmit();
                            break;
                        case 2:
                            new SenderGBN(socket, name, senderAddress, receiverName, receiverAddress, fileName).transmit();
                            break;
                        case 3:
                            new SenderSR(socket, name, senderAddress, receiverName, receiverAddress, fileName).transmit();
                            break;
                        default:
                            System.out.println("Invalid protocol. Using Stop-and-Wait.");
                            new SenderSW(socket, name, senderAddress, receiverName, receiverAddress, fileName).transmit();
                            break;
                    }
                     // Wait for end confirmation from receiver via channel
                     System.out.println(in.readLine());
                } else if (choice == 2) {
                    out.println("close");
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}