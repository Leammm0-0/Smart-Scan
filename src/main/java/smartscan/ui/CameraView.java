package smartscan.ui;

import smartscan.service.CameraService;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/** พื้นที่แสดงภาพสดจากกล้อง (วาดภาพล่าสุดของ CameraService ซ้ำทุก ~45 มิลลิวินาที) */
public class CameraView extends JPanel {
    private final CameraService camera;
    private Timer repaintTimer;

    public CameraView(CameraService camera) {
        this.camera = camera;
        setBackground(new Color(22, 28, 26));
    }

    public void startRepaintTimer() {
        if (repaintTimer != null && repaintTimer.isRunning()) {
            return;
        }
        repaintTimer = new Timer(45, e -> repaint());
        repaintTimer.start();
    }

    public void stopRepaintTimer() {
        if (repaintTimer != null) {
            repaintTimer.stop();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        BufferedImage image = camera.getLatestImage();
        if (image == null) {
            g.setColor(new Color(220, 220, 220));
            g.setFont(Theme.font(Font.BOLD, 16));
            String text = "Opening camera...";
            int textWidth = g.getFontMetrics().stringWidth(text);
            g.drawString(text, (getWidth() - textWidth) / 2, getHeight() / 2);
            return;
        }

        // ขยายภาพให้เต็มพื้นที่ (ส่วนที่เกินถูกตัดออก)
        double scale = Math.max(getWidth() / (double) image.getWidth(), getHeight() / (double) image.getHeight());
        int w = (int) (image.getWidth() * scale);
        int h = (int) (image.getHeight() * scale);
        g.drawImage(image, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);

        // ทับสีดำจางๆ ให้กรอบสีเขียวและตัวหนังสืออ่านง่าย
        g.setColor(new Color(0, 0, 0, 70));
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}
