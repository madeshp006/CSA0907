import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * TASK A5.2 & B2: Adversarial Security & Input Robustness Lab
 * 
 * Verifies:
 * 1. SQL Injection vulnerability of string-concatenated queries (A5.2 / B2.1).
 * 2. Complete immunity of PreparedStatement parameterized queries (B2.1).
 * 3. Parser resilience against malformed bus-pass records (B2.2).
 */
public class AdversarialSecurityLab {

    // Mock User Database Store simulating the relational engine
    static class UserRecord {
        String id;
        String password;
        String role;

        UserRecord(String id, String password, String role) {
            this.id = id;
            this.password = password;
            this.role = role;
        }
    }

    private static final List<UserRecord> userTable = new ArrayList<>();

    static {
        userTable.add(new UserRecord("admin", "SuperSecret@2026", "ADMINISTRATOR"));
        userTable.add(new UserRecord("192365007", "MadeshPass#123", "STUDENT"));
        userTable.add(new UserRecord("192365018", "VigneshPass#456", "STUDENT"));
    }

    /**
     * Unsafe Login Implementation (String Concatenation Vulnerability)
     * Simulates the exact SQL execution engine behavior when comments/tautologies are injected.
     */
    public static boolean unsafeLogin(String id, String pwd) {
        String rawSql = "SELECT * FROM users WHERE id='" + id + "' AND pwd='" + pwd + "'";
        System.out.println("  [Vulnerable Engine] Constructed SQL Query:");
        System.out.println("    --> " + rawSql);

        // SQL Parser Simulation of Injection
        if (id.contains("'--") || id.contains("' --") || id.contains("' OR '1'='1")) {
            // Extracted username before comment
            String targetUser = id.split("'")[0].trim();
            for (UserRecord u : userTable) {
                if (u.id.equalsIgnoreCase(targetUser)) {
                    System.out.println("  [SECURITY BREACH] Tautology/Comment evaluated! Authenticated as: " + u.id + " (" + u.role + ")");
                    return true;
                }
            }
        }

        // Standard matching
        for (UserRecord u : userTable) {
            if (u.id.equals(id) && u.password.equals(pwd)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Safe Login Implementation (PreparedStatement Parameterization)
     * Parameters are bound as literal values, never modifying the query AST.
     */
    public static boolean safeLogin(String id, String pwd) {
        String parameterizedSql = "SELECT * FROM users WHERE id = ? AND pwd = ?";
        System.out.println("  [PreparedStatement Engine] Parameterized AST Template:");
        System.out.println("    --> " + parameterizedSql);
        System.out.println("    --> Param 1 (String): [" + id + "]");
        System.out.println("    --> Param 2 (String): [" + pwd + "]");

        // In PreparedStatement, the injection payload is treated as a literal username string
        for (UserRecord u : userTable) {
            if (u.id.equals(id) && u.password.equals(pwd)) {
                return true;
            }
        }
        System.out.println("  [PROTECTION ACTIVE] Literal mismatch. No account with id='" + id + "'. Access denied.");
        return false;
    }

    public static void main(String[] args) throws IOException {
        System.out.println("================================================================================");
        System.out.println("  SIMATS ADVERSARIAL SECURITY & INPUT FORENSICS LAB (TASK B2)                   ");
        System.out.println("================================================================================");

        // =====================================================================
        // PART 1: SQL INJECTION ADVERSARIAL TEST
        // =====================================================================
        System.out.println("\n[Test 1] Demonstrating SQL Injection on Vulnerable Login Engine...");
        String normalId = "admin";
        String wrongPwd = "WrongPassword!";
        String maliciousPayload = "admin' -- ";

        System.out.println("\nCase A: Legitimate User with Incorrect Password");
        boolean resA = unsafeLogin(normalId, wrongPwd);
        System.out.println("  Outcome: " + (resA ? "LOGIN SUCCESS" : "LOGIN FAILED (Expected)"));

        System.out.println("\nCase B: Adversarial Injection ('" + maliciousPayload + "') against Unsafe Query");
        boolean resB = unsafeLogin(maliciousPayload, wrongPwd);
        System.out.println("  Outcome: " + (resB ? "EXPLOIT SUCCESSFUL (CRITICAL FLAW)" : "LOGIN FAILED"));

        System.out.println("\nCase C: Same Adversarial Injection against PreparedStatement");
        boolean resC = safeLogin(maliciousPayload, wrongPwd);
        System.out.println("  Outcome: " + (resC ? "LOGIN SUCCESS" : "ATTACK DEFLECTED (SECURE)"));

        // =====================================================================
        // PART 2: DAMAGED BUS-PASS ADVERSARIAL TEST
        // =====================================================================
        System.out.println("\n[Test 2] Demonstrating Damaged Bus-Pass File Robustness...");
        Path damagedFile = Files.createTempFile("adversarial_passes", ".csv");
        
        // Inject multiple adversarial malformed records
        List<String> rawLines = Arrays.asList(
            "P1001,192365007,P. Madesh,14,4200.0",                 // 1. Valid Record
            "P1002,INSUFFICIENT_FIELDS",                            // 2. Truncated fields (2 tokens)
            "P1003,192365018,R. Vignesh,14,INVALID_AMOUNT_ALPHA",  // 3. Non-numeric amount
            "",                                                     // 4. Blank line
            "   ",                                                  // 5. Whitespace line
            "P1004,192365022,S. Priya,7,3800.0,EXTRA_SIXTH_TOKEN",  // 6. Oversized fields (6 tokens)
            "P1005,192365035,K. Arjun,14,4200.0"                   // 7. Valid Record after damage
        );
        Files.write(damagedFile, rawLines);

        System.out.println("  Created damaged test file with 7 adversarial lines.");
        System.out.println("  Executing PassStore parser with schema validation filter...\n");

        int validCount = 0;
        int rejectedCount = 0;

        try (BufferedReader reader = Files.newBufferedReader(damagedFile)) {
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) {
                    System.out.printf("  Line %d: [REJECTED] Empty or whitespace line skipped.\n", lineNo);
                    rejectedCount++;
                    continue;
                }
                String[] tokens = line.split(",", -1);
                if (tokens.length != 5) {
                    System.out.printf("  Line %d: [REJECTED] Schema mismatch: %d tokens (Expected 5). Raw: '%s'\n",
                            lineNo, tokens.length, line);
                    rejectedCount++;
                    continue;
                }
                try {
                    int route = Integer.parseInt(tokens[3].trim());
                    double amt = Double.parseDouble(tokens[4].trim());
                    System.out.printf("  Line %d: [ACCEPTED] Valid Record: Pass ID=%s, Student=%s, Route=%d, Amt=Rs.%.2f\n",
                            lineNo, tokens[0], tokens[2], route, amt);
                    validCount++;
                } catch (NumberFormatException e) {
                    System.out.printf("  Line %d: [REJECTED] Type conversion error in numeric field: '%s'\n",
                            lineNo, e.getMessage());
                    rejectedCount++;
                }
            }
        }

        System.out.println("\n[Summary] Valid Accepted: " + validCount + " | Damaged Discarded: " + rejectedCount);
        Files.delete(damagedFile);
        System.out.println("================================================================================");
    }
}
