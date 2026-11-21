import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Label;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * TASK 4: Defective Code Demonstration
 * 
 * Demonstrates:
 * 1. Misspelling method name as mouseclicked without @Override (compiles silently but fails at runtime).
 * 2. How adding @Override immediately catches the error at compile-time.
 */
public class DrawPadDefective extends Frame {
    private final List<Point> points = new ArrayList<>();
    private final Label status = new Label("Clicks: 0");

    public DrawPadDefective() {
        super("Defective Draw Pad");
        add(status, BorderLayout.SOUTH);

        addMouseListener(new MouseAdapter() {
            // NOTE: Misspelled method name (lowercase 'c').
            // If @Override is added here, javac rejects compilation!
            // @Override
            public void mouseclicked(MouseEvent e) {
                // This method is NEVER called by the AWT Event Dispatcher!
                points.add(e.getPoint());
                status.setText("Clicks: " + points.size());
                repaint();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        setSize(400, 300);
    }

    public static void main(String[] args) {
        System.out.println("DrawPadDefective: Running with misspelled 'mouseclicked'. Notice clicks do nothing!");
        new DrawPadDefective().setVisible(true);
    }
}
