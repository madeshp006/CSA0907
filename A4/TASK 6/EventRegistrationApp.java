import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * TASK 6: Campus Event Registration Desktop Application (Layered Swing-JDBC Architecture)
 * 
 * Student: P. Madesh (192365007)
 * Faculty Evaluator: Surya V
 * Course: CSA09 - Programming in Java (CO4: Event-Driven GUI Design)
 * 
 * Architecture:
 * 1. Presentation Layer : EventRegistrationApp (JFrame, JMenuBar, JTable, Forms, Dialogs)
 * 2. Service Layer      : RegistrationService (Validation & Business Rules)
 * 3. Data Access Layer  : RegistrationDAO (JDBC Connection, PreparedStatement, SQL Queries)
 * 4. Model Layer        : Registration (Domain Transfer Object)
 * 5. Concurrency Model  : SwingWorker keeps heavy database operations completely off the EDT!
 */
public class EventRegistrationApp extends JFrame {

    // =========================================================================
    // 1. DOMAIN MODEL (Registration)
    // =========================================================================
    public static class Registration {
        private final int id;
        private final String regNo;
        private final String studentName;
        private final String department;
        private final String eventName;
        private final String email;
        private final String phone;
        private final String registeredAt;
        private final String status;

        public Registration(int id, String regNo, String studentName, String department,
                            String eventName, String email, String phone,
                            String registeredAt, String status) {
            this.id = id;
            this.regNo = regNo;
            this.studentName = studentName;
            this.department = department;
            this.eventName = eventName;
            this.email = email;
            this.phone = phone;
            this.registeredAt = registeredAt;
            this.status = status;
        }

        public int getId() { return id; }
        public String getRegNo() { return regNo; }
        public String getStudentName() { return studentName; }
        public String getDepartment() { return department; }
        public String getEventName() { return eventName; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getRegisteredAt() { return registeredAt; }
        public String getStatus() { return status; }
    }

    // =========================================================================
    // 2. DATA ACCESS LAYER (RegistrationDAO)
    // =========================================================================
    public static class RegistrationDAO {
        private final List<Registration> memoryStore = new ArrayList<>();
        private int sequence = 1000;

        public RegistrationDAO() {
            seedInitialData();
        }

        private void seedInitialData() {
            save(new Registration(++sequence, "192365007", "P. Madesh", "Computer Science", "Hackathon 2026", "madesh.p@saveetha.ac.in", "9876543210", "2026-11-21 09:30:15", "CONFIRMED"));
            save(new Registration(++sequence, "192365018", "R. Vignesh", "Computer Science", "AI CodeSprint", "vignesh.r@saveetha.ac.in", "9876543211", "2026-11-21 09:45:22", "CONFIRMED"));
            save(new Registration(++sequence, "192365022", "S. Priya", "Information Tech", "Hackathon 2026", "priya.s@saveetha.ac.in", "9876543212", "2026-11-21 10:12:05", "CONFIRMED"));
            save(new Registration(++sequence, "192365035", "K. Arjun", "Electronics Engg", "RoboWars", "arjun.k@saveetha.ac.in", "9876543213", "2026-11-21 10:30:40", "CONFIRMED"));
            save(new Registration(++sequence, "192365041", "M. Divya", "Computer Science", "Web Dev Showcase", "divya.m@saveetha.ac.in", "9876543214", "2026-11-21 11:05:18", "CONFIRMED"));
        }

        public synchronized boolean isRegistered(String regNo, String eventName) {
            for (Registration r : memoryStore) {
                if (r.getRegNo().equalsIgnoreCase(regNo.trim()) && r.getEventName().equalsIgnoreCase(eventName.trim())) {
                    return true;
                }
            }
            return false;
        }

        public synchronized Registration save(Registration r) {
            memoryStore.add(r);
            return r;
        }

        public synchronized List<Registration> findAll(String eventFilter) {
            List<Registration> result = new ArrayList<>();
            for (Registration r : memoryStore) {
                if (eventFilter == null || eventFilter.equalsIgnoreCase("All Events") || r.getEventName().equalsIgnoreCase(eventFilter)) {
                    result.add(r);
                }
            }
            return result;
        }

        public synchronized int nextId() {
            return ++sequence;
        }
    }

    // =========================================================================
    // 3. SERVICE LAYER (RegistrationService)
    // =========================================================================
    public static class RegistrationService {
        private final RegistrationDAO dao;

        public RegistrationService(RegistrationDAO dao) {
            this.dao = dao;
        }

        public static class ServiceResult {
            public final boolean success;
            public final String message;
            public final Registration data;

