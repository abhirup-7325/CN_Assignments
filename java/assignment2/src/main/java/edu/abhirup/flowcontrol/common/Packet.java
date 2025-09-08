package edu.abhirup.flowcontrol.common;


public class Packet {
    private final long source;
    private final long destination;
    private final int type;
    private final int seqNo;
    private final String segmentData;
    public String packetString;

    private static final String DIVISOR = "100000100110000010001110110110111";

    public Packet(long source, long destination, int type, int seqNo, String segmentData) {
        this.source = source;
        this.destination = destination;
        this.type = type;
        this.seqNo = seqNo;
        this.segmentData = segmentData;
    }

    public String toBinaryString(int dataSize) {
        String preamble = "01010101010101010101010101010101010101010101010101010101";
        String sfd = "10101011";
        String destAddress = String.format("%48s", Long.toBinaryString(this.destination)).replace(' ', '0');
        String sourceAddress = String.format("%48s", Long.toBinaryString(this.source)).replace(' ', '0');
        String typeToBits = String.format("%8s", Integer.toBinaryString(this.type)).replace(' ', '0');
        String seqToBits = String.format("%8s", Integer.toBinaryString(this.seqNo)).replace(' ', '0');

        StringBuilder dataBuilder = new StringBuilder();
        String currentSegmentData = this.segmentData;
        
        while (currentSegmentData.length() < dataSize) {
            currentSegmentData += '\0';
        }

        for (char character : currentSegmentData.toCharArray()) {
            dataBuilder.append(String.format("%8s", Integer.toBinaryString(character)).replace(' ', '0'));
        }

        String packetWithoutCRC = preamble + sfd + destAddress + sourceAddress + typeToBits + seqToBits + dataBuilder.toString();
        this.packetString = CRC.generateCRC(packetWithoutCRC, DIVISOR);
        return this.packetString;
    }

    public static Packet build(String binaryString) {
        long source = Long.parseLong(binaryString.substring(112, 160), 2);
        long destination = Long.parseLong(binaryString.substring(64, 112), 2);
        int type = Integer.parseInt(binaryString.substring(160, 168), 2);
        int seqNo = Integer.parseInt(binaryString.substring(168, 176), 2);

        String dataBits = binaryString.substring(176, binaryString.length() - 32);
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < dataBits.length(); i += 8) {
            String byteStr = dataBits.substring(i, i + 8);
            if (byteStr.equals("00000000")) continue;
            text.append((char) Integer.parseInt(byteStr, 2));
        }

        Packet newPacket = new Packet(source, destination, type, seqNo, text.toString());
        newPacket.packetString = binaryString;
        return newPacket;
    }

    public String getSegmentData() { return segmentData; }
    public int getType() { return type; }
    public int getSeqNo() { return seqNo; }
    public boolean hasError() { return CRC.checkCRC(this.packetString, DIVISOR); }
}