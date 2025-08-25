package edu.abhirup.client;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.net.Socket;
import edu.abhirup.client.utils.BlockBuilder;
import edu.abhirup.client.utils.ChecksumCalculator;
import edu.abhirup.client.utils.CrcCalculator;
import edu.abhirup.client.utils.StringConverter;
import edu.abhirup.client.utils.ErrorInjector;

public class Client {
    public static void main(String[] args) {
        int DATA_BLOCK_SIZE = 4;
        int CODE_BLOCK_SIZE = DATA_BLOCK_SIZE + 2;
        String INPUT_FILE_PATH = "src/main/resources/io/input.txt";

        BlockBuilder blockBuilder = new BlockBuilder();
        ArrayList<ArrayList<Integer>> blocks = blockBuilder.readBlocksFromInputBox(DATA_BLOCK_SIZE);

        // ChecksumCalculator checksumCalculator = new ChecksumCalculator();
        // blocks = checksumCalculator.calculateChecksum(blocks);

        String crc8 = "111010101";
        String crc10 = "11000110011";
        String crc16 = "11000000000000101";
        String crc32 = "100000100110000010001110110110111";

        CrcCalculator crcCalculator = new CrcCalculator();
        blocks = crcCalculator.calculateCrc(blocks, crc16);

        ErrorInjector errorInjector = new ErrorInjector();
        blocks = errorInjector.injectError(blocks, CODE_BLOCK_SIZE);

        StringConverter stringConverter = new StringConverter();
        String str = stringConverter.blocksToString(blocks);

        try {
            Socket socket = new Socket("localhost", 9999);
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            out.println(str);

            out.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