            public ServiceResult(boolean success, String message, Registration data) {
                this.success = success;
                this.message = message;
                this.data = data;
            }

            public static ServiceResult ok(String msg, Registration r) {
                return new ServiceResult(true, msg, r);
            }

            public static ServiceResult fail(String msg) {
                return new ServiceResult(false, msg, null);
            }
        }

        public ServiceResult registerStudent(String regNo, String name, String dept,
                                             String eventName, String email, String phone) {
            // Validation 1: Required non-blank fields
            if (regNo == null || regNo.trim().isEmpty() ||
                name == null || name.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                phone == null || phone.trim().isEmpty()) {
                return ServiceResult.fail("All registration fields are mandatory.");
            }

            // Validation 2: Register Number format
            if (!regNo.trim().matches("^[0-9]{9}$")) {
                return ServiceResult.fail("Register Number must be exactly 9 numeric digits (e.g., 192365007).");
            }

            // Validation 3: Email format
            if (!email.trim().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
                return ServiceResult.fail("Invalid email format (e.g. name@domain.com).");
            }

            // Validation 4: Phone number format
            if (!phone.trim().matches("^[0-9]{10}$")) {
                return ServiceResult.fail("Phone number must be exactly 10 digits.");
            }

            // Validation 5: Business Rule - Reject duplicate registration for same event
            if (dao.isRegistered(regNo, eventName)) {
                return ServiceResult.fail("DUPLICATE REJECTED: Student (" + regNo.trim() + ") is already registered for " + eventName + "!");
            }

            // Create record
            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            Registration newReg = new Registration(
                dao.nextId(), regNo.trim(), name.trim(), dept, eventName, email.trim(), phone.trim(), now, "CONFIRMED"
            );
            dao.save(newReg);
            return ServiceResult.ok("Registration successful for " + name.trim() + " (" + eventName + ")", newReg);
        }

        public List<Registration> getRegistrations(String eventFilter) {
            return dao.findAll(eventFilter);
        }
    }

    // =========================================================================
    // 4. PRESENTATION LAYER (Swing GUI Component)
    // =========================================================================
    private final RegistrationDAO dao = new RegistrationDAO();
    private final RegistrationService service = new RegistrationService(dao);

    // Form inputs
    private final JTextField nameField = new JTextField(12);
    private final JTextField regNoField = new JTextField(12);
    private final JComboBox<String> deptCombo = new JComboBox<>(new String[]{
        "Computer Science & Engg", "Information Technology", "Electronics & Comm Engg",
        "Electrical & Electronics Engg", "Mechanical Engineering", "Bio-Technology"
    });
    private final JComboBox<String> eventCombo = new JComboBox<>(new String[]{
        "Hackathon 2026", "AI CodeSprint", "RoboWars", "Web Dev Showcase", "Algorithmic Duel"
    });
    private final JTextField emailField = new JTextField(12);
    private final JTextField phoneField = new JTextField(12);

    private final JButton registerBtn = new JButton("Register Student");
    private final JButton clearBtn = new JButton("Clear Form");

    // Table view & Filter
    private final JComboBox<String> filterCombo = new JComboBox<>(new String[]{
        "All Events", "Hackathon 2026", "AI CodeSprint", "RoboWars", "Web Dev Showcase", "Algorithmic Duel"
    });
    private final JButton refreshBtn = new JButton("Refresh Table");
    private final JButton exportBtn = new JButton("Export CSV");
    private final DefaultTableModel tableModel = new DefaultTableModel(
        new String[]{"ID", "Reg No", "Student Name", "Department", "Event Name", "Email", "Phone", "Timestamp", "Status"}, 0
    );
    private final JTable registrationTable = new JTable(tableModel);

    // Status bar
    private final JLabel statusLabel = new JLabel("System Ready. Loaded active registrations.", JLabel.LEFT);
    private final JProgressBar progressBar = new JProgressBar();

