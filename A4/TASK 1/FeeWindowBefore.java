import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * TASK 1: Flawed Legacy Implementation for Design Review
 * 
 * Student: P. Madesh (192365007)
 * Course: CSA09 - Programming in Java (CO4: Event-Driven GUI Design)
 * 
 * This class represents the junior developer's initial flawed implementation
 * provided in the assignment specification for forensic diagnosis.
 */
public class FeeWindowBefore extends Frame implements ActionListener {
    TextField regNo = new TextField(15);
    TextField amount = new TextField(10);
    Button pay = new Button("Pay");
    Button clear = new Button("Clear");
    Button receipt = new Button("Receipt");

    // Note: Mock credentials to demonstrate compile and runtime flaws
    private static final String URL = "jdbc:sqlite:fees.db";
    private static final String USER = "admin";
    private static final String PWD = "password";

    public FeeWindowBefore() {
        super("Student Fee Payment (Flawed Legacy Design)");
        setLayout(new FlowLayout());
        add(new Label("Reg No:"));
        add(regNo);
        add(new Label("Amount:"));
        add(amount);
        add(pay);
        add(clear);
        add(receipt);

        pay.addActionListener(this);
        clear.addActionListener(this);
        receipt.addActionListener(this);

        setSize(450, 180);
        setLocationRelativeTo(null);
    }

    public void actionPerformed(ActionEvent e) {
        // DEFICIENCY 1: Using '==' for string reference comparison instead of .equals()
        // DEFICIENCY 2: Monolithic handler mixing UI, business logic, DB access, and thread sleep
        if (e.getActionCommand() == "Pay") {
            try {
                // DEFICIENCY 3: No input validation; throws unhandled NumberFormatException
                double a = Double.parseDouble(amount.getText());

                // DEFICIENCY 4: Heavy database work and Thread.sleep() on Event Dispatch Thread (EDT)
                // Freezes the entire GUI window for 3000ms!
                Connection c = DriverManager.getConnection(URL, USER, PWD);

                // DEFICIENCY 5: Severe SQL Injection vulnerability via string concatenation
                c.createStatement().executeUpdate("INSERT INTO fees VALUES('" + regNo.getText() + "'," + a + ")");
                
                // DEFICIENCY 6: Freezing the EDT thread directly
                Thread.sleep(3000); // wait for bank confirmation

                // DEFICIENCY 7: Feedback given only on System.out (console), invisible to GUI user!
                System.out.println("Paid");

                // DEFICIENCY 8: Resource leak! Connection and Statement never closed!
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        } else {
            // DEFICIENCY 9: 'Receipt' button falls into 'else', which clears the fields!
            regNo.setText("");
            amount.setText("");
        }
    }

    public static void main(String[] args) {
        FeeWindowBefore window = new FeeWindowBefore();
        window.setVisible(true);
        System.out.println("FeeWindowBefore loaded. Notice: Window cannot be closed via 'X' button (missing WindowListener).");
    }
}
