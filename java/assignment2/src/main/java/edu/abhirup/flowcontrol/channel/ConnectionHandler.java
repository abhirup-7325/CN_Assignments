package edu.abhirup.flowcontrol.channel;


import edu.abhirup.flowcontrol.common.ErrorInsertion;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ConnectionHandler implements Runnable {
    private final Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientName;
    private String clientAddress;
    public ConnectionHandler peerHandler;

    public ConnectionHandler(Socket socket) {
        this.clientSocket = socket;
        this.clientAddress = socket.getRemoteSocketAddress().toString();
    }
    
    public String getClientName() {
        return clientName;
    }

    private String processPacket(String packet) {
        Random rand = new Random();
        int flag = rand.nextInt(100);

        if (flag < 65) { // 65% chance of success
            return packet;
        } else if (flag < 80) { // 15% chance of error
            return ErrorInsertion.injectError(packet);
        } else if (flag < 90) { // 10% chance of delay
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return packet;
        } else { // 10% chance of packet loss
            return "";
        }
    }
    
    public void sendMessage(String msg) {
        out.println(msg);
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            out.println("You are now connected to server.\nClient Name: ");
            this.clientName = in.readLine();
            out.println(clientSocket.getLocalPort());
            
            Channel.clientMap.put(this.clientName, this);
            System.out.println("New connection from " + clientName + " at " + clientAddress);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if ("request for sending".equals(inputLine)) {
                    handleSendingRequest();
                } else if ("close".equals(inputLine)) {
                    break;
                } else if ("end".equals(inputLine)){
                     if (peerHandler != null) {
                        peerHandler.sendMessage(inputLine);
                        peerHandler = null;
                     }
                } else if (peerHandler != null) {
                    String processedPacket = processPacket(inputLine);
                    if (!processedPacket.isEmpty()) {
                        peerHandler.sendMessage(processedPacket);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(clientName + " disconnected.");
        } finally {
            try {
                clientSocket.close();
                Channel.clientMap.remove(this.clientName);
                 if (peerHandler != null) {
                    peerHandler.peerHandler = null; // Unlink peer
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleSendingRequest() throws IOException {
        List<String> availableClients = new ArrayList<>();
        for (ConnectionHandler handler : Channel.clientMap.values()) {
            if (handler != this && handler.peerHandler == null) {
                availableClients.add(handler.getClientName());
            }
        }

        if (availableClients.isEmpty()) {
            out.println("No client is available");
        } else {
            out.println(String.join(",", availableClients));
            int choice = Integer.parseInt(in.readLine());
            String peerName = availableClients.get(choice);
            
            ConnectionHandler chosenPeer = Channel.clientMap.get(peerName);

            synchronized(this) {
                if (chosenPeer != null && chosenPeer.peerHandler == null) {
                    this.peerHandler = chosenPeer;
                    chosenPeer.peerHandler = this;
                    out.println(chosenPeer.clientSocket.getPort());
                    chosenPeer.sendMessage(String.valueOf(this.clientSocket.getPort()));
                    System.out.println(this.clientName + " is now connected to " + chosenPeer.getClientName());
                } else {
                    out.println("Receiver is busy");
                }
            }
        }
    }
}