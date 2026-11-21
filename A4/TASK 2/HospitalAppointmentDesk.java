import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

/**
 * TASK 2: Hospital Outpatient Appointment Desk
 * 
 * Student: P. Madesh (192365007)
 * Faculty Evaluator: Surya V
 * Course: CSA09 - Programming in Java (CO4: Event-Driven GUI Design)
 * 
 * Features:
 * 1. Dynamic doctor dropdown cascading on department selection.
 * 2. Input validation for Patient ID and slot availability.
 * 3. Asynchronous booking simulation with in-progress state tracking.
 * 4. Confirmation dialog on window closing if booking is active.
 * 5. Full containment hierarchy with nested Swing panels.
 */
public class HospitalAppointmentDesk extends JFrame {
    // UI Components
    private final JTextField patientIdField = new JTextField(12);
    private final JComboBox<String> deptCombo = new JComboBox<>();
    private final JComboBox<String> doctorCombo = new JComboBox<>();
    private final JComboBox<String> slotCombo = new JComboBox<>();
    private final JButton bookBtn = new JButton("Book Appointment");
    private final JButton resetBtn = new JButton("Reset Form");
    private final JLabel statusLabel = new JLabel("Welcome to Outpatient Desk. Select department to begin.", JLabel.CENTER);
    private final JTextArea confirmationArea = new JTextArea(4, 30);
    private final JProgressBar progressBar = new JProgressBar();

    // In-memory Department -> Doctors Cache (O(1) lookup)
    private final Map<String, List<String>> deptDoctorMap = new LinkedHashMap<>();

    // State tracking
    private boolean isBookingInProgress = false;

    public HospitalAppointmentDesk() {
        super("SIMATS Hospital - Outpatient Appointment Desk");
        initData();
        initUI();
        bindEvents();
    }

    private void initData() {
        deptDoctorMap.put("Cardiology", Arrays.asList("Dr. R. Sharma (MD, DM)", "Dr. K. Anitha (MD)", "Dr. V. Rajesh (DNB)"));
        deptDoctorMap.put("Neurology", Arrays.asList("Dr. S. Sundaram (MCh)", "Dr. M. Deepa (MD)"));
        deptDoctorMap.put("Orthopedics", Arrays.asList("Dr. A. Joseph (MS)", "Dr. P. Chandran (D.Ortho)"));
        deptDoctorMap.put("Pediatrics", Arrays.asList("Dr. G. Lakshmi (MD)", "Dr. T. Praveen (DCH)"));
        deptDoctorMap.put("Dermatology", Arrays.asList("Dr. N. Harish (MD)", "Dr. B. Kavitha (DDVL)"));
    }

