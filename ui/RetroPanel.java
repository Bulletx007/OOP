package ui;

import javax.swing.*;
import java.awt.*;

public abstract class RetroPanel extends JPanel {

    public RetroPanel() {
        setPreferredSize(new Dimension(GB.W * GB.SCALE, GB.H * GB.SCALE));
        setBackground(GB.DARKEST);
        setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // ★ ปิดการmoothing เพื่อให้ฟอนต์และพิกเซลมีความคมกริบสไตล์เรโทรแท้ๆ
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        g2.scale(GB.SCALE, GB.SCALE);

        // สร้าง Buffer ภาพจำลองขนาด 160x144
        Image buffer = createImage(GB.W, GB.H);
        Graphics2D bg = (Graphics2D) buffer.getGraphics();

        drawPixels(bg);
        bg.dispose();

        g2.drawImage(buffer, 0, 0, this);
    }

    protected abstract void drawPixels(Graphics2D g);

    // ปรับแต่งฟอนต์ให้ตัวหนาและชัดเจนขึ้น
    protected void text(Graphics2D g, String s, int x, int y, Color c) {
        g.setFont(new Font("Monospaced", Font.BOLD, 9)); // เพิ่มขนาดเป็น 9 และตัวหนา
        g.setColor(c);
        g.drawString(s, x, y);
    }

    protected void textCenter(Graphics2D g, String s, int y, Color c) {
        g.setFont(new Font("Monospaced", Font.BOLD, 9)); // เพิ่มขนาดเป็น 9 และตัวหนา
        FontMetrics fm = g.getFontMetrics();
        g.setColor(c);
        g.drawString(s, (GB.W - fm.stringWidth(s)) / 2, y);
    }
}