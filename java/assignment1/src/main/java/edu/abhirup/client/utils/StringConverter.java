package edu.abhirup.client.utils;

import java.util.ArrayList;

public class StringConverter {
    public String blocksToString(ArrayList<ArrayList<Integer>> blocks) {
        StringBuilder sb = new StringBuilder();
        for (java.util.ArrayList<Integer> block : blocks) {
            for (Integer value : block) {
                sb.append((char) value.intValue());
            }
        }
        String result = sb.toString();
        System.out.println("Converted String: " + result);
        System.out.println("String length: " + result.length());
        
        return result;
    }
}
