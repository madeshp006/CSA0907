import java.util.*;

/**
 * TASK A5.1 & B3.3: Wallet Monetary Transfer & ACID Transaction Rollback Lab
 * 
 * Verifies:
 * 1. Atomic debit and credit transfer between Hostel Wallet (Account 101) and Canteen Wallet (Account 202).
 * 2. Injected failure between updates demonstrating clean rollback where no money is lost or created.
 * 3. Contrast against non-transactional auto-commit execution where ₹500 vanishes permanently.
 */
public class WalletTransactionLab {

    static class Account {
        int id;
        String name;
        double balance;

        Account(int id, String name, double balance) {
            this.id = id;
            this.name = name;
            this.balance = balance;
        }

        Account copy() {
            return new Account(id, name, balance);
        }
    }

    // Ledger state
    private static Account hostelWallet = new Account(101, "Hostel Wallet (Madesh)", 2500.0);
    private static Account canteenWallet = new Account(202, "Campus Canteen Terminal", 10000.0);

    public static void executeTransferWithTransaction(double amount, boolean injectCrashBetweenUpdates) {
        System.out.println("  ------------------------------------------------------------------------------");
        System.out.printf("  [INITIAL STATE] %s: Rs. %.2f | %s: Rs. %.2f (Total Money: Rs. %.2f)\n",
                hostelWallet.name, hostelWallet.balance, canteenWallet.name, canteenWallet.balance,
                (hostelWallet.balance + canteenWallet.balance));

        // Savepoints for transaction simulation
        Account backupHostel = hostelWallet.copy();
        Account backupCanteen = canteenWallet.copy();

        System.out.println("  --> Step 1: con.setAutoCommit(false); Transaction Boundary Established.");
        try {
            // Update 1: Debit from Hostel Wallet
            if (hostelWallet.balance < amount) {
                throw new RuntimeException("InsufficientFundsException: Balance Rs." + hostelWallet.balance + " < Rs." + amount);
            }
            hostelWallet.balance -= amount;
            System.out.printf("  --> Step 2: Debited Rs. %.2f from %s (Uncommitted Balance: Rs. %.2f)\n",
                    amount, hostelWallet.name, hostelWallet.balance);

            // Injected Hardware/Network Power Crash Point!
            if (injectCrashBetweenUpdates) {
                System.out.println("  --> [SIMULATED CRASH] Server power failure / network socket dropped between updates!");
                throw new RuntimeException("HardwareCrashException: Power failure before credit update!");
            }

            // Update 2: Credit to Canteen Wallet
            canteenWallet.balance += amount;
            System.out.printf("  --> Step 3: Credited Rs. %.2f to %s (Uncommitted Balance: Rs. %.2f)\n",
                    amount, canteenWallet.name, canteenWallet.balance);

            // Commit
            System.out.println("  --> Step 4: con.commit(); Transaction successfully persisted.");

        } catch (Exception e) {
            System.out.println("  --> [EXCEPTION CAUGHT]: " + e.getMessage());
            System.out.println("  --> Step 5: con.rollback(); Rolling back all uncommitted updates...");
            // Rollback state
            hostelWallet = backupHostel;
            canteenWallet = backupCanteen;
            System.out.println("  --> ROLLBACK COMPLETE: Accounts restored to pristine pre-transaction state.");
        }

        System.out.printf("  [FINAL STATE]   %s: Rs. %.2f | %s: Rs. %.2f (Total Money: Rs. %.2f)\n",
                hostelWallet.name, hostelWallet.balance, canteenWallet.name, canteenWallet.balance,
                (hostelWallet.balance + canteenWallet.balance));
        System.out.println("  ------------------------------------------------------------------------------");
    }

    public static void executeTransferWithoutTransaction(double amount) {
        System.out.println("  ------------------------------------------------------------------------------");
        System.out.printf("  [INITIAL STATE] %s: Rs. %.2f | %s: Rs. %.2f (Total Money: Rs. %.2f)\n",
                hostelWallet.name, hostelWallet.balance, canteenWallet.name, canteenWallet.balance,
                (hostelWallet.balance + canteenWallet.balance));

        System.out.println("  --> [AUTO-COMMIT MODE ACTIVE] Every update is immediately permanent!");
        try {
            // Update 1: Debit
            hostelWallet.balance -= amount;
            System.out.printf("  --> Update 1 Auto-Committed: Debited Rs. %.2f from %s (New Balance: Rs. %.2f)\n",
                    amount, hostelWallet.name, hostelWallet.balance);

            // Injected Crash
            System.out.println("  --> [SIMULATED CRASH] Power failure before second update!");
            throw new RuntimeException("Fatal System Crash!");

            // Update 2: Never reached!

        } catch (Exception e) {
            System.out.println("  --> System crashed! No rollback mechanism exists in auto-commit mode!");
        }

        System.out.printf("  [FINAL STATE]   %s: Rs. %.2f | %s: Rs. %.2f (Total Money: Rs. %.2f)\n",
                hostelWallet.name, hostelWallet.balance, canteenWallet.name, canteenWallet.balance,
                (hostelWallet.balance + canteenWallet.balance));
        System.out.printf("  --> CATASTROPHIC INCONSISTENCY: Rs. %.2f has completely vanished from the system!\n", amount);
        System.out.println("  ------------------------------------------------------------------------------");
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("  SIMATS HOSTEL WALLET - ACID TRANSACTION & CRASH RECOVERY LAB (TASK A5.1)      ");
        System.out.println("================================================================================");

        System.out.println("\n[Scenario 1: Normal Transfer (Rs. 500.00) under Transaction Boundary]");
        executeTransferWithTransaction(500.0, false);

        System.out.println("\n[Scenario 2: Crash Half-Way under Transaction Boundary (ACID Protected)]");
        executeTransferWithTransaction(500.0, true);

        System.out.println("\n[Scenario 3: Crash Half-Way under Auto-Commit Mode (Vulnerable to Money Loss)]");
        executeTransferWithoutTransaction(500.0);

        System.out.println("\n[Conclusion] JDBC commit/rollback guarantees the Conservation of Money Invariant.");
        System.out.println("================================================================================");
    }
}
