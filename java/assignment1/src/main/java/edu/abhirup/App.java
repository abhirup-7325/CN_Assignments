package edu.abhirup;

import edu.abhirup.client.Client;
import edu.abhirup.server.Server;

public class App {
    public static void main(String[] args) {
        try {
            Runnable serverTask = new ServerTask();
            Thread serverThread = new Thread(serverTask);

            serverThread.start();

            Thread.sleep(2000);

            Client.main(new String[]{});

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class ServerTask implements Runnable {
    @Override
    public void run() {
        try {
            Server.main(new String[]{});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
