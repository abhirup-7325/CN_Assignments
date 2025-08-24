package edu.abhirup.client.utils;

import java.util.ArrayList;

public class ChecksumCalculator {
    public ArrayList<ArrayList<Integer>> calculateChecksum(ArrayList<ArrayList<Integer>> blocks) {
        ArrayList<ArrayList<Integer>> checkSumBlocks = new ArrayList<>(blocks);
        for (ArrayList<Integer> block : checkSumBlocks) {
            int checksumValue = calculateChecksumForBlock(block);
            block.add(checksumValue);
        }
        System.out.println("Blocks with Checksums: " + checkSumBlocks);

        return checkSumBlocks;
    }

    public int calculateChecksumForBlock(ArrayList<Integer> block) {
        int summ = 0;

        for (Integer value : block) {
            summ += value;

            while ((summ >> 16) != 0) {
                summ = (summ & 0xFFFF) + (summ >> 16);
            }
        }

        return ~summ & 0xFFFF;
    }
}
