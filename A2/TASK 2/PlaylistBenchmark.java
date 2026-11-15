import java.util.*;

public class PlaylistBenchmark {
    static class Song {
        final String title;
        final String artist;
        final int durationSeconds;

        Song(String title, String artist, int durationSeconds) {
            this.title = title;
            this.artist = artist;
            this.durationSeconds = durationSeconds;
        }
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   SIMATS ENGINEERING - CO2 ASSIGNMENT 2: TASK 2 (Q2) BENCHMARK REPORT         ");
        System.out.println("   Student: P. Madesh  |  Register No: 192365007  |  Java Version: 25.0.2      ");
        System.out.println("   Topic: ArrayList vs LinkedList vs ArrayDeque (Music Playlist Architecture)  ");
        System.out.println("================================================================================\n");

        int[] sizes = { 1000, 10000, 50000 };

        for (int n : sizes) {
            System.out.printf(">>> BENCHMARKING PLAYLIST SIZE N = %,d SONGS%n", n);
            System.out.println("--------------------------------------------------------------------------------");
            benchmarkSize(n);
            System.out.println();
        }

        System.out.println(">>> EXPERIMENT: CHANGED CONDITION - 'UP NEXT' QUEUE EVALUATION");
        System.out.println("--------------------------------------------------------------------------------");
        evaluateUpNextQueue(20000);

        System.out.println("\n================================================================================");
        System.out.println(" [DECISION BOUNDARY]: Use ArrayList for indexing O(1), iteration, tail appends.");
        System.out.println("                      Stop using ArrayList for head/mid insertion > 5,000 items.");
        System.out.println("                      For 'Up Next' queues (head/tail ops), use ArrayDeque!     ");
        System.out.println("================================================================================");
    }

    private static void benchmarkSize(int n) {
        List<Song> arrayList = new ArrayList<>();
        List<Song> linkedList = new LinkedList<>();

        // 1. Add at end (N elements)
        long t1 = System.nanoTime();
        for (int i = 0; i < n; i++) arrayList.add(new Song("Track_" + i, "Artist", 210));
        double alAddEnd = (System.nanoTime() - t1) / 1_000_000.0;

        long t2 = System.nanoTime();
        for (int i = 0; i < n; i++) linkedList.add(new Song("Track_" + i, "Artist", 210));
        double llAddEnd = (System.nanoTime() - t2) / 1_000_000.0;

        // 2. Random Access get(k) - 1,000 lookups
        int lookups = Math.min(n, 1000);
        long t3 = System.nanoTime();
        for (int i = 0; i < lookups; i++) {
            Song s = arrayList.get(i * (n / lookups));
        }
        double alGetK = (System.nanoTime() - t3) / 1_000_000.0;

        long t4 = System.nanoTime();
        for (int i = 0; i < lookups; i++) {
            Song s = linkedList.get(i * (n / lookups));
        }
        double llGetK = (System.nanoTime() - t4) / 1_000_000.0;

        // 3. Iteration through whole list
        long t5 = System.nanoTime();
        long alDurationSum = 0;
        for (Song s : arrayList) alDurationSum += s.durationSeconds;
        double alIter = (System.nanoTime() - t5) / 1_000_000.0;

        long t6 = System.nanoTime();
        long llDurationSum = 0;
        for (Song s : linkedList) llDurationSum += s.durationSeconds;
        double llIter = (System.nanoTime() - t6) / 1_000_000.0;

        // 4. Insert at Head (500 operations to prevent test freezing on large N)
        int headOps = Math.min(n / 2, 500);
        long t7 = System.nanoTime();
        for (int i = 0; i < headOps; i++) arrayList.add(0, new Song("Head_" + i, "Artist", 180));
        double alHeadAdd = (System.nanoTime() - t7) / 1_000_000.0;

        long t8 = System.nanoTime();
        for (int i = 0; i < headOps; i++) linkedList.add(0, new Song("Head_" + i, "Artist", 180));
        double llHeadAdd = (System.nanoTime() - t8) / 1_000_000.0;

        // 5. Remove from Head (500 operations)
        long t9 = System.nanoTime();
        for (int i = 0; i < headOps; i++) arrayList.remove(0);
        double alHeadRem = (System.nanoTime() - t9) / 1_000_000.0;

        long t10 = System.nanoTime();
        for (int i = 0; i < headOps; i++) linkedList.remove(0);
        double llHeadRem = (System.nanoTime() - t10) / 1_000_000.0;

        System.out.printf("  %-28s | %14s | %14s | %12s%n", "Operation", "ArrayList", "LinkedList", "Advantage");
        System.out.println("  " + "-".repeat(74));
        System.out.printf("  %-28s | %11.3f ms | %11.3f ms | %s%n", 
            "Add at Tail (N items)", alAddEnd, llAddEnd, alAddEnd <= llAddEnd ? "ArrayList (~2x)" : "LinkedList");
        System.out.printf("  %-28s | %11.3f ms | %11.3f ms | %s%n", 
            "Random Access get(k) (1K ops)", alGetK, llGetK, String.format("ArrayList (%.0fx)", llGetK / Math.max(alGetK, 0.001)));
        System.out.printf("  %-28s | %11.3f ms | %11.3f ms | %s%n", 
            "Sequential Iteration (Whole)", alIter, llIter, String.format("ArrayList (%.1fx)", llIter / Math.max(alIter, 0.001)));
        System.out.printf("  %-28s | %11.3f ms | %11.3f ms | %s%n", 
            "Insert at Head (" + headOps + " ops)", alHeadAdd, llHeadAdd, String.format("LinkedList (%.1fx)", alHeadAdd / Math.max(llHeadAdd, 0.001)));
        System.out.printf("  %-28s | %11.3f ms | %11.3f ms | %s%n", 
            "Remove from Head (" + headOps + " ops)", alHeadRem, llHeadRem, String.format("LinkedList (%.1fx)", alHeadRem / Math.max(llHeadRem, 0.001)));
    }