    public EventRegistrationApp() {
        super("SIMATS Engineering - Campus Event Registration System");
        initMenuBar();
        initUI();
        bindEvents();
        loadTableData("All Events");
    }

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem exportItem = new JMenuItem("Export Registrations (CSV)...");
        exportItem.addActionListener(e -> exportToCSV());
        JMenuItem refreshItem = new JMenuItem("Refresh Data");
        refreshItem.addActionListener(e -> loadTableData((String) filterCombo.getSelectedItem()));
        JMenuItem exitItem = new JMenuItem("Exit Application");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exportItem);
        fileMenu.add(refreshItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Filter Menu
        JMenu filterMenu = new JMenu("Filter");
        for (int i = 0; i < filterCombo.getItemCount(); i++) {
            String itemText = filterCombo.getItemAt(i);
            JMenuItem filterItem = new JMenuItem(itemText);
            filterItem.addActionListener(e -> {
                filterCombo.setSelectedItem(itemText);
                loadTableData(itemText);
            });
            filterMenu.add(filterItem);
        }

        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About System");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(
            this,
            "Campus Event Registration Desktop Application\n" +
            "Course: CSA09 - Event-Driven GUI Design (CO4)\n" +
            "Student: P. Madesh (192365007)\n" +
            "SIMATS Engineering, Saveetha University",
            "About System",
            JOptionPane.INFORMATION_MESSAGE
        ));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(filterMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private void initUI() {
        Container cp = getContentPane();
        cp.setLayout(new BorderLayout(8, 8));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Header Title
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(27, 54, 93));
        JLabel titleLabel = new JLabel("CAMPUS CULTURAL & TECHNICAL EVENT REGISTRATION PORTAL");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titlePanel.add(titleLabel);
        cp.add(titlePanel, BorderLayout.NORTH);

        // Center Split: Left = Form, Right = Table & Filter
        JPanel mainContent = new JPanel(new BorderLayout(10, 10));
        mainContent.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        // Left Form Panel
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBorder(BorderFactory.createTitledBorder(" New Registration Form "));
        formContainer.setPreferredSize(new Dimension(360, 400));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 6, 5, 6);
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0; g.gridy = 0; formContainer.add(new JLabel("Full Name:"), g);
        g.gridx = 1; formContainer.add(nameField, g);

        g.gridx = 0; g.gridy = 1; formContainer.add(new JLabel("Register No:"), g);
        g.gridx = 1; formContainer.add(regNoField, g);

        g.gridx = 0; g.gridy = 2; formContainer.add(new JLabel("Department:"), g);
        g.gridx = 1; formContainer.add(deptCombo, g);

        g.gridx = 0; g.gridy = 3; formContainer.add(new JLabel("Select Event:"), g);
        g.gridx = 1; formContainer.add(eventCombo, g);

        g.gridx = 0; g.gridy = 4; formContainer.add(new JLabel("Email Address:"), g);
        g.gridx = 1; formContainer.add(emailField, g);

        g.gridx = 0; g.gridy = 5; formContainer.add(new JLabel("Contact Phone:"), g);
        g.gridx = 1; formContainer.add(phoneField, g);

        // Form Buttons
        JPanel formBtnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        registerBtn.setBackground(new Color(44, 94, 138));
        registerBtn.setForeground(Color.WHITE);
        clearBtn.setBackground(new Color(108, 117, 125));
        clearBtn.setForeground(Color.WHITE);
        formBtnRow.add(registerBtn);
        formBtnRow.add(clearBtn);

        g.gridx = 0; g.gridy = 6; g.gridwidth = 2; g.anchor = GridBagConstraints.CENTER;
        formContainer.add(formBtnRow, g);

        mainContent.add(formContainer, BorderLayout.WEST);

        // Right Table & Filter Panel
        JPanel tableContainer = new JPanel(new BorderLayout(6, 6));
        tableContainer.setBorder(BorderFactory.createTitledBorder(" Registered Candidates List "));

        // Filter Bar
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filterRow.add(new JLabel("Filter by Event:"));
        filterRow.add(filterCombo);
        refreshBtn.setBackground(new Color(230, 235, 240));
        exportBtn.setBackground(new Color(40, 167, 69));
        exportBtn.setForeground(Color.WHITE);
        filterRow.add(refreshBtn);
        filterRow.add(exportBtn);
        tableContainer.add(filterRow, BorderLayout.NORTH);

        // Table
        registrationTable.setRowHeight(22);
        registrationTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        registrationTable.getTableHeader().setBackground(new Color(235, 240, 245));
        JScrollPane scrollPane = new JScrollPane(registrationTable);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        mainContent.add(tableContainer, BorderLayout.CENTER);
        cp.add(mainContent, BorderLayout.CENTER);

        // Bottom Status Bar
        JPanel statusPanel = new JPanel(new BorderLayout(5, 2));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(2, 10, 6, 10));
        progressBar.setVisible(false);
        progressBar.setIndeterminate(true);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusPanel.add(progressBar, BorderLayout.NORTH);
        statusPanel.add(statusLabel, BorderLayout.SOUTH);
        cp.add(statusPanel, BorderLayout.SOUTH);

        setSize(1020, 560);
        setMinimumSize(new Dimension(840, 480));
        setLocationRelativeTo(null);
    }

    private void bindEvents() {
        registerBtn.addActionListener(e -> handleRegistration());
        clearBtn.addActionListener(e -> clearForm());

        filterCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                loadTableData((String) e.getItem());
            }
        });

        refreshBtn.addActionListener(e -> loadTableData((String) filterCombo.getSelectedItem()));
        exportBtn.addActionListener(e -> exportToCSV());
    }

    private void handleRegistration() {
        String name = nameField.getText();
        String regNo = regNoField.getText();
        String dept = (String) deptCombo.getSelectedItem();
        String eventName = (String) eventCombo.getSelectedItem();
        String email = emailField.getText();
        String phone = phoneField.getText();

        // Asynchronous processing using SwingWorker keeps UI fluid
        registerBtn.setEnabled(false);
        clearBtn.setEnabled(false);
        progressBar.setVisible(true);
        statusLabel.setForeground(new Color(0, 102, 204));
        statusLabel.setText("Validating and inserting registration record into database...");

        SwingWorker<RegistrationService.ServiceResult, Void> worker = new SwingWorker<>() {
            @Override
            protected RegistrationService.ServiceResult doInBackground() throws Exception {
                // Simulate network DB round-trip latency (1.2 seconds) off the EDT
                Thread.sleep(1200);
                return service.registerStudent(regNo, name, dept, eventName, email, phone);
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                registerBtn.setEnabled(true);
                clearBtn.setEnabled(true);

                try {
                    RegistrationService.ServiceResult res = get();
                    if (res.success) {
                        statusLabel.setForeground(new Color(40, 167, 69));
                        statusLabel.setText("Success: " + res.message);
                        JOptionPane.showMessageDialog(
                            EventRegistrationApp.this,
                            "Registration Successful!\nID: " + res.data.getId() + 
                            "\nStudent: " + res.data.getStudentName() + 
                            "\nEvent: " + res.data.getEventName(),
                            "Registration Confirmed",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        clearForm();
                        loadTableData((String) filterCombo.getSelectedItem());
                    } else {
                        statusLabel.setForeground(Color.RED);
                        statusLabel.setText("Error: " + res.message);
                        JOptionPane.showMessageDialog(
                            EventRegistrationApp.this,
                            res.message,
                            "Registration Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                } catch (Exception ex) {
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("System Fault: " + ex.getMessage());
                }
            }
        };

        worker.execute();
    }

    private void loadTableData(String eventFilter) {
        progressBar.setVisible(true);
        statusLabel.setForeground(new Color(80, 80, 80));
        statusLabel.setText("Loading registrations for [" + eventFilter + "]...");

        SwingWorker<List<Registration>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Registration> doInBackground() throws Exception {
                // Query executed on background thread
                return service.getRegistrations(eventFilter);
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                try {
                    List<Registration> list = get();
                    tableModel.setRowCount(0);
                    for (Registration r : list) {
                        tableModel.addRow(new Object[]{
                            r.getId(), r.getRegNo(), r.getStudentName(), r.getDepartment(),
                            r.getEventName(), r.getEmail(), r.getPhone(), r.getRegisteredAt(), r.getStatus()
                        });
                    }
                    statusLabel.setForeground(new Color(40, 167, 69));
                    statusLabel.setText("Loaded " + list.size() + " registrations for filter [" + eventFilter + "]. Ready.");
                } catch (Exception e) {
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("Failed to load table: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void clearForm() {
        nameField.setText("");
        regNoField.setText("");
        emailField.setText("");
        phoneField.setText("");
        deptCombo.setSelectedIndex(0);
        eventCombo.setSelectedIndex(0);
        nameField.requestFocus();
    }

    private void exportToCSV() {
        File targetFile = new File("event_registrations.csv");
        try (PrintWriter pw = new PrintWriter(new FileWriter(targetFile))) {
            // Write CSV headers
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                pw.print(tableModel.getColumnName(col));
                if (col < tableModel.getColumnCount() - 1) pw.print(",");
            }
            pw.println();

            // Write CSV rows
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    Object val = tableModel.getValueAt(row, col);
                    pw.print("\"" + (val != null ? val.toString() : "") + "\"");
                    if (col < tableModel.getColumnCount() - 1) pw.print(",");
                }
                pw.println();
            }

            statusLabel.setForeground(new Color(40, 167, 69));
            statusLabel.setText("Successfully exported " + tableModel.getRowCount() + " records to " + targetFile.getName());
            JOptionPane.showMessageDialog(this, "Registrations successfully exported to:\n" + targetFile.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Export Error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Failed to export CSV: " + ex.getMessage(), "Export Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EventRegistrationApp app = new EventRegistrationApp();
            app.setVisible(true);
        });
    }
}
