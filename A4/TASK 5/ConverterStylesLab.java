import java.awt.FlowLayout;
import java.awt.event.*;
import javax.swing.*;

/**
 * TASK 5: Four Event-Handling Styles & Modification Lab
 * 
 * Demonstrates and evaluates all 4 event-handling styles:
 * Style A: Frame itself implements ActionListener
 * Style B: Named inner class (ConvertHandler implements ActionListener)
 * Style C: Anonymous inner class (new ActionListener() { ... })
 * Style D: Lambda expression / Method reference (e -> convert())
 * 
 * Demonstrates Task 5.2 requirement changes:
 * 1. Convert triggered by Enter key in amount field.
 * 2. Result displayed in a JOptionPane dialog box.
 */
public class ConverterStylesLab {

    // Common conversion calculation logic
    private static double rate(String currency) {
        return currency.equals("USD") ? 0.012 : 0.011;
    }

    private static String executeConversion(String text, String currency) {
        try {
            double inr = Double.parseDouble(text.trim());
            if (inr < 0) return "Invalid amount: must be positive";
            double converted = inr * rate(currency);
            return String.format("%.2f %s", converted, currency);
        } catch (NumberFormatException e) {
            return "Invalid amount: non-numeric input";
        }
    }

    // =========================================================================
    // STYLE A: Frame Itself Implements ActionListener
    // =========================================================================
    public static class StyleAFrame extends JFrame implements ActionListener {
        private final JTextField amount = new JTextField(10);
        private final JComboBox<String> target = new JComboBox<>(new String[]{"USD", "EUR"});
        private final JButton convertBtn = new JButton("Convert (Style A)");
        private final JLabel result = new JLabel(" ");

        public StyleAFrame() {
            super("Style A: Frame Implements Listener");
            setLayout(new FlowLayout());
            add(new JLabel("INR:")); add(amount); add(target);
            add(convertBtn); add(result);

            // Register 'this' for both button and enter key
            convertBtn.addActionListener(this);
            amount.addActionListener(this); // Task 5.2 change

            addWindowListener(new WindowAdapter() {
                @Override public void windowClosing(WindowEvent e) { dispose(); }
            });
            pack();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String res = executeConversion(amount.getText(), (String) target.getSelectedItem());
            result.setText(res);
            // Task 5.2 change: Show dialog box
            JOptionPane.showMessageDialog(this, "Converted Result: " + res, "Style A Output", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // =========================================================================
    // STYLE B: Named Inner Class
    // =========================================================================
    public static class StyleBFrame extends JFrame {
        private final JTextField amount = new JTextField(10);
        private final JComboBox<String> target = new JComboBox<>(new String[]{"USD", "EUR"});
        private final JButton convertBtn = new JButton("Convert (Style B)");
        private final JLabel result = new JLabel(" ");

        public StyleBFrame() {
            super("Style B: Named Inner Class");
            setLayout(new FlowLayout());
            add(new JLabel("INR:")); add(amount); add(target);
            add(convertBtn); add(result);

            // Instantiate single reusable named listener instance
            ConvertActionHandler handler = new ConvertActionHandler();
            convertBtn.addActionListener(handler);
            amount.addActionListener(handler); // Task 5.2 change

            addWindowListener(new WindowAdapter() {
                @Override public void windowClosing(WindowEvent e) { dispose(); }
            });
            pack();
        }

        private class ConvertActionHandler implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                String res = executeConversion(amount.getText(), (String) target.getSelectedItem());
                result.setText(res);
                JOptionPane.showMessageDialog(StyleBFrame.this, "Converted Result: " + res, "Style B Output", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    // =========================================================================
    // STYLE C: Anonymous Inner Class
    // =========================================================================
    public static class StyleCFrame extends JFrame {
        private final JTextField amount = new JTextField(10);
        private final JComboBox<String> target = new JComboBox<>(new String[]{"USD", "EUR"});
        private final JButton convertBtn = new JButton("Convert (Style C)");
        private final JLabel result = new JLabel(" ");

        public StyleCFrame() {
            super("Style C: Anonymous Inner Class");
            setLayout(new FlowLayout());
            add(new JLabel("INR:")); add(amount); add(target);
            add(convertBtn); add(result);

            // Reusable anonymous inner class reference
            ActionListener anonListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String res = executeConversion(amount.getText(), (String) target.getSelectedItem());
                    result.setText(res);
                    JOptionPane.showMessageDialog(StyleCFrame.this, "Converted Result: " + res, "Style C Output", JOptionPane.INFORMATION_MESSAGE);
                }
            };
            convertBtn.addActionListener(anonListener);
            amount.addActionListener(anonListener); // Task 5.2 change

            addWindowListener(new WindowAdapter() {
                @Override public void windowClosing(WindowEvent e) { dispose(); }
            });
            pack();
        }
    }

    // =========================================================================
    // STYLE D: Lambda Expression (Modern Concise Style)
    // =========================================================================
    public static class StyleDFrame extends JFrame {
        private final JTextField amount = new JTextField(10);
        private final JComboBox<String> target = new JComboBox<>(new String[]{"USD", "EUR"});
        private final JButton convertBtn = new JButton("Convert (Style D)");
        private final JLabel result = new JLabel(" ");

        public StyleDFrame() {
            super("Style D: Lambda Expression");
            setLayout(new FlowLayout());
            add(new JLabel("INR:")); add(amount); add(target);
            add(convertBtn); add(result);

            // Reusable single lambda listener
            ActionListener lambdaHandler = e -> {
                String res = executeConversion(amount.getText(), (String) target.getSelectedItem());
                result.setText(res);
                JOptionPane.showMessageDialog(this, "Converted Result: " + res, "Style D Output", JOptionPane.INFORMATION_MESSAGE);
            };

            convertBtn.addActionListener(lambdaHandler);
            amount.addActionListener(lambdaHandler); // Task 5.2 change

            addWindowListener(new WindowAdapter() {
                @Override public void windowClosing(WindowEvent e) { dispose(); }
            });
            pack();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("Launching Style D Converter (Modern Lambda Implementation)...");
            new StyleDFrame().setVisible(true);
        });
    }
}
