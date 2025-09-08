package edu.abhirup.flowcontrol.common;


import java.util.Random;

public class ErrorInsertion {

    private static final Random random = new Random();

    private static String singleError(String data) {
        int n = data.length();
        int index = random.nextInt(n);
        char[] edata = data.toCharArray();
        edata[index] = (edata[index] == '0') ? '1' : '0';
        return new String(edata);
    }

    public static String injectError(String data) {
        // For simplicity, we only implement single bit error injection
        return singleError(data);
    }
}