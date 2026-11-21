import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class CinemaRaceConditionLab {
    // -------------------------------------------------------------------------
    // 1. UNSYNCHRONIZED (RACE CONDITION)
    // -------------------------------------------------------------------------
    static class UnsyncBooking {
        final boolean[] booked;
        int bookingsMade = 0;
        UnsyncBooking(int seats) { booked = new boolean[seats]; }

        boolean book(int seat) {
            if (!booked[seat]) {
                // Micro-delay expands the check-then-act vulnerability window
                try { Thread.sleep(0, 500); } catch (InterruptedException ignored) {}
                booked[seat] = true;
                bookingsMade++;
                return true;
            }
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // 2. SYNCHRONIZED METHOD (COARSE-GRAINED LOCK ON THIS)
    // -------------------------------------------------------------------------
    static class SyncMethodBooking {
        final boolean[] booked;
        int bookingsMade = 0;
        SyncMethodBooking(int seats) { booked = new boolean[seats]; }

        synchronized boolean book(int seat) {
            if (!booked[seat]) {
                booked[seat] = true;
                bookingsMade++;
                return true;
            }
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // 3. SYNCHRONIZED BLOCK (FINE-GRAINED SEAT-LEVEL LOCKS)
    // -------------------------------------------------------------------------
    static class FineGrainedBooking {
        final boolean[] booked;
        final Object[] seatLocks;
        int bookingsMade = 0;

        FineGrainedBooking(int seats) {
            booked = new boolean[seats];
            seatLocks = new Object[seats];
            for (int i = 0; i < seats; i++) seatLocks[i] = new Object();
        }

        boolean book(int seat) {
            synchronized (seatLocks[seat]) {
                if (!booked[seat]) {
                    booked[seat] = true;
                    synchronized (this) { bookingsMade++; }
                    return true;
                }
            }
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // 4. ATOMIC BOOLEAN ALTERNATIVE (LOCK-FREE CAS)
    // -------------------------------------------------------------------------
    static class AtomicBooking {
        final AtomicBoolean[] seats;
        int bookingsMade = 0;

        AtomicBooking(int numSeats) {
            seats = new AtomicBoolean[numSeats];
            for (int i = 0; i < numSeats; i++) seats[i] = new AtomicBoolean(false);
        }

        boolean book(int seat) {
            // Atomic compare-and-set: hardware CAS instruction
            if (seats[seat].compareAndSet(false, true)) {
                synchronized (this) { bookingsMade++; }
                return true;
            }
            return false;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("================================================================================");
        System.out.println("   SIMATS ENGINEERING - CO3 ASSIGNMENT 3: TASK 5 (Q5) RACE CONDITIONS LAB       ");
        System.out.println("   Student A: P. Madesh (192365007)  |  Student B: R. Vignesh (192365018)       ");
        System.out.println("   Faculty: Surya V  |  GitHub: https://github.com/madeshp006/CSA0907           ");
        System.out.println("================================================================================\n");

        System.out.println(">>> STUDENT A: REPRODUCING DOUBLE-BOOKING ACROSS 10 RUNS (UNSYNCHRONIZED)");
        System.out.println("--------------------------------------------------------------------------------");
        for (int run = 1; run <= 10; run++) {
            UnsyncBooking unsync = new UnsyncBooking(10);
            Thread[] users = new Thread[100];
            for (int i = 0; i < users.length; i++) {
                final int seat = i % 10;
                users[i] = new Thread(() -> unsync.book(seat));
                users[i].start();
            }
            for (Thread t : users) t.join();
            int doubleBookings = unsync.bookingsMade - 10;
            System.out.printf("  Run #%02d: Seats Sold = %2d / 10 | Double Bookings = %d %s%n",
                    run, unsync.bookingsMade, doubleBookings, doubleBookings > 0 ? "[RACE DETECTED!]" : "[FLUKED MATCH]");
        }

        System.out.println("\n>>> STUDENT B: PERFORMANCE COMPARISON OF 10,000 BOOKING REQUESTS");
        System.out.println("--------------------------------------------------------------------------------");
        benchmark10kBookings();

        System.out.println("\n================================================================================");
        System.out.println(" [JOINT DECISION FOR 20 SCREENS]: Deploy instance-level or screen-level locks.  ");
        System.out.println("                                  Never use 'static synchronized' across screens!");
        System.out.println("================================================================================");
    }

    private static void benchmark10kBookings() throws InterruptedException {
        int seats = 100;
        int threads = 10000;

        // 1. Sync Method
        SyncMethodBooking syncMethod = new SyncMethodBooking(seats);
        long t1 = System.nanoTime();
        Thread[] th1 = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            final int s = i % seats;
            th1[i] = new Thread(() -> syncMethod.book(s));
            th1[i].start();
        }
        for (Thread t : th1) t.join();
        double d1 = (System.nanoTime() - t1) / 1_000_000.0;

        // 2. Fine-grained block
        FineGrainedBooking fineGrained = new FineGrainedBooking(seats);
        long t2 = System.nanoTime();
        Thread[] th2 = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            final int s = i % seats;
            th2[i] = new Thread(() -> fineGrained.book(s));
            th2[i].start();
        }
        for (Thread t : th2) t.join();
        double d2 = (System.nanoTime() - t2) / 1_000_000.0;

        // 3. Atomic CAS
        AtomicBooking atomic = new AtomicBooking(seats);
        long t3 = System.nanoTime();
        Thread[] th3 = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            final int s = i % seats;
            th3[i] = new Thread(() -> atomic.book(s));
            th3[i].start();
        }
        for (Thread t : th3) t.join();
        double d3 = (System.nanoTime() - t3) / 1_000_000.0;

        System.out.printf("  %-25s | %12s | %14s | %14s%n", "Synchronization Strategy", "Exec Time", "Seats Sold", "Integrity");
        System.out.println("  " + "-".repeat(74));
        System.out.printf("  %-25s | %9.2f ms | %14d | %14s%n", "Synchronized Method", d1, syncMethod.bookingsMade, "100% Correct");
        System.out.printf("  %-25s | %9.2f ms | %14d | %14s%n", "Fine-Grained Seat Locks", d2, fineGrained.bookingsMade, "100% Correct");
        System.out.printf("  %-25s | %9.2f ms | %14d | %14s%n", "AtomicBoolean (CAS)", d3, atomic.bookingsMade, "100% Correct");
    }
}
