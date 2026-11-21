import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Service Layer for Fee Payment System
 * Encapsulates input validation, database persistence, and payment transactions.
 * Completely decoupled from presentation/GUI components.
 */
public class FeeService {
    private final String dbUrl;

    public FeeService() {
        this("jdbc:sqlite:fees.db");
        initDatabase();
    }

    public FeeService(String dbUrl) {
        this.dbUrl = dbUrl;
        initDatabase();
    }

    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS fees (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "reg_no TEXT NOT NULL, " +
                         "amount REAL NOT NULL, " +
                         "tx_id TEXT NOT NULL, " +
                         "paid_at TEXT NOT NULL)");
        } catch (SQLException e) {
            System.err.println("[FeeService] DB Init Notice: " + e.getMessage());
        }
    }

    public static class ValidationResult {
        public final boolean valid;
        public final String errorMessage;
        public final double parsedAmount;

        public ValidationResult(boolean valid, String errorMessage, double parsedAmount) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.parsedAmount = parsedAmount;
        }

        public static ValidationResult ok(double amount) {
            return new ValidationResult(true, null, amount);
        }

        public static ValidationResult fail(String message) {
            return new ValidationResult(false, message, 0.0);
        }
    }

    public static class PaymentReceipt {
        public final String regNo;
        public final double amount;
        public final String transactionId;
        public final String timestamp;

        public PaymentReceipt(String regNo, double amount, String transactionId, String timestamp) {
            this.regNo = regNo;
            this.amount = amount;
            this.transactionId = transactionId;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return String.format(
                "========================================\n" +
                "         SIMATS FEE RECEIPT             \n" +
                "========================================\n" +
                "Register No    : %s\n" +
                "Amount Paid    : Rs. %.2f\n" +
                "Transaction ID : %s\n" +
                "Date & Time    : %s\n" +
                "Status         : SUCCESS / CONFIRMED\n" +
                "========================================",
                regNo, amount, transactionId, timestamp
            );
        }
    }

    public ValidationResult validate(String regNo, String amountStr) {
        if (regNo == null || regNo.trim().isEmpty()) {
            return ValidationResult.fail("Register number cannot be empty.");
        }
        if (!regNo.trim().matches("^[0-9A-Za-z]+$")) {
            return ValidationResult.fail("Register number must be alphanumeric.");
        }
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return ValidationResult.fail("Amount field cannot be empty.");
        }
        try {
            double parsed = Double.parseDouble(amountStr.trim());
            if (parsed <= 0) {
                return ValidationResult.fail("Amount must be strictly greater than 0.");
            }
            if (parsed > 500000) {
                return ValidationResult.fail("Amount exceeds maximum single transaction limit (Rs. 5,00,000).");
            }
            return ValidationResult.ok(parsed);
        } catch (NumberFormatException e) {
            return ValidationResult.fail("Amount must be a valid numeric quantity.");
        }
    }

    public PaymentReceipt processPayment(String regNo, double amount) throws SQLException, InterruptedException {
        // Simulate bank confirmation latency (executed on worker thread, never blocking EDT)
        Thread.sleep(1500);

        String txId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Secure parameterized SQL to eliminate SQL Injection (CWE-89)
        String sql = "INSERT INTO fees (reg_no, amount, tx_id, paid_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, regNo.trim());
            ps.setDouble(2, amount);
            ps.setString(3, txId);
            ps.setString(4, timestamp);
            ps.executeUpdate();
        }

        return new PaymentReceipt(regNo.trim(), amount, txId, timestamp);
    }
}
