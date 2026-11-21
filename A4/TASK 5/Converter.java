import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * TASK 5: Complete the Code - Currency Converter (Lambda Version)
 * 
 * Student: P. Madesh (192365007)
 * Faculty Evaluator: Surya V
 * Course: CSA09 - Programming in Java (CO4: Event-Driven GUI Design)
 * 
 * Completed TODO 1 and TODO 2:
 * - TODO 1: Registers ONE reusable lambda listener for both the Convert button and Enter key.
 * - TODO 2: Parses the amount safely, displaying formatted 2-decimal output or "Invalid amount".
 */
public class Converter extends JFrame {
    private final JTextField amount = new JTextField(10);
    private final JComboBox<String> target =
        new JComboBox<>(new String[]{"USD", "EUR"});
    private final JLabel result = new JLabel(" ");

    private static double rate(String currency) {
        return currency.equals("USD") ? 0.012 : 0.011;
    }

    public Converter() {
        super("Currency Converter");
        setLayout(new FlowLayout());
        JButton convertButton = new JButton("Convert");
        add(new JLabel("INR")); add(amount); add(target);
        add(convertButton); add(result);

        // TODO 1: Register ONE lambda listener for both the Convert button and the
        // Enter key in the amount field
        ActionListener listener = e -> convert();
        convertButton.addActionListener(listener);
        amount.addActionListener(listener);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private void convert() {
        // TODO 2: Parse the amount and show the converted value with 2 decimals, or
        // "Invalid amount" if parsing fails
        try {
            double inr = Double.parseDouble(amount.getText().trim());
            if (inr < 0) {
                result.setText("Invalid amount");
                return;
            }
            String selectedCurrency = (String) target.getSelectedItem();
            double converted = inr * rate(selectedCurrency);
            result.setText(String.format("%.2f %s", converted, selectedCurrency));
        } catch (NumberFormatException e) {
            result.setText("Invalid amount");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Converter().setVisible(true));
    }
}
