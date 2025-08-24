package edu.abhirup.server.utils;

import java.util.ArrayList;

public class ChecksumValidator {

    // Validate the entire message, replacing each block of 5 chars with 4 valid chars or "****"
    public String validateMessage(String message) {
        StringBuilder result = new StringBuilder();

        // Process each 5-character block
        for (int i = 0; i < message.length(); i += 5) {
            ArrayList<Integer> block = new ArrayList<>();

            // First 4 characters are data, 5th is checksum
            for (int j = 0; j < 5; j++) {
                block.add((int) message.charAt(i + j));
            }

            // Validate and append either correct data or ****
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

    // Validate a single block (first 4 chars + checksum)
    public boolean validateBlock(ArrayList<Integer> block) {
        int sum = 0;

        // Sum first 4 chars (16-bit wraparound)
        for (int i = 0; i < 4; i++) {
            sum += block.get(i);
            while ((sum >> 16) != 0) { // Wrap-around carry
                sum = (sum & 0xFFFF) + (sum >> 16);
            }
        }

        // One's complement of sum should match the checksum (5th value)
        int checksum = block.get(4);
        int computedChecksum = ~sum & 0xFFFF;

        return computedChecksum == checksum;
    }
}
