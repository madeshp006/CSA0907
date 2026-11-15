import java.util.*;

public class AttendanceDeduplicationLab {
    // Flawed Student class: equals() overridden, hashCode() NOT overridden
    static class FlawedStudent {
        String regNo;
        String name;

        FlawedStudent(String regNo, String name) {
            this.regNo = regNo;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof FlawedStudent)) return false;
            return regNo.equals(((FlawedStudent) o).regNo);
        }
        // hashCode() inherited from Object (identity hash)
    }

    // Corrected Student class: consistent equals() and hashCode()
    static class CorrectStudent {
        private final String regNo;
        private final String name;

        CorrectStudent(String regNo, String name) {
            this.regNo = regNo;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CorrectStudent)) return false;
            return Objects.equals(regNo, ((CorrectStudent) o).regNo);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(regNo);
        }

        @Override
        public String toString() {
            return regNo + " (" + name + ")";
        }
    }

    // Mutable student for testing mutation pitfall
    static class MutableStudent {
        String regNo;
        String name;

        MutableStudent(String regNo, String name) {
            this.regNo = regNo;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MutableStudent)) return false;
            return Objects.equals(regNo, ((MutableStudent) o).regNo);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(regNo);
        }
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   SIMATS ENGINEERING - CO2 ASSIGNMENT 2: TASK 3 (Q3) EXPERIMENTAL LAB         ");
        System.out.println("   Student: P. Madesh  |  Register No: 192365007  |  Java Version: 25.0.2      ");
        System.out.println("   Topic: Engineering the hashCode-equals Contract in HashSet                  ");
        System.out.println("================================================================================\n");

        System.out.println(">>> EXPERIMENT 1: TEST WHERE HASHSET APPEARS TO WORK (IDENTICAL REFERENCE)");
        System.out.println("--------------------------------------------------------------------------------");
        Set<FlawedStudent> test1Set = new HashSet<>();
        FlawedStudent s1 = new FlawedStudent("192311001", "Arun");
        // Prediction: Size should be 1 because s1 == s1, same identity hash code
        test1Set.add(s1);
        test1Set.add(s1); // Same reference added again
        System.out.println("   Prediction: Set size = 1 (Reference identity s1 == s1 forces match)");
        System.out.println("   Observed  : Set size = " + test1Set.size() + " [Appears to work correctly!]");

        System.out.println("\n>>> EXPERIMENT 2: TEST DELIBERATELY EXPOSING DUPLICATE ENTRIES (FLAWED)");
        System.out.println("--------------------------------------------------------------------------------");
        Set<FlawedStudent> test2Set = new HashSet<>();
        FlawedStudent scanGate1 = new FlawedStudent("192311001", "Arun");
        FlawedStudent scanGate3 = new FlawedStudent("192311001", "Arun"); // Distinct object!
        test2Set.add(scanGate1);
        test2Set.add(scanGate3);
        System.out.println("   Prediction: Set size = 2 (Violates uniqueness! Different memory addresses)");
        System.out.println("   Observed  : Set size = " + test2Set.size());
        System.out.printf("   scanGate1 HashCode = %d | scanGate3 HashCode = %d (DIFFERENT BUCKETS!)%n",
            scanGate1.hashCode(), scanGate3.hashCode());
        System.out.println("   Explanation: HashSet checks bucket first. Since hashes differ, equals() is NEVER called!");

        System.out.println("\n>>> EXPERIMENT 3: MUTATION AFTER INSERTION PITFALL (ORPHANED ELEMENT)");
        System.out.println("--------------------------------------------------------------------------------");
        Set<MutableStudent> test3Set = new HashSet<>();
        MutableStudent mutStudent = new MutableStudent("192311099", "Karthik");
        test3Set.add(mutStudent);
        int originalHash = mutStudent.hashCode();
        System.out.println("   Added student: 192311099 | Bucket Hash = " + originalHash);
        System.out.println("   Before mutation: set.contains(mutStudent)? -> " + test3Set.contains(mutStudent));

        // Mutate regNo after insertion
        mutStudent.regNo = "192311777";
        int newHash = mutStudent.hashCode();
        System.out.println("   Mutated regNo to: 192311777 | New Bucket Hash = " + newHash);
        boolean containsAfter = test3Set.contains(mutStudent);
        System.out.println("   After mutation : set.contains(mutStudent)? -> " + containsAfter);
        System.out.println("   Set size: " + test3Set.size() + " (Element is trapped/orphaned in old bucket! Memory Leak!)");

        System.out.println("\n>>> EXPERIMENT 4: VERIFICATION OF THE CORRECTED STUDENT CLASS");
        System.out.println("--------------------------------------------------------------------------------");
        Set<CorrectStudent> correctedSet = new HashSet<>();
        correctedSet.add(new CorrectStudent("192311001", "Arun"));
        correctedSet.add(new CorrectStudent("192311002", "Divya"));
        correctedSet.add(new CorrectStudent("192311001", "Arun")); // Duplicate regNo
        System.out.println("   Added 3 student scans (1 duplicate regNo).");
        System.out.println("   Corrected Set size: " + correctedSet.size() + " (Unique students: 2) [PASS]");
        System.out.println("   Contains students: " + correctedSet);

        System.out.println("\n================================================================================");
        System.out.println(" [CONTRACT INVARIANT]: If a.equals(b) == true, then a.hashCode() == b.hashCode()");
        System.out.println("                       Keys in hash collections MUST be immutable (final)!      ");
        System.out.println("================================================================================");
    }
}
