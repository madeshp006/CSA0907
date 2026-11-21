import java.io.*;
import java.util.*;

// Custom checked exception for business overdraft
class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) {
        super(message);
    }
}

public class ATMWithdrawalSimulation {
    private static double balance = 10000.0; // Current account balance

    /**
     * Executes ATM cash withdrawal.
     * Demonstrates checked exception (FileNotFoundException, InsufficientFundsException),
     * unchecked exception (IllegalArgumentException, NumberFormatException),
     * multi-catch, and mandatory finally cleanup.
     */
    public static void withdraw(String cardFilePath, String accountStr, String amountStr) {
        System.out.printf("  [INITIATING TXN] Acc: %s | Amount: %s | CardFile: %s%n", accountStr, amountStr, cardFilePath);
        BufferedReader cardReader = null;
        boolean sessionOpened = false;

        try {
            // Failure Point 4: Checked IO Exception (Missing card-reader file)
            File f = new File(cardFilePath);
            if (!f.exists()) {
                throw new FileNotFoundException("ATM Card Reader hardware config file not found: " + cardFilePath);
            }
            cardReader = new BufferedReader(new FileReader(f));
            sessionOpened = true;

            // Failure Point 1: Unchecked NumberFormatException (Letters instead of digits)
            long accNo = Long.parseLong(accountStr.trim());

            // Parse amount
            double amount = Double.parseDouble(amountStr.trim());

            // Failure Point 2: Unchecked IllegalArgumentException (Negative or zero amount)
            if (amount <= 0) {
                throw new IllegalArgumentException("Withdrawal amount must be strictly positive: Rs " + amount);
            }

            // Failure Point 3: Checked InsufficientFundsException (Overdraft)
            if (amount > balance) {
                throw new InsufficientFundsException(String.format("Overdraft Denied! Requested Rs %.2f exceeds Balance Rs %.2f", amount, balance));
            }

            // Dispense Cash
            balance -= amount;
            System.out.printf("  [+] SUCCESS: Cash Dispensed: Rs %.2f | New Account Balance: Rs %.2f%n", amount, balance);

        } catch (FileNotFoundException e) {
            // Specific Checked Exception
            System.err.println("  [CAUGHT FileNotFoundException] Hardware Alert: " + e.getMessage());
        } catch (NumberFormatException e) {
            // Specific Unchecked Format Exception
            System.err.println("  [CAUGHT NumberFormatException] Input Error: Invalid numerical characters entered.");
        } catch (IllegalArgumentException | InsufficientFundsException e) {
            // Multi-catch for financial domain policy violations
            System.err.println("  [CAUGHT Multi-Catch " + e.getClass().getSimpleName() + "] Financial Policy: " + e.getMessage());
        } catch (Exception e) {
            // Top-level general fallback
            System.err.println("  [CAUGHT General Exception] Unexpected system error: " + e.getMessage());
        } finally {
            // Mandatory cleanup: guarantees card ejection and resource closure under ALL circumstances
            if (cardReader != null) {
                try { cardReader.close(); } catch (IOException ignored) {}
            }
            System.out.println("  [FINALLY GUARANTEE]: Card ejected safely. Security lock released. Session closed.");
        }
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   SIMATS ENGINEERING - CO3 ASSIGNMENT 3: TASK 1 (Q1) ATM FAILURE SIMULATION   ");
        System.out.println("   Student A: P. Madesh (192365007)  |  Student B: R. Vignesh (192365018)       ");
        System.out.println("   Faculty: Surya V  |  GitHub: https://github.com/madeshp006/CSA0907           ");
        System.out.println("================================================================================\n");

        // Create a temporary valid card reader file for testing
        File tempCard = new File("card_reader.cfg");
        try {
            tempCard.createNewFile();
        } catch (IOException ignored) {}

        System.out.println(">>> STUDENT B TEST SUITE: 5 DISTINCT EXECUTION PATHS");
        System.out.println("--------------------------------------------------------------------------------");

        System.out.println("\n--- Test Case 1: Valid Normal Withdrawal ---");
        withdraw("card_reader.cfg", "10023456789", "2000.00");

        System.out.println("\n--- Test Case 2: Non-Numeric Input (Letters instead of digits) ---");
        withdraw("card_reader.cfg", "10023456789", "FIVE_HUNDRED");

        System.out.println("\n--- Test Case 3: Negative Withdrawal Amount ---");
        withdraw("card_reader.cfg", "10023456789", "-500.00");

        System.out.println("\n--- Test Case 4: Overdraft Exceeding Current Balance ---");
        withdraw("card_reader.cfg", "10023456789", "50000.00");

        System.out.println("\n--- Test Case 5: Missing Card Reader Configuration File ---");
        withdraw("missing_slot.cfg", "10023456789", "1000.00");

        // Clean up temp file
        if (tempCard.exists()) tempCard.delete();

        System.out.println("\n================================================================================");
        System.out.println(" [STUDENT A & B CONCLUSION]: Catch blocks ordered narrowest -> broadest.       ");
        System.out.println("                            finally block executed in 100% of tested paths!    ");
        System.out.println("================================================================================");
    }
}
