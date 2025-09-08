package edu.abhirup.flowcontrol.utils;


import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;

public class TestGenerator {
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static void main(String[] args) {
        int n = 7 * (32 * 13); // Number of characters to generate
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }

        try (FileWriter file = new FileWriter("data.txt")) {
            file.write(sb.toString());
            System.out.println("Successfully created data.txt with " + n + " characters.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}