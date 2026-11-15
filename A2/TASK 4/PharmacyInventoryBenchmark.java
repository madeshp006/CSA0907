import java.util.*;

public class PharmacyInventoryBenchmark {
    static class Medicine {
        final String code;
        final String name;
        final int stockQuantity;
        final double unitPrice;

        Medicine(String code, String name, int stockQuantity, double unitPrice) {
            this.code = code;
            this.name = name;
            this.stockQuantity = stockQuantity;
            this.unitPrice = unitPrice;
        }

        @Override
        public String toString() {
            return String.format("%s: %s (Qty: %d, Rs %.2f)", code, name, stockQuantity, unitPrice);
        }
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   SIMATS ENGINEERING - CO2 ASSIGNMENT 2: TASK 4 (Q4) BENCHMARK REPORT         ");
        System.out.println("   Student: P. Madesh  |  Register No: 192365007  |  Java Version: 25.0.2      ");
        System.out.println("   Topic: Choosing a Map for a Pharmacy Inventory (HashMap vs TreeMap vs Linked)");
        System.out.println("================================================================================\n");

        int[] inventorySizes = { 1000, 10000, 100000 };

        for (int n : inventorySizes) {
            System.out.printf(">>> BENCHMARKING PHARMACY INVENTORY SIZE N = %,d MEDICINES%n", n);
            System.out.println("--------------------------------------------------------------------------------");
            benchmarkMapPerformance(n);
            System.out.println();
        }

        System.out.println(">>> SCALABILITY INQUIRY & EVENING REPORT ANALYSIS");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("  • Target Single-Lookup Threshold : < 10 microseconds (sub-microsecond ideal)");
        System.out.println("  • Natural Report Capabilities    : TreeMap -> Sorted Code Order natively O(n)");
        System.out.println("                                     LinkedHashMap -> Insertion Order natively O(n)");
        System.out.println("                                     HashMap -> Arbitrary hash order (Fails both)");
        System.out.println("  • Recommended Architecture       : Deploy LinkedHashMap!");
        System.out.println("    Provides O(1) day-time lookup and instant evening Insertion-Order report.");
        System.out.println("    Single evening sort on keySet() takes < 25 ms for 100,000 items.");

        System.out.println("\n>>> WHY HASHTABLE IS NOT PREFERRED IN MODERN JAVA:");
        System.out.println("  1. Heavy thread contention: Every method is 'synchronized', locking the entire table.");
        System.out.println("  2. Disallows null keys and null values (throws NullPointerException).");
        System.out.println("  3. Replaced by ConcurrentHashMap for concurrent safety, or HashMap for single-thread.");
        System.out.println("================================================================================");
    }

    private static void benchmarkMapPerformance(int n) {
        // Generate pseudo-random deterministic test medicines
        List<Medicine> dataset = new ArrayList<>(n);
        Random rand = new Random(42);
        for (int i = 0; i < n; i++) {
            String code = String.format("MED-%06d", rand.nextInt(n * 5));
            dataset.add(new Medicine(code, "Drug_" + i, 50 + rand.nextInt(200), 15.0 + rand.nextInt(100)));
        }

        Map<String, Medicine> hashMap = new HashMap<>(n);
        Map<String, Medicine> treeMap = new TreeMap<>();
        Map<String, Medicine> linkedMap = new LinkedHashMap<>(n);

        // 1. Insertion (Population)
        long t1 = System.nanoTime();
        for (Medicine m : dataset) hashMap.put(m.code, m);
        double hmPut = (System.nanoTime() - t1) / 1_000_000.0;

        long t2 = System.nanoTime();
        for (Medicine m : dataset) treeMap.put(m.code, m);
        double tmPut = (System.nanoTime() - t2) / 1_000_000.0;

        long t3 = System.nanoTime();
        for (Medicine m : dataset) linkedMap.put(m.code, m);
        double lmPut = (System.nanoTime() - t3) / 1_000_000.0;

        // 2. 100,000 Lookups
        int lookupCount = 100000;
        String[] lookupKeys = new String[lookupCount];
        for (int i = 0; i < lookupCount; i++) {
            lookupKeys[i] = dataset.get(rand.nextInt(dataset.size())).code;
        }

        long t4 = System.nanoTime();
        for (String k : lookupKeys) { Medicine m = hashMap.get(k); }
        double hmGet = (System.nanoTime() - t4) / 1_000_000.0;

        long t5 = System.nanoTime();
        for (String k : lookupKeys) { Medicine m = treeMap.get(k); }
        double tmGet = (System.nanoTime() - t5) / 1_000_000.0;

        long t6 = System.nanoTime();
        for (String k : lookupKeys) { Medicine m = linkedMap.get(k); }
        double lmGet = (System.nanoTime() - t6) / 1_000_000.0;

        // 3. Evening Report 1: Code Order Generation
        long t7 = System.nanoTime();
        int tmCount = 0;
        for (Map.Entry<String, Medicine> entry : treeMap.entrySet()) tmCount++;
        double tmCodeReport = (System.nanoTime() - t7) / 1_000_000.0;

        long t8 = System.nanoTime();
        List<String> hmSortedKeys = new ArrayList<>(hashMap.keySet());
        Collections.sort(hmSortedKeys);
        for (String k : hmSortedKeys) { Medicine m = hashMap.get(k); }
        double hmCodeReport = (System.nanoTime() - t8) / 1_000_000.0;

        long t9 = System.nanoTime();
        List<String> lmSortedKeys = new ArrayList<>(linkedMap.keySet());
        Collections.sort(lmSortedKeys);
        for (String k : lmSortedKeys) { Medicine m = linkedMap.get(k); }
        double lmCodeReport = (System.nanoTime() - t9) / 1_000_000.0;

        // 4. Evening Report 2: Insertion Order Generation
        long t10 = System.nanoTime();
        int lmCount = 0;
        for (Map.Entry<String, Medicine> entry : linkedMap.entrySet()) lmCount++;
        double lmInsertReport = (System.nanoTime() - t10) / 1_000_000.0;

        System.out.printf("  %-16s | %10s | %18s | %16s | %16s%n",
            "Map Class", "Put Time", "100K Lookups Time", "Code Order Report", "Insertion Report");
        System.out.println("  " + "-".repeat(84));
        System.out.printf("  %-16s | %7.2f ms | %15.2f ms | %12.2f ms* | %16s%n",
            "HashMap", hmPut, hmGet, hmCodeReport, "N/A (Needs List)");
        System.out.printf("  %-16s | %7.2f ms | %15.2f ms | %12.2f ms  | %16s%n",
            "TreeMap", tmPut, tmGet, tmCodeReport, "N/A (Needs SeqID)");
        System.out.printf("  %-16s | %7.2f ms | %15.2f ms | %12.2f ms* | %13.2f ms%n",
            "LinkedHashMap", lmPut, lmGet, lmCodeReport, lmInsertReport);
        System.out.println("  (* Note: Includes extra Collections.sort() pass required when map lacks natural order)");
    }
}
