package edu.abhirup.server.utils;

import java.util.ArrayList;

public class CrcValidator {
    private String generator;

    public String validateMessage(String message) {
        if (message.length() % 6 != 0) {
            throw new IllegalArgumentException("Invalid message length");
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < message.length(); i += 6) {
            String block = message.substring(i, i + 6);
            if (validateBlock(block)) {
                result.append(block.substring(0, 4));
            } else {
                result.append("****");
            }
        }

        return result.toString();
    }

    public boolean validateBlock(String block) {
        if (block.length() != 6) {
            return false;
        }

        ArrayList<Integer> dataBits = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int value = block.charAt(i);
            for (int j = 7; j >= 0; j--) {
                dataBits.add((value >> j) & 1);
            }
        }

        int high = block.charAt(4);
        int low = block.charAt(5);
        int combined = (high << 16) | low;

        int genDegree = generator.length() - 1;
        ArrayList<Integer> crcBits = new ArrayList<>();
        for (int i = genDegree - 1; i >= 0; i--) {
            crcBits.add((combined >> i) & 1);
        }

        ArrayList<Integer> temp = new ArrayList<>(dataBits);
        temp.addAll(crcBits);

        ArrayList<Integer> genBits = new ArrayList<>();
        for (char c : generator.toCharArray()) {
            genBits.add(c - '0');
        }

        for (int i = 0; i <= temp.size() - genBits.size(); i++) {
            if (temp.get(i) == 1) {
                for (int j = 0; j < genBits.size(); j++) {
                    temp.set(i + j, temp.get(i + j) ^ genBits.get(j));
                }
            }
        }

        for (int i = temp.size() - genDegree; i < temp.size(); i++) {
            if (temp.get(i) != 0) {
                return false;
            }
        }

        return true;
    }

    public CrcValidator(String generator) {
        this.generator = generator;
    }
}