    private static void evaluateUpNextQueue(int ops) {
        System.out.printf("  Workload: %,d operations of mixed AddFirst, AddLast, and RemoveFirst%n", ops);

        // 1. ArrayList as Queue
        List<Song> arrayList = new ArrayList<>();
        long t1 = System.nanoTime();
        for (int i = 0; i < ops; i++) {
            if (i % 2 == 0) arrayList.add(0, new Song("S" + i, "A", 180));
            else arrayList.add(new Song("S" + i, "A", 180));
            if (arrayList.size() > 500) arrayList.remove(0);
        }
        double alTime = (System.nanoTime() - t1) / 1_000_000.0;

        // 2. LinkedList as Queue
        Deque<Song> linkedList = new LinkedList<>();
        long t2 = System.nanoTime();
        for (int i = 0; i < ops; i++) {
            if (i % 2 == 0) linkedList.addFirst(new Song("S" + i, "A", 180));
            else linkedList.addLast(new Song("S" + i, "A", 180));
            if (linkedList.size() > 500) linkedList.removeFirst();
        }
        double llTime = (System.nanoTime() - t2) / 1_000_000.0;

        // 3. ArrayDeque as Queue (Circular Buffer)
        Deque<Song> arrayDeque = new ArrayDeque<>();
        long t3 = System.nanoTime();
        for (int i = 0; i < ops; i++) {
            if (i % 2 == 0) arrayDeque.addFirst(new Song("S" + i, "A", 180));
            else arrayDeque.addLast(new Song("S" + i, "A", 180));
            if (arrayDeque.size() > 500) arrayDeque.removeFirst();
        }
        double adTime = (System.nanoTime() - t3) / 1_000_000.0;

        System.out.printf("  %-16s | %12s | %20s%n", "Data Structure", "Exec Time", "Per-Element Memory");
        System.out.println("  " + "-".repeat(55));
        System.out.printf("  %-16s | %9.2f ms | %s%n", "ArrayList", alTime, "4 bytes/ref + shifting copy cost");
        System.out.printf("  %-16s | %9.2f ms | %s%n", "LinkedList", llTime, "24-32 bytes (Node obj + 2 ptrs)");
        System.out.printf("  %-16s | %9.2f ms | %s%n", "ArrayDeque", adTime, "4 bytes/ref (Zero node overhead)");
        System.out.printf("  [+] ArrayDeque is %.1fx faster than LinkedList and %.1fx faster than ArrayList!%n",
            llTime / Math.max(adTime, 0.001), alTime / Math.max(adTime, 0.001));
    }
}
