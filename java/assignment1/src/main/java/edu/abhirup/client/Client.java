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

        CrcCalculator crcCalculator = new CrcCalculator();
        blocks = crcCalculator.calculateCrc(blocks, "1011");

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
