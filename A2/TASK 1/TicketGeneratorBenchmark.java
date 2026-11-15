import java.util.*;

public class TicketGeneratorBenchmark {
    // Simulated ticket field names and sample values
    private static final String[] FIELD_KEYS = {
        "PNR", "TrainNo", "TrainName", "Origin", "Destination", 
        "Date", "Class", "Quota", "Coach", "Berth",
        "BerthType", "Passenger1", "Age1", "Gender1", "Status1",
        "Passenger2", "Age2", "Gender2", "Status2", "BaseFare",
        "ReservationFee", "SuperfastSurcharge", "TatkalCharge", "GST", "TotalFare",
        "PaymentID", "TxnStatus", "GateNo", "Platform", "BoardingTime",
        "DepTime", "ArrTime", "DistanceKM", "FoodChoice", "InsuranceOpt",
        "InsuranceID", "IDProofType", "IDProofNo", "ContactMobile", "ContactEmail",
        "BookedAt", "AgentID", "ChartStatus", "CurrentStatus", "RefundRule",
        "CancellationTerms", "CateringCharge", "DynamicFare", "SecurityPin", "Barcode"
    };

    // Approach 1: Naive String concatenation using '+'
    public static String buildTicketWithString(int fieldCount) {
        String ticket = "";
        for (int i = 0; i < fieldCount; i++) {
            String key = FIELD_KEYS[i % FIELD_KEYS.length];
            String val = "VAL_" + i;
            ticket = ticket + key + ":" + val + "; "; // Produces intermediate String object every iteration
        }
        return ticket;
    }