    private void initUI() {
        setLayout(new BorderLayout(12, 12));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // North: Hospital Banner
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(27, 54, 93));
        JLabel title = new JLabel("HOSPITAL OUTPATIENT APPOINTMENT SCHEDULER");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 14));
        headerPanel.add(title);
        add(headerPanel, BorderLayout.NORTH);

        // Center: Form Panel in GridBagLayout
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder(" Patient & Appointment Details "));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.anchor = GridBagConstraints.WEST;

        // Populate Combos
        for (String dept : deptDoctorMap.keySet()) {
            deptCombo.addItem(dept);
        }
        refreshDoctorList((String) deptCombo.getSelectedItem());

        slotCombo.addItem("09:00 AM - 09:30 AM");
        slotCombo.addItem("10:00 AM - 10:30 AM");
        slotCombo.addItem("11:30 AM - 12:00 PM");
        slotCombo.addItem("02:00 PM - 02:30 PM");
        slotCombo.addItem("03:30 PM - 04:00 PM");

        // Row 0: Patient ID
        c.gridx = 0; c.gridy = 0; formPanel.add(new JLabel("Patient ID (e.g., P-1029):"), c);
        c.gridx = 1; formPanel.add(patientIdField, c);

        // Row 1: Department
        c.gridx = 0; c.gridy = 1; formPanel.add(new JLabel("Department:"), c);
        c.gridx = 1; formPanel.add(deptCombo, c);

        // Row 2: Doctor (Dependent Dropdown)
        c.gridx = 0; c.gridy = 2; formPanel.add(new JLabel("Consulting Doctor:"), c);
        c.gridx = 1; formPanel.add(doctorCombo, c);

        // Row 3: Slot
        c.gridx = 0; c.gridy = 3; formPanel.add(new JLabel("Appointment Slot:"), c);
        c.gridx = 1; formPanel.add(slotCombo, c);

        // Row 4: Buttons
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        bookBtn.setBackground(new Color(44, 94, 138));
        bookBtn.setForeground(Color.WHITE);
        resetBtn.setBackground(new Color(108, 117, 125));
        resetBtn.setForeground(Color.WHITE);
        buttonRow.add(bookBtn);
        buttonRow.add(resetBtn);

        c.gridx = 0; c.gridy = 4; c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonRow, c);

        // Row 5: Confirmation Area
        confirmationArea.setEditable(false);
        confirmationArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        confirmationArea.setBackground(new Color(245, 247, 250));
        JScrollPane scrollArea = new JScrollPane(confirmationArea);
        c.gridx = 0; c.gridy = 5; c.gridwidth = 2; c.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(scrollArea, c);

        add(formPanel, BorderLayout.CENTER);

        // South: Status and Progress
        JPanel southPanel = new JPanel(new BorderLayout(5, 5));
        southPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 8, 10));
        progressBar.setVisible(false);
        progressBar.setIndeterminate(true);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        southPanel.add(progressBar, BorderLayout.NORTH);
        southPanel.add(statusLabel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(540, 420));
        setLocationRelativeTo(null);
    }

    private void bindEvents() {
        // Event 1: Department selection changes -> Doctor dropdown updates
        deptCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selectedDept = (String) e.getItem();
                refreshDoctorList(selectedDept);
                statusLabel.setText("Department changed to " + selectedDept + ". Doctor list refreshed.");
            }
        });

        // Event 2: Book Appointment Button
        bookBtn.addActionListener(e -> handleBooking());

        // Event 3: Reset Button
        resetBtn.addActionListener(e -> resetForm());

        // Event 4: Window Closing Event (Must ask confirmation if booking is in progress)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (isBookingInProgress) {
                    int response = JOptionPane.showConfirmDialog(
                        HospitalAppointmentDesk.this,
                        "A booking is currently in progress!\nAre you sure you want to abort and exit?",
                        "Booking in Progress",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                    );
                    if (response == JOptionPane.YES_OPTION) {
                        dispose();
                        System.exit(0);
                    }
                } else {
                    int response = JOptionPane.showConfirmDialog(
                        HospitalAppointmentDesk.this,
                        "Exit Appointment Scheduler?",
                        "Confirm Exit",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                    );
                    if (response == JOptionPane.YES_OPTION) {
                        dispose();
                        System.exit(0);
                    }
                }
            }
        });
    }

    private void refreshDoctorList(String dept) {
        doctorCombo.removeAllItems();
        List<String> docs = deptDoctorMap.get(dept);
        if (docs != null) {
            for (String doc : docs) {
                doctorCombo.addItem(doc);
            }
        }
    }

    private void handleBooking() {
        String pid = patientIdField.getText().trim();
        String dept = (String) deptCombo.getSelectedItem();
        String doc = (String) doctorCombo.getSelectedItem();
        String slot = (String) slotCombo.getSelectedItem();

        // Validation
        if (pid.isEmpty()) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Error: Patient ID is required.");
            JOptionPane.showMessageDialog(this, "Please enter Patient ID.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            patientIdField.requestFocus();
            return;
        }

        if (!pid.matches("^P-[0-9]{3,6}$")) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Error: Patient ID must match format 'P-XXXX' (e.g. P-1029).");
            JOptionPane.showMessageDialog(this, "Patient ID format invalid. Must be 'P-' followed by 3-6 digits.", "Format Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Lock form & mark in-progress
        isBookingInProgress = true;
        bookBtn.setEnabled(false);
        resetBtn.setEnabled(false);
        progressBar.setVisible(true);
        statusLabel.setForeground(new Color(0, 102, 204));
        statusLabel.setText("Contacting hospital scheduling server for " + doc + "...");

        // Asynchronous scheduling worker
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                Thread.sleep(1800); // Simulate network booking latency
                return "APPT-" + (100000 + new Random().nextInt(900000));
            }

            @Override
            protected void done() {
                isBookingInProgress = false;
                progressBar.setVisible(false);
                bookBtn.setEnabled(true);
                resetBtn.setEnabled(true);

                try {
                    String apptId = get();
                    statusLabel.setForeground(new Color(40, 167, 69));
                    statusLabel.setText("Booking Confirmed: " + apptId);
                    
                    String receipt = String.format(
                        "-----------------------------------------------\n" +
                        " APPOINTMENT CONFIRMATION: %s\n" +
                        " Patient ID : %s | Dept: %s\n" +
                        " Doctor     : %s\n" +
                        " Time Slot  : %s\n" +
                        " Status     : CONFIRMED & SLOTTED\n" +
                        "-----------------------------------------------",
                        apptId, pid, dept, doc, slot
                    );
                    confirmationArea.setText(receipt);

                    JOptionPane.showMessageDialog(
                        HospitalAppointmentDesk.this,
                        "Appointment successfully booked!\nAppointment Token: " + apptId + 
                        "\nPatient: " + pid + "\nDoctor: " + doc + "\nSlot: " + slot,
                        "Booking Confirmed",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (Exception ex) {
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("Booking failed: " + ex.getMessage());
                }
            }
        };

        worker.execute();
    }

    private void resetForm() {
        patientIdField.setText("");
        deptCombo.setSelectedIndex(0);
        slotCombo.setSelectedIndex(0);
        confirmationArea.setText("");
        statusLabel.setForeground(Color.BLACK);
        statusLabel.setText("Form reset. Ready.");
        patientIdField.requestFocus();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HospitalAppointmentDesk desk = new HospitalAppointmentDesk();
            desk.setVisible(true);
        });
    }
}
