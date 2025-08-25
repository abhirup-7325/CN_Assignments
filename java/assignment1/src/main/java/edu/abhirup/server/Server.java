package edu.abhirup.server;

import java.net.ServerSocket;
import java.net.Socket;

import edu.abhirup.server.utils.ChecksumValidator;
import edu.abhirup.server.utils.CrcValidator;

import java.io.InputStreamReader;
import java.io.BufferedReader;

public class Server {
    public static void main(String[] args) {
        while (true) {
            try {
                ServerSocket serverSocket = new ServerSocket(9999);
                Socket socket = serverSocket.accept();

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    // ChecksumValidator validator = new ChecksumValidator();
                    // String validatedMessage = validator.validateMessage(line);
                    System.out.println("Size: " + line.length());

                    String crc8 = "111010101";
                    String crc10 = "11000110011";
                    String crc16 = "11000000000000101";
                    String crc32 = "100000100110000010001110110110111";
                    CrcValidator crcValidator = new CrcValidator(crc16);
                    String validatedMessage = crcValidator.validateMessage(line);

                    System.out.println(validatedMessage);
                }
                
                in.close();
                socket.close();
                serverSocket.close();
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
