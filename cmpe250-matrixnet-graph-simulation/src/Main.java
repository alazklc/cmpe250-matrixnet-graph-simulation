import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.StringTokenizer;

// Entry point for the application
public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java Main <input_file> <output_file>");
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];

        MatrixNet matrixNet = new MatrixNet();

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile));
                PrintWriter pw = new PrintWriter(
                        new java.io.BufferedOutputStream(new java.io.FileOutputStream(outputFile)))) {

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;

                StringTokenizer st = new StringTokenizer(line);
                if (!st.hasMoreTokens())
                    continue;

                String command = st.nextToken();
                String result = "";

                // Execute commands based on the action specified
                switch (command) {
                    case "spawn_host":
                        if (st.countTokens() == 2) {
                            String id = st.nextToken();
                            int clearance = Integer.parseInt(st.nextToken());
                            result = matrixNet.spawnHost(id, clearance);
                        }
                        break;
                    case "link_backdoor":
                        if (st.countTokens() == 5) {
                            String id1 = st.nextToken();
                            String id2 = st.nextToken();
                            int latency = Integer.parseInt(st.nextToken());
                            int bandwidth = Integer.parseInt(st.nextToken());
                            int firewall = Integer.parseInt(st.nextToken());
                            result = matrixNet.linkBackdoor(id1, id2, latency, bandwidth, firewall);
                        }
                        break;
                    case "seal_backdoor":
                        if (st.countTokens() == 2) {
                            String id1 = st.nextToken();
                            String id2 = st.nextToken();
                            result = matrixNet.sealBackdoor(id1, id2);
                        }
                        break;
                    case "trace_route":
                        if (st.countTokens() == 4) {
                            String id1 = st.nextToken();
                            String id2 = st.nextToken();
                            int minBandwidth = Integer.parseInt(st.nextToken());
                            int lambda = Integer.parseInt(st.nextToken());
                            result = matrixNet.traceRoute(id1, id2, minBandwidth, lambda);
                        }
                        break;
                    case "scan_connectivity":
                        result = matrixNet.scanConnectivity();
                        break;
                    case "simulate_breach":
                        if (st.countTokens() == 1) {
                            String id = st.nextToken();
                            result = matrixNet.simulateBreach(id);
                        } else if (st.countTokens() == 2) {
                            String id1 = st.nextToken();
                            String id2 = st.nextToken();
                            result = matrixNet.simulateBreach(id1, id2);
                        }
                        break;
                    case "oracle_report":
                        result = matrixNet.oracleReport();
                        break;
                    default:
                        break;
                }

                if (!result.isEmpty()) {
                    pw.println(result);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
