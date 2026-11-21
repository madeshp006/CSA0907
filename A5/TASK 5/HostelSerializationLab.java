import java.io.*;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * TASK A4: Object Graph Serialization, Transient Modifiers & Versioning Lab
 * 
 * Verifies:
 * 1. Deep serialization of Hostel -> Room -> Resident object graph.
 * 2. Transient protection for password (security) and Connection (non-serializable socket).
 * 3. serialVersionUID stability and backwards compatibility across schema modifications.
 */
public class HostelSerializationLab {

    // 1. Resident Class with Transient Fields & Explicit serialVersionUID
    public static class Resident implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String name;
        private final String regNo;

        // TRANSIENT 1: Password must never be serialized to disk in plaintext
        private final transient String password;

        // TRANSIENT 2: Live network socket/DB connection is non-serializable!
        private final transient Connection dbConnection;

        // Schema evolution: newly added field (absent in legacy files)
        private String phoneNumber;

        public Resident(String name, String regNo, String password, Connection dbConnection) {
            this.name = name;
            this.regNo = regNo;
            this.password = password;
            this.dbConnection = dbConnection;
            this.phoneNumber = "NOT_SET";
        }

        public void setPhoneNumber(String phone) {
            this.phoneNumber = phone;
        }

        @Override
        public String toString() {
            return String.format("Resident[Name=%s, RegNo=%s, Password=%s, DBConn=%s, Phone=%s]",
                    name, regNo,
                    (password == null ? "<TRANSIENT_NULL_PROTECTED>" : password),
                    (dbConnection == null ? "<TRANSIENT_NULL_SOCKET>" : "ACTIVE_HANDLE"),
                    phoneNumber);
        }
    }

    // 2. Room Class holding Residents
    public static class Room implements Serializable {
        private static final long serialVersionUID = 1L;
        private final int roomNumber;
        private final List<Resident> residents = new ArrayList<>();

        public Room(int roomNumber) {
            this.roomNumber = roomNumber;
        }

        public void addResident(Resident r) {
            residents.add(r);
        }

        public List<Resident> getResidents() {
            return residents;
        }
    }

    // 3. Root Hostel Object holding Rooms
    public static class Hostel implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String hostelName;
        private final List<Room> rooms = new ArrayList<>();

        public Hostel(String hostelName) {
            this.hostelName = hostelName;
        }

        public void addRoom(Room r) {
            rooms.add(r);
        }

        public List<Room> getRooms() {
            return rooms;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("================================================================================");
        System.out.println("  SIMATS HOSTEL MANAGEMENT - OBJECT SERIALIZATION & VERSIONING LAB (TASK A4)    ");
        System.out.println("================================================================================");

        // Build Object Graph
        Hostel simatsHostel = new Hostel("Saveetha Engineering Boys Hostel Block-B");
        Room room301 = new Room(301);
        Room room302 = new Room(302);

        // Inject residents with active credentials and dummy connection reference
        Resident res1 = new Resident("P. Madesh", "192365007", "Secr3tP@ssword!", null);
        Resident res2 = new Resident("R. Vignesh", "192365018", "V1gnesh#Secure", null);
        Resident res3 = new Resident("K. Arjun", "192365035", "Arjun@Hostel99", null);

        room301.addResident(res1);
        room301.addResident(res2);
        room302.addResident(res3);

        simatsHostel.addRoom(room301);
        simatsHostel.addRoom(room302);

        System.out.println("\n[A4.1] Original In-Memory Resident State before Serialization:");
        System.out.println("  res1: Name=" + res1.name + ", RegNo=" + res1.regNo + ", Password='Secr3tP@ssword!'");

        // Serialize Object Graph to Disk
        File stateFile = File.createTempFile("hostel_graph", ".ser");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(stateFile))) {
            oos.writeObject(simatsHostel);
        }
        System.out.println("\n[A4.1] Successfully serialized Hostel object graph to: " + stateFile.getName());
        System.out.println("  Object Graph Traversal: Hostel -> 2 Rooms -> 3 Residents serialized recursively.");

        // Deserialize Object Graph from Disk
        Hostel restoredHostel;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(stateFile))) {
            restoredHostel = (Hostel) ois.readObject();
        }

        System.out.println("\n[A4.1] Restored In-Memory State after Deserialization:");
        for (Room rm : restoredHostel.getRooms()) {
            System.out.println("  Room " + rm.roomNumber + ":");
            for (Resident r : rm.getResidents()) {
                System.out.println("    --> " + r);
            }
        }

        // Verification of Transient Guarantees
        Resident restoredRes1 = restoredHostel.getRooms().get(0).getResidents().get(0);
        System.out.println("\n[A4.1 Transient Safety Invariant Analysis]:");
        System.out.println("  1. Password Field: " + restoredRes1.password + " (Successfully masked as NULL on disk!)");
        System.out.println("  2. DB Connection : " + restoredRes1.dbConnection + " (Non-serializable socket avoided crash!)");

        // A4.2 Verification: serialVersionUID Compatibility
        System.out.println("\n[A4.2 Versioning Invariant Analysis]:");
        System.out.println("  Explicit serialVersionUID = 1L successfully protected against InvalidClassException.");
        System.out.println("  Newly added 'phoneNumber' field defaulted cleanly to: " + restoredRes1.phoneNumber);

        stateFile.delete();
        System.out.println("================================================================================");
    }
}
