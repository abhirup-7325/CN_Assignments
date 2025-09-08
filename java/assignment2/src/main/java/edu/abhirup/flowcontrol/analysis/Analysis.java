package edu.abhirup.flowcontrol.analysis;


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class Analysis {
    private static final int BANDWIDTH = 4000; // bps

    public static void generateReport(String senderName, String receiverName, String analysisFileName,
                                      int effectivePkt, int totalPkt, double totalTime, List<Double> rttStore) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(analysisFileName, true))) {
            writer.println("\n" + senderName + " is sending data to " + receiverName + "--------------------");
            writer.printf("Total packets sent = %d\n", totalPkt);
            writer.printf("Effective packets sent = %d\n", effectivePkt);
            writer.printf("Total time taken = %.6f minutes\n", totalTime / 60.0);

            // Assuming packet size of 72 bytes (46 payload + 12 header + 4 trailer + ~10 for SFD/preamble) for throughput calculation
            double throughput = (effectivePkt * 72 * 8) / totalTime;
            writer.printf("Receiver Throughput = %d bps\n", (int) throughput);

            double efficiency = (throughput / BANDWIDTH);
            writer.printf("Utilization percentage = %.2f %%\n", efficiency * 100);
            
            double avgRtt = rttStore.stream().mapToDouble(d -> d).average().orElse(0.0);
            writer.printf("Average RTT = %.6f seconds\n", avgRtt / 1000.0);

            writer.println();
        } catch (IOException e) {
            System.err.println("Error writing to analysis file: " + e.getMessage());
        }
    }
}