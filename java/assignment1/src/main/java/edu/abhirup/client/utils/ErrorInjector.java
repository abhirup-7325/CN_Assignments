package edu.abhirup.client.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class ErrorInjector {

    private final Random random = new Random();

    
    public ArrayList<ArrayList<Integer>> injectError(ArrayList<ArrayList<Integer>> blocks, int blockSize) {
        ArrayList<ArrayList<Integer>> corruptedBlocks = new ArrayList<>();

        for (ArrayList<Integer> block : blocks) {
            ArrayList<Integer> corruptedBlock = new ArrayList<>(block);

            // Random error type: 0=single, 1=two, 2=burst, 3=odd number, rest=no error
            int errorType = random.nextInt(10); 
            // int errorType = 3;

            switch (errorType) {
                case 0:
                    corruptedBlock = injectSingleBitError(corruptedBlock, blockSize);
                    break;
                case 1:
                    corruptedBlock = injectTwoBitError(corruptedBlock, blockSize);
                    break;
                case 2:
                    // I have set burst length to be between 2 and 4 bits
                    int burstLength = random.nextInt(3) + 2;
                    corruptedBlock = injectBurstError(corruptedBlock, blockSize, burstLength);
                    break;
                case 3:
                    int oddErrors = random.nextInt(5) * 2 + 1;
                    corruptedBlock = injectOddNumberError(corruptedBlock, blockSize, oddErrors);
                    break;
                default:
                    break;
            }

            corruptedBlocks.add(corruptedBlock);
        }

        System.out.println("Corrupted Blocks: " + corruptedBlocks);
        return corruptedBlocks;
    }

    
    public ArrayList<Integer> injectSingleBitError(ArrayList<Integer> block, int blockSize) {
        int bitPosition = random.nextInt(blockSize * 8);
        return flipBit(block, bitPosition);
    }

    
    public ArrayList<Integer> injectTwoBitError(ArrayList<Integer> block, int blockSize) {
        int bit1 = random.nextInt(blockSize * 8);
        int bit2;
        do {
            bit2 = random.nextInt(blockSize * 8);
        } while (bit2 == bit1);

        ArrayList<Integer> newBlock = flipBit(block, bit1);
        return flipBit(newBlock, bit2);
    }

    
    public ArrayList<Integer> injectBurstError(ArrayList<Integer> block, int blockSize, int burstLength) {
        int start = random.nextInt(blockSize * 8 - burstLength + 1);
        ArrayList<Integer> newBlock = new ArrayList<>(block);

        for (int i = 0; i < burstLength; i++) {
            newBlock = flipBit(newBlock, start + i);
        }

        return newBlock;
    }

    
    public ArrayList<Integer> injectOddNumberError(ArrayList<Integer> block, int blockSize, int numErrors) {
        ArrayList<Integer> newBlock = new ArrayList<>(block);
        int totalBits = blockSize * 8;

        numErrors = Math.min(numErrors, totalBits);

        
        HashSet<Integer> bitPositions = new HashSet<>();

        while (bitPositions.size() < numErrors) {
            bitPositions.add(random.nextInt(totalBits));
        }

        
        for (int bit : bitPositions) {
            newBlock = flipBit(newBlock, bit);
        }

        return newBlock;
    }



    private ArrayList<Integer> flipBit(ArrayList<Integer> block, int bitPosition) {
        ArrayList<Integer> newBlock = new ArrayList<>(block);

        int byteIndex = bitPosition / 8;
        int bitIndex = bitPosition % 8;

        int value = newBlock.get(byteIndex);
        value ^= (1 << bitIndex);
        newBlock.set(byteIndex, value);

        return newBlock;
    }
}
