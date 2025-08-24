package edu.abhirup.client.utils;

import java.util.ArrayList;

public class CrcCalculator {
    private String generator;

    public ArrayList<ArrayList<Integer>> calculateCrc(ArrayList<ArrayList<Integer>> blocks, String generator) {
        this.generator = generator;

        ArrayList<ArrayList<Integer>> resultBlocks = new ArrayList<>();
        for (ArrayList<Integer> block : blocks) {
            resultBlocks.add(computeCrcForBlock(block));
        }

        System.out.println("CRC: " + resultBlocks);

        return resultBlocks;
    }

    private ArrayList<Integer> computeCrcForBlock(ArrayList<Integer> block) {
        ArrayList<Integer> dataBits = new ArrayList<>();
        for (int value : block) {
            for (int i = 7; i >= 0; i--) {
                dataBits.add((value >> i) & 1);
            }
        }

        int genDegree = generator.length() - 1;
        ArrayList<Integer> temp = new ArrayList<>(dataBits);
        for (int i = 0; i < genDegree; i++) temp.add(0);

        ArrayList<Integer> genBits = new ArrayList<>();
        for (char c : generator.toCharArray()) genBits.add(c - '0');

        for (int i = 0; i <= temp.size() - genBits.size(); i++) {
            if (temp.get(i) == 1) {
                for (int j = 0; j < genBits.size(); j++) {
                    temp.set(i + j, temp.get(i + j) ^ genBits.get(j));
                }
            }
        }

        ArrayList<Integer> crcBits = new ArrayList<>();
        for (int i = temp.size() - genDegree; i < temp.size(); i++) {
            crcBits.add(temp.get(i));
        }

        while (crcBits.size() < 32) crcBits.add(0, 0);

        ArrayList<Integer> crcChars = new ArrayList<>();
        for (int i = 0; i < 32; i += 16) {
            int val = 0;
            for (int j = 0; j < 16; j++) {
                val = (val << 1) | crcBits.get(i + j);
            }
            crcChars.add(val);
        }

        ArrayList<Integer> finalBlock = new ArrayList<>(block);
        finalBlock.addAll(crcChars);
        System.out.println("Final block with CRC: " + finalBlock);

        return finalBlock;
    }
}
