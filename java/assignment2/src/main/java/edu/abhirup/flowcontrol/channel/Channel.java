package edu.abhirup.flowcontrol.channel;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Channel {

    public static ConcurrentHashMap<String, ConnectionHandler> clientMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        int port = 9999;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);
            System.out.println("Waiting for client requests...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ConnectionHandler handler = new ConnectionHandler(clientSocket);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}