package edu.abhirup.flowcontrol.common;


public class CRC {

    private static String xor(String a, String b) {
        StringBuilder result = new StringBuilder();
        for (int i = 1; i < b.length(); i++) {
            if (a.charAt(i) == b.charAt(i)) {
                result.append("0");
            } else {
                result.append("1");
            }
        }
        return result.toString();
    }

    private static String binaryDivision(String dividend, String divisor) {
        int divisorLength = divisor.length();
        int dividendLength = dividend.length();
        String temp = dividend.substring(0, divisorLength);

        while (divisorLength < dividendLength) {
            if (temp.charAt(0) == '1') {
                temp = xor(divisor, temp) + dividend.charAt(divisorLength);
            } else {
                StringBuilder zeros = new StringBuilder();
                for(int i=0; i<divisorLength; i++) zeros.append("0");
                temp = xor(zeros.toString(), temp) + dividend.charAt(divisorLength);
            }
            divisorLength++;
        }

        if (temp.charAt(0) == '1') {
            temp = xor(divisor, temp);
        } else {
            StringBuilder zeros = new StringBuilder();
            for(int i=0; i<divisorLength; i++) zeros.append("0");
            temp = xor(zeros.toString(), temp);
        }
        return temp;
    }

    public static String generateCRC(String data, String poly) {
        int polyLength = poly.length();
        StringBuilder appendedData = new StringBuilder(data);
        for (int i = 0; i < polyLength - 1; i++) {
            appendedData.append("0");
        }
        String remainder = binaryDivision(appendedData.toString(), poly);
        return data + remainder;
    }

    public static boolean checkCRC(String data, String poly) {
        String remainder = binaryDivision(data, poly);
        for (int i = 0; i < remainder.length(); i++) {
            if (remainder.charAt(i) == '1') {
                return true; // Error detected
            }
        }
        return false; // No error
    }
}