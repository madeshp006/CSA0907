import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * TASK 1: Refactored Event-Driven Architecture
 * 
 * Student: P. Madesh (192365007)
 * Faculty Evaluator: Surya V
 * Course: CSA09 - Programming in Java (CO4: Event-Driven GUI Design)
 * 
 * Improvements Implemented:
 * 1. Strict Separation of Concerns: GUI delegates to FeeService.
 * 2. Dedicated Event Handlers: Independent ActionListeners for each button.
 * 3. Robust Input Validation: Handles empty, non-numeric, and out-of-range input.
 * 4. Asynchronous Background Execution: DB and network latency executed off the EDT.
 * 5. Visual UI Feedback: Progress updates and status banners visible on screen.
 * 6. Receipt Generation: Dedicated dialog displaying transaction invoice.
 * 7. Security: Parameterized PreparedStatement eliminating SQL injection.
 * 8. Window Closing: Proper WindowAdapter handling clean disposal.
 */
public class FeeWindowRefactored extends JFrame {
    private final JTextField regNoField = new JTextField(15);
    private final JTextField amountField = new JTextField(10);
    private final JButton payBtn = new JButton("Pay Fee");
    private final JButton clearBtn = new JButton("Clear Fields");
    private final JButton receiptBtn = new JButton("View Receipt");
    private final JLabel statusLabel = new JLabel("Ready. Enter details to proceed.", JLabel.CENTER);
    private final JProgressBar progressBar = new JProgressBar();

    private final FeeService feeService = new FeeService();
    private FeeService.PaymentReceipt lastReceipt = null;

    public FeeWindowRefactored() {
        super("SIMATS Engineering - Student Fee Payment Portal");
        initUI();
        bindEvents();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(27, 54, 93));
        JLabel titleLabel = new JLabel("ONLINE FEE SUBMISSION SYSTEM");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Register Number:"), gbc);
        gbc.gridx = 1;
        formPanel.add(regNoField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Tuition Fee Amount (Rs):"), gbc);
        gbc.gridx = 1;
        formPanel.add(amountField, gbc);

        // Button Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        payBtn.setBackground(new Color(44, 94, 138));
        payBtn.setForeground(Color.WHITE);
        clearBtn.setBackground(new Color(108, 117, 125));
        clearBtn.setForeground(Color.WHITE);
        receiptBtn.setBackground(new Color(40, 167, 69));
        receiptBtn.setForeground(Color.WHITE);
        receiptBtn.setEnabled(false);

        btnPanel.add(payBtn);
        btnPanel.add(clearBtn);
        btnPanel.add(receiptBtn);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(btnPanel, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Footer / Status Panel
        JPanel footerPanel = new JPanel(new BorderLayout(5, 5));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        progressBar.setVisible(false);
        progressBar.setIndeterminate(true);
        statusLabel.setForeground(new Color(30, 30, 30));
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        footerPanel.add(progressBar, BorderLayout.NORTH);
        footerPanel.add(statusLabel, BorderLayout.SOUTH);
        add(footerPanel, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(480, 240));
        setLocationRelativeTo(null);
    }

    private void bindEvents() {
        // Dedicated Listener 1: Pay Action
        payBtn.addActionListener(e -> handlePayment());

        // Dedicated Listener 2: Clear Action
        clearBtn.addActionListener(e -> handleClear());

        // Dedicated Listener 3: Receipt Action
        receiptBtn.addActionListener(e -> handleReceipt());

        // Window Closing Listener
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int choice = JOptionPane.showConfirmDialog(
                    FeeWindowRefactored.this,
                    "Are you sure you want to exit the Fee Portal?",
                    "Confirm Exit",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                if (choice == JOptionPane.YES_OPTION) {
                    dispose();
                    System.exit(0);
                }
            }
        });
    }

    private void handlePayment() {
        String reg = regNoField.getText();
        String amtStr = amountField.getText();

        // 1. Validate Input
        FeeService.ValidationResult val = feeService.validate(reg, amtStr);
        if (!val.valid) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Validation Error: " + val.errorMessage);
            JOptionPane.showMessageDialog(this, val.errorMessage, "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Execute Payment Asynchronously off the EDT using SwingWorker
        payBtn.setEnabled(false);
        clearBtn.setEnabled(false);
        progressBar.setVisible(true);
        statusLabel.setForeground(new Color(0, 102, 204));
        statusLabel.setText("Connecting to bank payment gateway... please wait.");

        SwingWorker<FeeService.PaymentReceipt, Void> worker = new SwingWorker<>() {
            @Override
            protected FeeService.PaymentReceipt doInBackground() throws Exception {
                // Heavy work (network/DB) runs on background worker thread
                return feeService.processPayment(reg, val.parsedAmount);
            }

            @Override
            protected void done() {
                // Completion callback runs safely back on the EDT
                progressBar.setVisible(false);
                payBtn.setEnabled(true);
                clearBtn.setEnabled(true);
                try {
                    lastReceipt = get();
                    receiptBtn.setEnabled(true);
                    statusLabel.setForeground(new Color(40, 167, 69));
                    statusLabel.setText("Payment Confirmed! Tx ID: " + lastReceipt.transactionId);
                    JOptionPane.showMessageDialog(
                        FeeWindowRefactored.this,
                        "Payment of Rs. " + String.format("%.2f", lastReceipt.amount) + 
                        " successfully processed for Reg No: " + lastReceipt.regNo + 
                        "\nTransaction ID: " + lastReceipt.transactionId,
                        "Payment Successful",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (Exception ex) {
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("Transaction Failed: " + ex.getMessage());
                    JOptionPane.showMessageDialog(
                        FeeWindowRefactored.this,
                        "Payment gateway error: " + ex.getMessage(),
                        "Transaction Failed",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }

    private void handleClear() {
        regNoField.setText("");
        amountField.setText("");
        statusLabel.setForeground(Color.BLACK);
        statusLabel.setText("Fields cleared. Ready.");
        receiptBtn.setEnabled(false);
        lastReceipt = null;
        regNoField.requestFocus();
    }

    private void handleReceipt() {
        if (lastReceipt == null) {
            JOptionPane.showMessageDialog(this, "No successful transaction to display.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JTextArea receiptArea = new JTextArea(lastReceipt.toString());
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        receiptArea.setEditable(false);
        receiptArea.setBackground(new Color(245, 245, 245));
        receiptArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JOptionPane.showMessageDialog(this, receiptArea, "Payment Receipt", JOptionPane.PLAIN_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FeeWindowRefactored window = new FeeWindowRefactored();
            window.setVisible(true);
        });
    }
}
