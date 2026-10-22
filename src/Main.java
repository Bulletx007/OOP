import ui.GamePanel;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
           JFrame f = new JFrame("PIXEL METEOR");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            GamePanel p = new GamePanel();
            f.add(p);
            f.pack();
            f.setLocationRelativeTo(null);   // เอา c: ออก
            f.setVisible(true);              // เอา b: ออก
            p.requestFocusInWindow();

        });
    }
}