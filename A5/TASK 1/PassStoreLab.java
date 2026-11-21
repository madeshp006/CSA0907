import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

/**
 * TASK B1: Comprehensive Experimental Validation & Growth Measurement Suite
 * 
 * Verifies:
 * - B1.1: Validation on normal, non-existent, and damaged lines.
 * - B1.4: Search time latency as records grow from 1,000 -> 10,000 -> 100,000.
 */
public class PassStoreLab {

    public static void main(String[] args) throws IOException {
        System.out.println("================================================================================");
        System.out.println("  SIMATS TRANSPORT OFFICE - BUS PASS FILE STORE VERIFICATION LAB (TASK B1)       ");
        System.out.println("================================================================================");

        Path testFile = Files.createTempFile("bus_passes_lab", ".csv");
        PassStore store = new PassStore(testFile);

        // 1. Validation Tests (B1.1)
        System.out.println("\n[B1.1] Executing Functional Boundary Tests...");
        store.add("PASS-001", "192365007", "P. Madesh", 14, 4200.0);
        store.add("PASS-002", "192365018", "R. Vignesh", 14, 4200.0);
        store.add("PASS-003", "192365022", "S. Priya", 7, 3600.0);
        // Inject damaged lines
        Files.writeString(testFile, "PASS-CORRUPT,INVALID_TOKEN_COUNT\n", StandardOpenOption.APPEND);
        Files.writeString(testFile, "\n", StandardOpenOption.APPEND); // empty line
        store.add("PASS-004", "192365035", "K. Arjun", 14, 4200.0);

        // Test A: Normal Case
        String[] normal = store.find("PASS-001");
        System.out.println("  [Test Normal] Finding 'PASS-001': " + Arrays.toString(normal));

        // Test B: Non-Existent ID
        String[] missing = store.find("PASS-999");
        System.out.println("  [Test Missing] Finding 'PASS-999': " + Arrays.toString(missing) + " (Expected: null)");

        // Test C: Corrupted Record Recovery
        String[] corrupt = store.find("PASS-CORRUPT");
        System.out.println("  [Test Corrupt Line] Finding 'PASS-CORRUPT': " + Arrays.toString(corrupt) + " (Cleanly skipped)");

        // Test D: Subsequent Record Found After Corrupt Line
        String[] afterCorrupt = store.find("PASS-004");
        System.out.println("  [Test Post-Corruption Recovery] Finding 'PASS-004': " + Arrays.toString(afterCorrupt));

        // Test E: Listing by Route
        List<String[]> route14Passes = store.listByRoute(14);
        System.out.println("  [Test List by Route 14] Found " + route14Passes.size() + " passes (Expected: 3).");

        // 2. Growth Measurement (B1.4)
        System.out.println("\n[B1.4] Measuring Growth: Search Latency vs File Scale (O(n) Linear Scan)...");
        int[] recordScales = {1_000, 10_000, 100_000};

        for (int count : recordScales) {
            Path scaleFile = Files.createTempFile("scale_" + count, ".csv");
            PassStore scaleStore = new PassStore(scaleFile);
            
            // Populate records in bulk
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= count; i++) {
                sb.append(String.format("P-%06d,REG%06d,Student_%d,%d,%.1f\n",
                        i, i, i, (i % 20) + 1, 3500.0 + (i % 1000)));
                if (i % 5000 == 0) {
                    Files.writeString(scaleFile, sb.toString(), StandardOpenOption.APPEND);
                    sb.setLength(0);
                }
            }
            if (sb.length() > 0) {
                Files.writeString(scaleFile, sb.toString(), StandardOpenOption.APPEND);
            }

            // Benchmark worst-case search (last record in file)
            String targetId = String.format("P-%06d", count);
            long start = System.nanoTime();
            String[] res = scaleStore.find(targetId);
            long elapsedNs = System.nanoTime() - start;
            double elapsedMs = elapsedNs / 1_000_000.0;
            long fileSizeKb = Files.size(scaleFile) / 1024;

            System.out.printf("  Records: %,7d | File Size: %,6d KB | Target: %s | Search Time: %7.2f ms | Status: %s\n",
                    count, fileSizeKb, targetId, elapsedMs, (res != null ? "FOUND" : "NOT FOUND"));

            Files.delete(scaleFile);
        }

        Files.delete(testFile);
        System.out.println("\n[B1 Complete] All empirical validations and growth benchmarks executed.");
    }
}
