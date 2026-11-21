import java.io.*;
import java.util.*;

public class DailySalesReportGenerator {
    // -------------------------------------------------------------------------
    // VERSION 1: Manual Resource Management (Vulnerable to File Descriptor Leaks)
    // -------------------------------------------------------------------------
    public static int generateReportManual(List<String> storeFiles, String summaryFile) {
        System.out.println("  [Executing Version 1: Manual try-finally]");
        BufferedWriter writer = null;
        int filesProcessed = 0;
        int leakedHandles = 0;

        try {
            writer = new BufferedWriter(new FileWriter(summaryFile));
            writer.write("=== DAILY RETAIL SALES SUMMARY REPORT (MANUAL) ===\n");

            for (String file : storeFiles) {
                BufferedReader reader = null;
                try {
                    File f = new File(file);
                    if (!f.exists()) {
                        throw new FileNotFoundException("Store file missing: " + file);
                    }
                    reader = new BufferedReader(new FileReader(f));
                    String line = reader.readLine();
                    if (line != null && line.contains("CORRUPT")) {
                        throw new IOException("Corrupt payload detected in store file: " + file);
                    }
                    writer.write("Store Data [" + file + "]: " + line + "\n");
                    filesProcessed++;

                    // If an exception occurs, reader.close() is SKIPPED here!
                    reader.close();
                } catch (IOException e) {
                    System.err.printf("    [-] Failure on file '%s': %s%n", file, e.getMessage());
                    leakedHandles++; // Unclosed file handle!
                    // In naive code, exception inside loop without proper nested finally leaks descriptor
                }
            }
        } catch (IOException e) {
            System.err.println("    [-] Master summary writer failure: " + e.getMessage());
        } finally {
            // Manual closing of summary writer
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.err.println("    [-] Exception thrown while closing writer: " + e.getMessage());
                }
            }
        }
        System.out.printf("  [Result Manual]: Processed %d files | Leaked File Handles: %d%n", filesProcessed, leakedHandles);
        return filesProcessed;
    }

    // -------------------------------------------------------------------------
    // VERSION 2: Modern Java try-with-resources (Guaranteed Leak-Free)
    // -------------------------------------------------------------------------
    public static int generateReportRobust(List<String> storeFiles, String summaryFile) {
        System.out.println("  [Executing Version 2: Robust try-with-resources]");
        int filesProcessed = 0;

        // AutoCloseable writer automatically closed on exit
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(summaryFile))) {
            writer.write("=== DAILY RETAIL SALES SUMMARY REPORT (ROBUST) ===\n");

            for (String file : storeFiles) {
                File f = new File(file);
                if (!f.exists()) {
                    System.err.printf("    [-] Skipping missing file '%s'%n", file);
                    continue;
                }

                // Nested try-with-resources: Reader is GUARANTEED to be closed
                // even if an IOException is thrown during readLine()!
                try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
                    String line = reader.readLine();
                    if (line != null && line.contains("CORRUPT")) {
                        throw new IOException("Corrupt payload detected in store file: " + file);
                    }
                    writer.write("Store Data [" + file + "]: " + line + "\n");
                    filesProcessed++;
                } catch (IOException e) {
                    System.err.printf("    [-] Handled error on '%s': %s (Resource closed safely by TWR)%n", file, e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("    [-] Master writer error: " + e.getMessage());
        }

        System.out.printf("  [Result Robust]: Processed %d files | Leaked File Handles: 0 (100%% Clean)%n", filesProcessed);
        return filesProcessed;
    }

    // -------------------------------------------------------------------------
    // DEMONSTRATION OF SUPPRESSED EXCEPTIONS
    // -------------------------------------------------------------------------
    static class BrokenResource implements AutoCloseable {
        private final String name;
        BrokenResource(String name) { this.name = name; }
        public void doWork() throws IOException {
            throw new IOException("Primary failure during data read in " + name);
        }
        @Override
        public void close() throws IOException {
            throw new IOException("Secondary failure during close() in " + name);
        }
    }

    public static void demonstrateSuppressedExceptions() {
        System.out.println("\n>>> STUDENT B: SUPPRESSED EXCEPTIONS DEMONSTRATION");
        System.out.println("--------------------------------------------------------------------------------");
        try (BrokenResource res = new BrokenResource("StoreFile_99")) {
            res.doWork();
        } catch (IOException e) {
            System.out.println("  Caught Primary Exception: " + e.getMessage());
            Throwable[] suppressed = e.getSuppressed();
            System.out.println("  Suppressed Exceptions Count: " + suppressed.length);
            for (Throwable s : suppressed) {
                System.out.println("    -> [Suppressed]: " + s.getMessage());
            }
            System.out.println("  [EXPLANATION]: try-with-resources preserves BOTH primary and close() exceptions");
            System.out.println("                 without masking the root cause!");
        }
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   SIMATS ENGINEERING - CO3 ASSIGNMENT 3: TASK 3 (Q3) RESOURCE SAFETY LAB       ");
        System.out.println("   Student A: P. Madesh (192365007)  |  Student B: R. Vignesh (192365018)       ");
        System.out.println("   Faculty: Surya V  |  GitHub: https://github.com/madeshp006/CSA0907           ");
        System.out.println("================================================================================\n");

        // Prepare test store files
        List<String> storeFiles = new ArrayList<>();
        try {
            // File 1: Valid
            File f1 = new File("store_01.csv");
            try (FileWriter fw = new FileWriter(f1)) { fw.write("Store 01: Sales = Rs 120,000"); }
            storeFiles.add("store_01.csv");

            // File 2: Valid
            File f2 = new File("store_02.csv");
            try (FileWriter fw = new FileWriter(f2)) { fw.write("Store 02: Sales = Rs 85,000"); }
            storeFiles.add("store_02.csv");

            // File 3: Corrupt (causes read exception)
            File f3 = new File("store_03_corrupt.csv");
            try (FileWriter fw = new FileWriter(f3)) { fw.write("Store 03: CORRUPT_PAYLOAD_OFFSET"); }
            storeFiles.add("store_03_corrupt.csv");

            // File 4: Missing (causes FileNotFound)
            storeFiles.add("store_04_missing.csv");

            // File 5: Valid
            File f5 = new File("store_05.csv");
            try (FileWriter fw = new FileWriter(f5)) { fw.write("Store 05: Sales = Rs 210,000"); }
            storeFiles.add("store_05.csv");

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(">>> STUDENT A DEBUGGING ANALYSIS: RUNNING WITH FAILING INPUTS");
        System.out.println("--------------------------------------------------------------------------------");
        generateReportManual(storeFiles, "summary_manual.txt");

        System.out.println();
        generateReportRobust(storeFiles, "summary_robust.txt");

        demonstrateSuppressedExceptions();

        // Clean up test files
        for (String s : storeFiles) {
            File f = new File(s);
            if (f.exists()) f.delete();
        }
        new File("summary_manual.txt").delete();
        new File("summary_robust.txt").delete();

        System.out.println("\n================================================================================");
        System.out.println(" [JOINT DECISION]: try-with-resources guarantees LIFO closing and suppresses    ");
        System.out.println("                   secondary exceptions. Eliminates 'Too Many Open Files' bug! ");
        System.out.println("================================================================================");
    }
}
