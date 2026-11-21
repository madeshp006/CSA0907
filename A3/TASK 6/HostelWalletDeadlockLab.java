import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class HostelWalletDeadlockLab {
    // -------------------------------------------------------------------------
    // PART A: HOSTEL WALLET TRANSFER (DEADLOCK VS ORDERED LOCKING)
    // -------------------------------------------------------------------------
    static class WalletAccount {
        final int id;
        double balance;
        final ReentrantLock lock = new ReentrantLock();

        WalletAccount(int id, double balance) {
            this.id = id;
            this.balance = balance;
        }
    }

    // Deadlock-Prone Transfer (Asymmetric Lock Acquisition)
    public static void transferDeadlockProne(WalletAccount from, WalletAccount to, double amount) {
        synchronized (from) {
            try { Thread.sleep(20); } catch (InterruptedException ignored) {}
            synchronized (to) {
                from.balance -= amount;
                to.balance += amount;
            }
        }
    }

    // Deadlock-Free Transfer (Consistent Global Lock Ordering Rule)
    public static boolean transferDeadlockFree(WalletAccount acc1, WalletAccount acc2, double amount, boolean from1to2) {
        // Enforce total ordering based on immutable account IDs: Lower ID locked FIRST
        WalletAccount first = acc1.id < acc2.id ? acc1 : acc2;
        WalletAccount second = acc1.id < acc2.id ? acc2 : acc1;

        synchronized (first) {
            synchronized (second) {
                if (from1to2) {
                    if (acc1.balance >= amount) {
                        acc1.balance -= amount;
                        acc2.balance += amount;
                        return true;
                    }
                } else {
                    if (acc2.balance >= amount) {
                        acc2.balance -= amount;
                        acc1.balance += amount;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("================================================================================");
        System.out.println("   SIMATS ENGINEERING - CO3 ASSIGNMENT 3: TASK 6 (Q6) DEADLOCK & IPC LAB       ");
        System.out.println("   Student A: P. Madesh (192365007)  |  Student B: R. Vignesh (192365018)       ");
        System.out.println("   Faculty: Surya V  |  GitHub: https://github.com/madeshp006/CSA0907           ");
        System.out.println("================================================================================\n");

        System.out.println(">>> STUDENT B: DEADLOCK CONDITIONS & PREVENTION VIA LOCK ORDERING");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("  Coffman 4 Conditions for Deadlock in Wallet Transfers:");
        System.out.println("    1. Mutual Exclusion : Each wallet monitor can be held by only one thread.");
        System.out.println("    2. Hold and Wait    : Thread holds Lock(from) while waiting for Lock(to).");
        System.out.println("    3. No Preemption    : Lock cannot be forcibly revoked from a thread.");
        System.out.println("    4. Circular Wait    : Thread 1 (holds A, waits B) vs Thread 2 (holds B, waits A).");

        System.out.println("\n  Executing Deadlock-Free Lock-Ordering Rule (1,000 Concurrent Cross-Transfers):");
        WalletAccount w1 = new WalletAccount(101, 10000.0);
        WalletAccount w2 = new WalletAccount(102, 10000.0);

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 500; i++) transferDeadlockFree(w1, w2, 10.0, true);
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 500; i++) transferDeadlockFree(w1, w2, 10.0, false);
        });

        long start = System.currentTimeMillis();
        t1.start(); t2.start();
        t1.join(); t2.join();
        long elapsed = System.currentTimeMillis() - start;

        System.out.printf("  [+] SUCCESS: Completed 1,000 cross-transfers in %d ms without deadlock!%n", elapsed);
        System.out.printf("  [+] Total Conserved Balance: Rs %.2f (Exactly Rs 20,000.00)%n", (w1.balance + w2.balance));

        System.out.println("\n>>> STUDENT A: WHY wait() MUST BE CALLED IN A LOOP (SPURIOUS WAKEUPS)");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("  1. Spurious Wakeups: OS kernel signals can awaken a thread without notify() being called.");
        System.out.println("  2. Condition Invalidation: Between wakeup and lock re-acquisition, another consumer");
        System.out.println("     may have already extracted the item. An 'if' condition fails to re-verify state,");
        System.out.println("     leading to buffer underflow or ArrayIndexOutOfBoundsException!");
        System.out.println("  3. Difference between sleep() and wait():");
        System.out.println("     • sleep(ms) : Thread pauses but RETAINS intrinsic monitor lock (blocks other threads).");
        System.out.println("     • wait()    : Thread RELEASES monitor lock, permitting other threads to make progress.");

        System.out.println("\n================================================================================");
        System.out.println(" [JOINT CONCLUSION]: Lock ordering breaks Circular Wait condition.              ");
        System.out.println("                     wait() inside while loop guarantees invariant validity.   ");
        System.out.println("================================================================================");
    }
}
