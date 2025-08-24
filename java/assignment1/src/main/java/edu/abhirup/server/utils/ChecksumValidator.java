package edu.abhirup.server.utils;

import java.util.ArrayList;

public class ChecksumValidator {

    public String validateMessage(String message) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < message.length(); i += 5) {
            ArrayList<Integer> block = new ArrayList<>();

            for (int j = 0; j < 5; j++) {
                block.add((int) message.charAt(i + j));
            }

            if (validateBlock(block)) {
                for (int k = 0; k < 4; k++) {
                    result.append((char) block.get(k).intValue());
                }
            } else {
                result.append("****");
            }
        }

        return result.toString();
    }

    public boolean validateBlock(ArrayList<Integer> block) {
        int summ = 0;

        for (int i = 0; i < 4; i++) {
            summ += block.get(i);
            while ((summ >> 16) != 0) {
                summ = (summ & 0xFFFF) + (summ >> 16);
            }
        }

        int checksum = block.get(4);
        int computedChecksum = ~summ & 0xFFFF;

        return computedChecksum == checksum;
    }
}
