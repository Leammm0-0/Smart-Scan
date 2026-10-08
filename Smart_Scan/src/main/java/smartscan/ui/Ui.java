package smartscan.ui;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;

/** ตัวช่วยสร้าง component หน้าตาเดียวกันทั้งแอป (ปุ่มโค้งมน, กล่องโค้งมน, ป้ายสถานะ ฯลฯ) */
public final class Ui {
    private Ui() {
    }

    // ================= ข้อความ / รูป =================

    /** แสดงเงิน: ถ้าเป็นจำนวนเต็มไม่โชว์ทศนิยม เช่น ฿40 หรือ ฿12.50 */
    public static String money(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.001) {
            return "฿" + (int) Math.rint(value);
        }
        return String.format("฿%.2f", value);
    }

    public static JLabel label(String text, int style, int size) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.font(style, size));
        return label;
    }

    public static JLabel label(String text, int style, int size, Color color) {
        JLabel label = label(text, style, size);
        label.setForeground(color);
        return label;
    }

    /** โหลดรูปจากไฟล์แล้วย่อขนาด (ไม่พบไฟล์คืน null) */
    public static ImageIcon loadIcon(String path, int w, int h) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                return null;
            }
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                return null;
            }
            return new ImageIcon(image.getScaledInstance(w, h, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return null;
        }
    }

    // ================= กล่องโค้งมน =================

    private static class RoundedPanel extends JPanel {
        private final Color color;
        private final int radius;

        RoundedPanel(Color color, int radius) {
            this.color = color;
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedBorderPanel extends JPanel {
        private final Color color;
        private final int thickness;
        private final int radius;

        RoundedBorderPanel(Color color, int thickness, int radius) {
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(thickness, thickness, getWidth() - thickness * 2 - 1, getHeight() - thickness * 2 - 1, radius, radius);
            g2.dispose();
        }
    }

    /** กล่องพื้นทึบโค้งมน */
    public static JPanel roundedPanel(Color color, int radius) {
        return new RoundedPanel(color, radius);
    }

    /** กรอบโค้งมน (โปร่งใสข้างใน) ใช้เป็นกรอบสแกน */
    public static JPanel roundedBorderPanel(Color borderColor, int thickness, int radius) {
        return new RoundedBorderPanel(borderColor, thickness, radius);
    }

    /** กล่องสี่เหลี่ยมเล็กๆ ใส่ไอคอน */
    public static JPanel iconBox(String path, int iconSize, Color bg) {
        JPanel box = roundedPanel(bg, 9);
        box.setPreferredSize(new Dimension(34, 34));
        box.setMinimumSize(new Dimension(34, 34));
        box.setMaximumSize(new Dimension(34, 34));
        box.setLayout(new GridBagLayout());
        box.add(new JLabel(loadIcon(path, iconSize, iconSize)));
        return box;
    }

    // ================= ปุ่ม =================

    /**
     * ปุ่มที่วาดสีพื้นเอง ทำให้สีขึ้นเหมือนกันทั้ง Mac และ Windows
     * (ปุ่ม Swing ปกติบน Mac จะไม่ยอมเปลี่ยนสีพื้น)
     */
    private static class RoundedButton extends JButton {
        private final int radius;

        RoundedButton(String text, Color bg, Color fg, int radius) {
            super(text);
            this.radius = radius;
            setBackground(bg);
            setForeground(fg);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            // ล้างขอบ/ระยะเว้นเดิมของปุ่มบน Mac ที่กินพื้นที่จนข้อความถูกตัดเป็น "..." (ปุ่มที่ต้องการขอบเองจะตั้งทับทีหลัง)
            setBorder(BorderFactory.createEmptyBorder());
            setMargin(new Insets(0, 0, 0, 0));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color bg = getBackground();
            g2.setColor(isEnabled() ? bg : new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 110));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** ปุ่มหลักสีเขียวเข้ม ตัวอักษรขาว */
    public static JButton primaryButton(String text) {
        JButton button = new RoundedButton(text, Theme.DARK, Color.WHITE, 16);
        button.setFont(Theme.font(Font.BOLD, 13));
        button.setBorder(new EmptyBorder(13, 16, 13, 16));
        return button;
    }

    public static JButton primaryIconButton(String text, String iconPath) {
        JButton button = primaryButton(text);
        button.setIcon(loadIcon(iconPath, 19, 19));
        button.setIconTextGap(8);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        return button;
    }

    /** ปุ่มกลมสีขาว มีไอคอน (ปุ่มปิด / ปุ่มช่วยเหลือบนหน้า Scan) */
    public static JButton roundIconButton(String iconPath) {
        JButton button = new RoundedButton("", new Color(250, 250, 250, 235), Color.BLACK, 34);
        button.setIcon(loadIcon(iconPath, 16, 16));
        button.setPreferredSize(new Dimension(34, 34));
        button.setMinimumSize(new Dimension(34, 34));
        button.setMaximumSize(new Dimension(34, 34));
        button.setMargin(new Insets(0, 0, 0, 0));
        return button;
    }

    /** ปุ่มกลมสีเทาอ่อน ใส่ข้อความสั้นๆ เช่น "←" (ปุ่มย้อนกลับ) */
    public static JButton backButton() {
        JButton button = new RoundedButton("←", new Color(245, 245, 245), new Color(40, 40, 40), 38);
        button.setPreferredSize(new Dimension(38, 38));
        button.setMargin(new Insets(0, 0, 0, 0));
        return button;
    }

    public static JButton smallButton(String text) {
        JButton button = new RoundedButton(text, Theme.SOFT, Theme.DARK, 14);
        button.setBorder(new EmptyBorder(6, 12, 6, 12));
        return button;
    }

    /** ปุ่มเล็กๆ + / − ปรับจำนวนสินค้า */
    public static JButton tinyButton(String text) {
        JButton button = new RoundedButton(text, new Color(222, 234, 226), Theme.DARK, 10);
        button.setFont(Theme.font(Font.BOLD, 13));
        button.setPreferredSize(new Dimension(27, 25));
        button.setMargin(new Insets(0, 0, 0, 0));
        return button;
    }

    // ================= ป้ายต่างๆ =================

    /** ป้ายเล็กๆ สีเขียวอ่อน เช่น "3 items" */
    public static JLabel badge(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(new Color(229, 248, 239));
        label.setForeground(Theme.DARK);
        label.setFont(Theme.font(Font.BOLD, 10));
        label.setBorder(new EmptyBorder(6, 10, 6, 10));
        return label;
    }

    /** ป้ายสถานะสีเขียวเข้มใต้กรอบสแกน เช่น "● Scanner ready" (เปลี่ยนข้อความด้วย setPillText) */
    public static JLabel statusPill(String text) {
        JLabel label = new JLabel("", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(7, 66, 47, 225));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                return new Dimension(Math.max(132, d.width), 28); // กว้างขึ้นอัตโนมัติถ้าข้อความยาว
            }
        };
        label.setOpaque(false);
        label.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        label.setFont(Theme.font(Font.PLAIN, 10));
        setPillText(label, text);
        return label;
    }

    public static void setPillText(JLabel pill, String text) {
        pill.setText("<html><span style='color:#24D69D'>●</span> <span style='color:white'>" + text + "</span></html>");
    }
}
