import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TASK B1: Complete the Code - Bus Pass File Store
 * 
 * Student: P. Madesh (192365007)
 * Faculty Evaluator: Surya V
 * Course: CSA09 - Programming in Java (CO5: Data Persistence Engineering)
 * 
 * Completed TODO 1:
 * - Reads file line-by-line using BufferedReader inside try-with-resources.
 * - Robustly validates token count; skips damaged records (fields.length != 5).
 * - Matches pass ID and returns matching record array, or null if absent.
 */
public class PassStore {
    private final Path file;

    public PassStore(Path file) {
        this.file = file;
    }

    public void add(String passId, String regNo, String name, int route, double amount)
            throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(file,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            w.write(String.join(",", passId, regNo, name,
                    String.valueOf(route), String.valueOf(amount)));
            w.newLine();
        }
    }

    /** Returns the five fields of the pass with the given ID, or null if not found. */
    public String[] find(String passId) throws IOException {
        // TODO 1: Read the file line by line with a BufferedReader inside try-with-
        // resources; skip damaged lines (not exactly 5 fields); return the fields of
        // the matching pass
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue; // Skip empty lines
                }
                String[] fields = line.split(",", -1);
                if (fields.length != 5) {
                    continue; // Skip damaged lines that do not have exactly 5 fields
                }
                if (fields[0].trim().equals(passId)) {
                    return fields; // Matching pass found
                }
            }
        }
        return null; // Pass ID not found
    }

    /** Returns all valid passes assigned to a specific bus route */
    public List<String[]> listByRoute(int routeNo) throws IOException {
        List<String[]> matches = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] fields = line.split(",", -1);
                if (fields.length != 5) continue;
                try {
                    int r = Integer.parseInt(fields[3].trim());
                    if (r == routeNo) {
                        matches.add(fields);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return matches;
    }

    public static void main(String[] args) throws IOException {
        Path f = Files.createTempFile("passes", ".csv");
        PassStore store = new PassStore(f);
        store.add("P1001", "192311001", "Arun", 12, 4500);
        Files.writeString(f, "P1002,DAMAGED LINE\n", StandardOpenOption.APPEND);
        store.add("P1003", "192311003", "Divya", 7, 3800);
        
        System.out.println(Arrays.toString(store.find("P1003")));
        System.out.println(Arrays.toString(store.find("P1002")));
        
        Files.delete(f);
    }
}
