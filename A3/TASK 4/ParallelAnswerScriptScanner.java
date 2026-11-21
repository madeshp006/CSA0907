import java.util.*;

public class ParallelAnswerScriptScanner {
    // -------------------------------------------------------------------------
    // APPROACH 1: Implementing Runnable (Preferred / Decoupled Task)
    // -------------------------------------------------------------------------
    static class ScannerStationTask implements Runnable {
        private final int stationId;
        private final int batchSize;
        private int scannedCount = 0;

        ScannerStationTask(int stationId, int batchSize) {
            this.stationId = stationId;
            this.batchSize = batchSize;
        }

        @Override
        public void run() {
            String threadName = Thread.currentThread().getName();
            // Trace states
            for (int i = 1; i <= batchSize; i++) {
                try {
                    // Enter TIMED_WAITING state
                    Thread.sleep(15);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                scannedCount++;
            }
            System.out.printf("  [FINISHED] Station %d finished scanning %d scripts on thread '%s'%n",
                    stationId, scannedCount, threadName);
        }

        public int getScannedCount() { return scannedCount; }
    }

    // -------------------------------------------------------------------------
    // APPROACH 2: Extending Thread Class
    // -------------------------------------------------------------------------
    static class ScannerStationThread extends Thread {
        private final int stationId;
        private final int batchSize;
        private int scannedCount = 0;

        ScannerStationThread(int stationId, int batchSize) {
            super("ScannerThread-" + stationId);
            this.stationId = stationId;
            this.batchSize = batchSize;
        }

        @Override
        public void run() {
            for (int i = 1; i <= batchSize; i++) {
                try {
                    Thread.sleep(15);
                } catch (InterruptedException e) {
                    return;
                }
                scannedCount++;
            }
            System.out.printf("  [FINISHED] Extended Station %d finished scanning %d scripts on '%s'%n",
                    stationId, scannedCount, getName());
        }

        public int getScannedCount() { return scannedCount; }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("================================================================================");
        System.out.println("   SIMATS ENGINEERING - CO3 ASSIGNMENT 3: TASK 4 (Q4) THREAD LIFECYCLE LAB      ");
        System.out.println("   Student A: P. Madesh (192365007)  |  Student B: R. Vignesh (192365018)       ");
        System.out.println("   Faculty: Surya V  |  GitHub: https://github.com/madeshp006/CSA0907           ");
        System.out.println("================================================================================\n");

        System.out.println(">>> STUDENT A: PROOF THAT calling run() DOES NOT CREATE A NEW THREAD");
        System.out.println("--------------------------------------------------------------------------------");
        ScannerStationTask syncTask = new ScannerStationTask(99, 10);
        System.out.println("  Calling syncTask.run() directly from main:");
        syncTask.run(); // Synchronous direct call
        System.out.println("  [NOTICE]: Executed synchronously on thread 'main'! ZERO new threads were created.");

        System.out.println("\n>>> STUDENT A & B: 4 PARALLEL SCANNER STATIONS (start(), isAlive(), join())");
        System.out.println("--------------------------------------------------------------------------------");
        int NUM_STATIONS = 4;
        Thread[] stationThreads = new Thread[NUM_STATIONS];
        ScannerStationTask[] tasks = new ScannerStationTask[NUM_STATIONS];

        for (int i = 0; i < NUM_STATIONS; i++) {
            tasks[i] = new ScannerStationTask(i + 1, 25);
            stationThreads[i] = new Thread(tasks[i], "Station-Worker-" + (i + 1));
            // Student A Lifecycle: State is NEW
            System.out.printf("  Station %d: Thread created. State = %s%n", (i + 1), stationThreads[i].getState());
        }

        // Start stations (State becomes RUNNABLE)
        for (int i = 0; i < NUM_STATIONS; i++) {
            stationThreads[i].start();
        }

        // Check isAlive()
        System.out.println("\n  Verifying isAlive() while stations are actively processing:");
        for (int i = 0; i < NUM_STATIONS; i++) {
            System.out.printf("    Thread %s isAlive? -> %b (State: %s)%n",
                    stationThreads[i].getName(), stationThreads[i].isAlive(), stationThreads[i].getState());
        }

        // Student A: join() ensures main thread WAITS for all 4 stations before printing total
        System.out.println("\n  Invoking join() on all 4 station threads...");
        for (int i = 0; i < NUM_STATIONS; i++) {
            stationThreads[i].join();
        }

        int totalScripts = 0;
        for (ScannerStationTask t : tasks) totalScripts += t.getScannedCount();
        System.out.printf("\n  [+] ALL STATIONS JOINED: Total Answer Scripts Scanned = %d / 100%n", totalScripts);

        System.out.println("\n>>> STUDENT B: 10 MULTITHREADED RUNS TO RECORD FINISH ORDER VARIABILITY");
        System.out.println("--------------------------------------------------------------------------------");
        for (int run = 1; run <= 10; run++) {
            List<Integer> finishOrder = Collections.synchronizedList(new ArrayList<>());
            Thread[] batch = new Thread[4];
            for (int s = 0; s < 4; s++) {
                final int stationNum = s + 1;
                batch[s] = new Thread(() -> {
                    try { Thread.sleep((int)(Math.random() * 20)); } catch (InterruptedException ignored) {}
                    finishOrder.add(stationNum);
                });
                batch[s].start();
            }
            for (Thread t : batch) t.join();
            System.out.printf("  Run #%02d Finish Order: %s%n", run, finishOrder);
        }

        System.out.println("\n>>> STUDENT B: DAEMON THREAD TERMINATION EXPERIMENT");
        System.out.println("--------------------------------------------------------------------------------");
        Thread daemonScanner = new Thread(() -> {
            try {
                System.out.println("  Daemon background worker started...");
                Thread.sleep(5000);
                System.out.println("  Daemon finished! (SHOULD NEVER PRINT)");
            } catch (InterruptedException ignored) {}
        });
        daemonScanner.setDaemon(true);
        daemonScanner.start();
        System.out.println("  Daemon thread isDaemon? -> " + daemonScanner.isDaemon());
        System.out.println("  Main thread will NOT join daemon; main terminates and JVM halts daemon immediately.");

        System.out.println("\n================================================================================");
        System.out.println(" [JOINT CONCLUSION]: Multithreaded finish order is non-deterministic due to OS  ");
        System.out.println("                     time-slicing. Programs must coordinate state using join()! ");
        System.out.println("================================================================================");
    }
}