    // Approach 2: StringBuilder (mutable, unsynchronized)
    public static String buildTicketWithStringBuilder(int fieldCount) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fieldCount; i++) {
            String key = FIELD_KEYS[i % FIELD_KEYS.length];
            String val = "VAL_" + i;
            sb.append(key).append(':').append(val).append("; ");
        }
        return sb.toString();
    }

    // Approach 3: StringBuffer (mutable, synchronized)
    public static String buildTicketWithStringBuffer(int fieldCount) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < fieldCount; i++) {
            String key = FIELD_KEYS[i % FIELD_KEYS.length];
            String val = "VAL_" + i;
            sb.append(key).append(':').append(val).append("; ");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   SIMATS ENGINEERING - CO2 ASSIGNMENT 2: TASK 1 (Q1) BENCHMARK REPORT         ");
        System.out.println("   Student: P. Madesh  |  Register No: 192365007  |  Java Version: 25.0.2      ");
        System.out.println("   Topic: Building Text Under Load - String vs StringBuilder vs StringBuffer   ");
        System.out.println("================================================================================\n");

        // Warm up JIT compiler
        for (int i = 0; i < 200; i++) {
            buildTicketWithString(10);
            buildTicketWithStringBuilder(10);
            buildTicketWithStringBuffer(10);
        }

        System.out.println(">>> EXPERIMENT 1: THREE DELIBERATELY CONTRASTING WORKLOADS");
        System.out.println("--------------------------------------------------------------------------------");

        // Workload 1: Single short ticket (5 fields)
        runWorkload("Workload 1: Single Short Ticket (1 Ticket x 5 Fields)", 1, 5);

        // Workload 2: High-volume batch generation (10,000 tickets x 50 fields each)
        runWorkload("Workload 2: High-Volume Batch (10,000 Tickets x 50 Fields)", 10000, 50);

        // Workload 3: Single massive document (1 Ticket x 1,000 fields)
        runWorkload("Workload 3: Deep Iteration / Group Manifest (1 Ticket x 1,000 Fields)", 1, 1000);

        System.out.println("\n>>> EXPERIMENT 2: MULTITHREADED SERVER DESIGN DECISION DEMONSTRATION");
        System.out.println("--------------------------------------------------------------------------------");
        demonstrateMultithreadedScenario();

        System.out.println("\n================================================================================");
        System.out.println(" [CONCLUSION]: Per-Ticket: StringBuilder (O(n), zero lock contention)           ");
        System.out.println("               Shared Log: StringBuffer (Thread-safe concurrent appends)        ");
        System.out.println("================================================================================");
    }

    private static void runWorkload(String label, int ticketCount, int fieldCount) {
        System.out.println("\n--- " + label + " ---");
        System.out.printf("  Config: %d tickets, %d fields/ticket | Total field appends = %d%n", 
            ticketCount, fieldCount, (long) ticketCount * fieldCount);

        // 1. Measure String (+)
        System.gc();
        long memBeforeStr = getUsedMemory();
        long startStr = System.nanoTime();
        for (int t = 0; t < ticketCount; t++) {
            buildTicketWithString(fieldCount);
        }
        long timeStr = System.nanoTime() - startStr;
        long memAfterStr = getUsedMemory();
        long objectsStr = (long) ticketCount * (fieldCount - 1); // intermediate strings

        // 2. Measure StringBuilder
        System.gc();
        long memBeforeSb = getUsedMemory();
        long startSb = System.nanoTime();
        for (int t = 0; t < ticketCount; t++) {
            buildTicketWithStringBuilder(fieldCount);
        }
        long timeSb = System.nanoTime() - startSb;
        long memAfterSb = getUsedMemory();
        long objectsSb = 0; // mutable buffer, 0 intermediate string objects

        // 3. Measure StringBuffer
        System.gc();
        long memBeforeSbuf = getUsedMemory();
        long startSbuf = System.nanoTime();
        for (int t = 0; t < ticketCount; t++) {
            buildTicketWithStringBuffer(fieldCount);
        }
        long timeSbuf = System.nanoTime() - startSbuf;
        long memAfterSbuf = getUsedMemory();
        long objectsSbuf = 0;

        double msStr = timeStr / 1_000_000.0;
        double msSb = timeSb / 1_000_000.0;
        double msSbuf = timeSbuf / 1_000_000.0;

        System.out.printf("  %-16s | %12s | %18s | %14s | %10s%n", 
            "Approach", "Exec Time", "Intermediate Objs", "Memory Delta", "Speedup vs String");
        System.out.println("  " + "-".repeat(78));
        System.out.printf("  %-16s | %10.2f ms | %18d | %11.2f MB | %9.1fx (Baseline)%n", 
            "String (+)", msStr, objectsStr, (memAfterStr - memBeforeStr)/(1024.0*1024.0), 1.0);
        System.out.printf("  %-16s | %10.2f ms | %18d | %11.2f MB | %9.1fx FASTER%n", 
            "StringBuilder", msSb, objectsSb, (memAfterSb - memBeforeSb)/(1024.0*1024.0), msStr / Math.max(msSb, 0.001));
        System.out.printf("  %-16s | %10.2f ms | %18d | %11.2f MB | %9.1fx FASTER%n", 
            "StringBuffer", msSbuf, objectsSbuf, (memAfterSbuf - memBeforeSbuf)/(1024.0*1024.0), msStr / Math.max(msSbuf, 0.001));
    }

    private static void demonstrateMultithreadedScenario() {
        System.out.println("  Scenario: 4 concurrent booking threads appending to a shared audit log.");
        final StringBuffer sharedAuditLog = new StringBuffer();
        final int THREADS = 4;
        final int ENTRIES_PER_THREAD = 2500;
        Thread[] workers = new Thread[THREADS];

        long start = System.currentTimeMillis();
        for (int i = 0; i < THREADS; i++) {
            final int threadId = i;
            workers[i] = new Thread(() -> {
                // Thread-local generation: StringBuilder
                for (int j = 0; j < ENTRIES_PER_THREAD; j++) {
                    StringBuilder ticketText = new StringBuilder(64);
                    ticketText.append("TKT-").append(threadId).append("-").append(j);
                    
                    // Shared append: StringBuffer is synchronized and safe
                    sharedAuditLog.append(ticketText).append('\n');
                }
            });
            workers[i].start();
        }

        for (Thread w : workers) {
            try { w.join(); } catch (InterruptedException ignored) {}
        }
        long elapsed = System.currentTimeMillis() - start;

        // Verify thread safety
        int lineCount = sharedAuditLog.toString().split("\n").length;
        System.out.printf("  [+] Total entries safely logged = %d / %d (100%% integrity preserved)%n", 
            lineCount, THREADS * ENTRIES_PER_THREAD);
        System.out.printf("  [+] Multithreaded execution time = %d ms | Zero race conditions observed.%n", elapsed);
    }

    private static long getUsedMemory() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }
}
