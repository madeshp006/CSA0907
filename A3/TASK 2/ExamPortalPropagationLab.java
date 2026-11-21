import java.io.IOException;
import java.time.LocalTime;
import java.util.*;

// Base checked exception for the examination portal
abstract class ExamPortalException extends Exception {
    public ExamPortalException(String message) { super(message); }
    public ExamPortalException(String message, Throwable cause) { super(message, cause); }
}

// Subclass 1: Exam window closed
class ExamWindowClosedException extends ExamPortalException {
    public ExamWindowClosedException(String message) { super(message); }
}

// Subclass 2: Student not registered
class UnregisteredStudentException extends ExamPortalException {
    public UnregisteredStudentException(String message) { super(message); }
}

// Subclass 3: Storage failure (Chained)
class ExamStorageException extends ExamPortalException {
    public ExamStorageException(String message, Throwable cause) { super(message, cause); }
}

// Layer 3: Storage Layer
class ExamStorageLayer {
    public void persistFile(String studentId, String fileName, long fileSizeBytes) throws ExamStorageException {
        try {
            if (fileSizeBytes > 5 * 1024 * 1024) { // 5 MB limit
                throw new IOException("NFS Storage Cluster Disk Quota Exceeded (Max 5MB allowed)");
            }
            // Simulated success
        } catch (IOException e) {
            // Exception Chaining: Wrap low-level IO error into domain storage exception
            throw new ExamStorageException("Storage subsystem failed for submission: " + fileName, e);
        }
    }
}

// Layer 2: Service Layer
class ExamServiceLayer {
    private final ExamStorageLayer storage = new ExamStorageLayer();
    private final Set<String> registeredStudents = new HashSet<>(Arrays.asList("192365007", "192365018"));
    private final LocalTime deadline = LocalTime.of(17, 0); // 5:00 PM

    public void submitExam(String studentId, String fileName, long sizeBytes, LocalTime submitTime) 
            throws ExamWindowClosedException, UnregisteredStudentException, ExamStorageException {
        // Business Rule 1: Window check
        if (submitTime.isAfter(deadline)) {
            throw new ExamWindowClosedException("Submission Rejected: Exam portal closed at " + deadline + ". Attempted: " + submitTime);
        }
        // Business Rule 2: Registration check
        if (!registeredStudents.contains(studentId)) {
            throw new UnregisteredStudentException("Authorization Denied: Student ID " + studentId + " is not registered for course CSA09.");
        }
        // Delegate to storage layer
        storage.persistFile(studentId, fileName, sizeBytes);
    }
}

// Layer 1: Controller Layer (Presentation / UI Boundary)
class ExamControllerLayer {
    private final ExamServiceLayer service = new ExamServiceLayer();

    public void handlePostSubmission(String studentId, String fileName, long sizeBytes, LocalTime submitTime) {
        System.out.printf("  [HTTP POST /submit] Student: %s | File: %s (%,d bytes) | Time: %s%n",
                studentId, fileName, sizeBytes, submitTime);
        try {
            service.submitExam(studentId, fileName, sizeBytes, submitTime);
            System.out.println("  [HTTP 200 OK]: Submission successfully accepted and archived.");
        } catch (ExamWindowClosedException e) {
            System.err.println("  [HTTP 403 FORBIDDEN]: " + e.getMessage());
        } catch (UnregisteredStudentException e) {
            System.err.println("  [HTTP 401 UNAUTHORIZED]: " + e.getMessage());
        } catch (ExamStorageException e) {
            System.err.println("  [HTTP 500 INTERNAL ERROR]: " + e.getMessage());
            System.err.println("  [ROOT CAUSE IDENTIFIED]: " + e.getCause().getClass().getName() + " -> " + e.getCause().getMessage());
        }
    }
}

public class ExamPortalPropagationLab {
    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   SIMATS ENGINEERING - CO3 ASSIGNMENT 3: TASK 2 (Q2) EXCEPTION PROPAGATION    ");
        System.out.println("   Student A: P. Madesh (192365007)  |  Student B: R. Vignesh (192365018)       ");
        System.out.println("   Faculty: Surya V  |  GitHub: https://github.com/madeshp006/CSA0907           ");
        System.out.println("================================================================================\n");

        ExamControllerLayer controller = new ExamControllerLayer();

        System.out.println(">>> STUDENT B EVIDENCE: 4 MULTI-LAYER PROPAGATION SCENARIOS");
        System.out.println("--------------------------------------------------------------------------------");

        System.out.println("\n--- Scenario 1: Valid On-Time Registered Submission ---");
        controller.handlePostSubmission("192365007", "P_Madesh_Answers.pdf", 1024 * 1024, LocalTime.of(16, 30));

        System.out.println("\n--- Scenario 2: Late Submission (Exam Window Closed) ---");
        controller.handlePostSubmission("192365007", "P_Madesh_Late.pdf", 1024 * 1024, LocalTime.of(17, 15));

        System.out.println("\n--- Scenario 3: Unregistered Student ID ---");
        controller.handlePostSubmission("192399999", "Unknown_Student.pdf", 1024 * 1024, LocalTime.of(16, 45));

        System.out.println("\n--- Scenario 4: Storage Quota Exceeded (Chained Exception) ---");
        controller.handlePostSubmission("192365018", "R_Vignesh_Heavy.pdf", 8 * 1024 * 1024, LocalTime.of(16, 50));

        System.out.println("\n>>> EXCEPTION CHAINING DEMONSTRATION & ROOT CAUSE INSPECTION:");
        System.out.println("--------------------------------------------------------------------------------");
        try {
            new ExamStorageLayer().persistFile("192365007", "test_quota.dat", 10 * 1024 * 1024);
        } catch (ExamStorageException chained) {
            System.out.println("  Top-Level Exception : " + chained.getMessage());
            System.out.println("  Nested Cause Object : " + chained.getCause());
            System.out.println("  Root Cause Message  : " + chained.getCause().getMessage());
            System.out.println("  [VERIFICATION]: Original low-level IOException preserved without leaking to student UI.");
        }

        System.out.println("\n================================================================================");
        System.out.println(" [JOINT CONCLUSION]: Storage layer throws SubmissionStorageException to preserve");
        System.out.println("                     architectural abstraction and provide clean user feedback. ");
        System.out.println("================================================================================");
    }
}
