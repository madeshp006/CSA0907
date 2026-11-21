import java.awt.BorderLayout;
import java.awt.Color;
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
 * TASK 4: Event-Handling Forensics & Repaired DrawPad
 * 
 * Student: P. Madesh (192365007)
 * Faculty Evaluator: Surya V
 * Course: CSA09 - Programming in Java (CO4: Event-Driven GUI Design)
 * 
 * Completed TODO 1 and TODO 2:
 * 1. Correctly overrides mouseClicked (with @Override protection).
 * 2. Adds points to List<Point>, updates status label, and calls repaint().
 * 3. In paint(Graphics g), renders every point as a 6x6 circle centered on the coordinates.
 * 4. Ensures dots permanently survive window minimizing, restoring, and resizing!
 */
public class DrawPad extends Frame {
    private final List<Point> points = new ArrayList<>();
    private final Label status = new Label("Clicks: 0");

    public DrawPad() {
        super("Draw Pad");
        add(status, BorderLayout.SOUTH);

        addMouseListener(new MouseAdapter() {
            // TODO 1: Override mouseClicked correctly: store the point, update the
            // label and request a repaint
            @Override
            public void mouseClicked(MouseEvent e) {
                points.add(e.getPoint());
                status.setText("Clicks: " + points.size());
                repaint();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                System.exit(0);
            }
        });

        setSize(400, 300);
        setLocationRelativeTo(null);
    }

    @Override
    public void paint(Graphics g) {
        // Clear background / paint borders
        super.paint(g);

        // TODO 2: Draw every stored point as a 6 x 6 filled circle centred on the point
        g.setColor(new Color(27, 54, 93)); // Deep Academic Navy
        for (Point p : points) {
            g.fillOval(p.x - 3, p.y - 3, 6, 6);
        }
    }

    public static void main(String[] args) {
        new DrawPad().setVisible(true);
    }
}
