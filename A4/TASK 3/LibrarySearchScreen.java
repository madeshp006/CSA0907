import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * TASK 3: Campus Library Search Screen with Responsive Nested Layouts
 * 
 * Student: P. Madesh (192365007)
 * Faculty Evaluator: Surya V
 * Course: CSA09 - Programming in Java (CO4: Event-Driven GUI Design)
 * 
 * Layout Hierarchy & Resizing Invariants:
 * 1. Root Frame ContentPane: BorderLayout(0, 0)
 *    - NORTH : Search Panel (BorderLayout -> FlowLayout(LEFT)). Preserves preferred height.
 *    - CENTER: Results Panel (BorderLayout wrapping JScrollPane). Expands vertically & horizontally!
 *    - SOUTH : Navigation & Status Panel (BorderLayout -> Center: FlowLayout buttons, South: Status label).
 *              Preserves button sizes and status row height.
 */
public class LibrarySearchScreen extends JFrame {
    // Search Components
    private final JTextField searchField = new JTextField(18);
    private final JComboBox<String> categoryCombo = new JComboBox<>(new String[]{
        "All Categories", "Computer Science", "Electrical Engg", "Mathematics", "Mechanical Engg"
    });
    private final JButton searchBtn = new JButton("Search Catalog");

    // Results Table Components
    private final DefaultTableModel tableModel = new DefaultTableModel(
        new Object[][]{
            {"CS-101", "Introduction to Java Programming & Data Structures", "Y. Daniel Liang", "Floor 2, Shelf B4", "AVAILABLE"},
            {"CS-204", "Operating System Concepts (Silberschatz)", "A. Silberschatz", "Floor 2, Shelf C1", "RESERVED"},
            {"CS-308", "Design Patterns: Elements of Reusable Object-Oriented Software", "Erich Gamma et al.", "Floor 2, Shelf A2", "AVAILABLE"},
            {"EE-201", "Signals and Systems (Oppenheim)", "Alan V. Oppenheim", "Floor 1, Shelf D3", "AVAILABLE"},
            {"MA-105", "Advanced Engineering Mathematics", "Erwin Kreyszig", "Floor 3, Shelf E1", "AVAILABLE"},
            {"CS-412", "Database System Concepts", "Abraham Silberschatz", "Floor 2, Shelf C3", "AVAILABLE"},
            {"CS-520", "Computer Networks: A Systems Approach", "Larry L. Peterson", "Floor 2, Shelf D5", "RESERVED"},
            {"ME-301", "Fluid Mechanics & Thermodynamics", "Frank M. White", "Floor 1, Shelf A4", "AVAILABLE"}
        },
        new String[]{"Accession #", "Title", "Author(s)", "Location / Call No.", "Status"}
    );
    private final JTable resultsTable = new JTable(tableModel);

    // Navigation and Action Buttons
    private final JButton prevBtn = new JButton("< Prev");
    private final JButton nextBtn = new JButton("Next >");
    private final JButton reserveBtn = new JButton("Reserve Selected Book");
    private final JLabel statusLabel = new JLabel("Showing 8 search records. Ready.", JLabel.LEFT);

    public LibrarySearchScreen() {
        super("SIMATS Engineering - Central Campus Library Catalog");
        initUI();
        bindEvents();
    }

    private void initUI() {
        // Root container uses BorderLayout
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(0, 0));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // =========================================================================
        // 1. NORTH CONTAINER: Title Banner and Search Controls Row
        // =========================================================================
        JPanel northContainer = new JPanel(new BorderLayout());

        // Title Banner
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(27, 54, 93));
        JLabel titleLabel = new JLabel("CAMPUS CENTRAL LIBRARY - SEARCH & RESERVATION PORTAL");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 13));
        titlePanel.add(titleLabel);
        northContainer.add(titlePanel, BorderLayout.NORTH);

        // Search Row: Uses FlowLayout(LEFT) so components retain their natural preferred size
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        searchRow.setBackground(new Color(245, 247, 250));
        searchRow.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(208, 215, 222)));

        searchRow.add(new JLabel("Keywords:"));
        searchRow.add(searchField);
        searchRow.add(new JLabel("Category:"));
        searchRow.add(categoryCombo);
        searchBtn.setBackground(new Color(44, 94, 138));
        searchBtn.setForeground(Color.WHITE);
        searchRow.add(searchBtn);

        northContainer.add(searchRow, BorderLayout.CENTER);
        contentPane.add(northContainer, BorderLayout.NORTH);

        // =========================================================================
        // 2. CENTER CONTAINER: Results View (Expands in both directions!)
        // =========================================================================
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBorder(BorderFactory.createTitledBorder(" Catalog Search Results "));

        resultsTable.setRowHeight(24);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        resultsTable.getTableHeader().setBackground(new Color(235, 240, 245));

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        centerContainer.add(scrollPane, BorderLayout.CENTER);
        contentPane.add(centerContainer, BorderLayout.CENTER);

        // =========================================================================
        // 3. SOUTH CONTAINER: Bottom Action Buttons and Status Bar
        // =========================================================================
        JPanel southContainer = new JPanel(new BorderLayout());
        southContainer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(208, 215, 222)));

        // Action Buttons Row: Uses FlowLayout(CENTER) so buttons keep standard size
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        prevBtn.setBackground(new Color(230, 235, 240));
        nextBtn.setBackground(new Color(230, 235, 240));
        reserveBtn.setBackground(new Color(40, 167, 69));
        reserveBtn.setForeground(Color.WHITE);

        buttonRow.add(prevBtn);
        buttonRow.add(nextBtn);
        buttonRow.add(reserveBtn);
        southContainer.add(buttonRow, BorderLayout.CENTER);

        // Status Bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        statusBar.setBackground(new Color(240, 242, 245));
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(60, 60, 60));
        statusBar.add(statusLabel);
        southContainer.add(statusBar, BorderLayout.SOUTH);

        contentPane.add(southContainer, BorderLayout.SOUTH);

        // Window Dimensions
        setSize(780, 480);
        setMinimumSize(new Dimension(520, 340));
        setLocationRelativeTo(null);
    }

    private void bindEvents() {
        searchBtn.addActionListener(e -> {
            String query = searchField.getText().trim();
            String cat = (String) categoryCombo.getSelectedItem();
            statusLabel.setText("Filtering catalog by '" + query + "' under " + cat + "... (8 matches found)");
            JOptionPane.showMessageDialog(this, "Search executed for '" + query + "' in category [" + cat + "].", "Search Query", JOptionPane.INFORMATION_MESSAGE);
        });

        reserveBtn.addActionListener(e -> {
            int selectedRow = resultsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a book from the results table to reserve.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String title = (String) tableModel.getValueAt(selectedRow, 1);
            String status = (String) tableModel.getValueAt(selectedRow, 4);
            if ("RESERVED".equalsIgnoreCase(status)) {
                JOptionPane.showMessageDialog(this, "This book is already reserved by another borrower.", "Unavailable", JOptionPane.ERROR_MESSAGE);
            } else {
                tableModel.setValueAt("RESERVED", selectedRow, 4);
                statusLabel.setText("Success: Reserved '" + title + "'. Collection hold active for 24 hours.");
                JOptionPane.showMessageDialog(this, "Book successfully reserved:\n" + title, "Reservation Confirmed", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        prevBtn.addActionListener(e -> statusLabel.setText("Already at page 1 of catalog."));
        nextBtn.addActionListener(e -> statusLabel.setText("Page 2 has no additional records."));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LibrarySearchScreen screen = new LibrarySearchScreen();
            screen.setVisible(true);
        });
    }
}
