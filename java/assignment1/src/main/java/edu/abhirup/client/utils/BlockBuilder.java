package edu.abhirup.client.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import edu.abhirup.client.gui.InputBoxWindow;

public class BlockBuilder {

    public ArrayList<ArrayList<Integer>> buildBlocks(String str, int blockSize) {
        ArrayList<ArrayList<Integer>> blocks = new ArrayList<>();
        for (int i = 0; i < str.length(); i += blockSize) {
            ArrayList<Integer> block = new ArrayList<>();
            for (int j = 0; j < blockSize; j++) {
                if (i + j < str.length()) {
                    block.add((int) str.charAt(i + j));
                } else {
                    block.add(0);
                }
            }
            blocks.add(block);
        }

        System.out.println("Blocks: " + blocks);
        return blocks;
    }

    public ArrayList<ArrayList<Integer>> readBlocksFromFile(String filePath, int blockSize) {
        String str = "";

        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            int content;
            while ((content = fis.read()) != -1) {
                str += (char) content;
            }
            System.out.println("String size: " + str.length());
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return buildBlocks(str, blockSize);
    }

    public ArrayList<ArrayList<Integer>> readBlocksFromInputBox(int blockSize) {
        String userInput = InputBoxWindow.showInputDialog(null);

        String result = "localhost " + "localhost " + userInput;

        return buildBlocks(result, blockSize);
    }
}
