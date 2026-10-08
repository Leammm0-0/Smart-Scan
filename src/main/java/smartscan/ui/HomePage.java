package smartscan.ui;

import smartscan.model.Cart;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

/** หน้า 1: Home (วิธีใช้ + ปุ่ม Start scanning) */
public class HomePage extends JPanel {
    private final Cart cart;
    private final JLabel basketText = Ui.label("", Font.PLAIN, 11);

    public HomePage(Cart cart, Runnable onStart) {
        this.cart = cart;

        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(26, 18, 18, 18));

        add(createIntroSection(), BorderLayout.CENTER);
        add(createBottomSection(onStart), BorderLayout.SOUTH);
        refresh();
    }

    /** อัปเดตกล่องตะกร้าด้านล่าง (เรียกทุกครั้งที่กลับมาหน้านี้) */
    public void refresh() {
        if (cart.isEmpty()) {
            basketText.setText("<html><b>Your basket is empty</b><br>"
                    + "<span style='color:#777777'>Scan your first item to get started</span></html>");
        } else {
            basketText.setText("<html><b>" + cart.getTotalQuantity() + " items in your basket</b><br>"
                    + "<span style='color:#777777'>Total " + Ui.money(cart.calculateTotal()) + "</span></html>");
        }
    }

    // ===== กล่องวิธีใช้ด้านบน =====
    private JPanel createIntroSection() {
        JPanel intro = Ui.roundedPanel(Color.WHITE, 22);
        intro.setLayout(new BoxLayout(intro, BoxLayout.Y_AXIS));
        intro.setBorder(new EmptyBorder(16, 16, 16, 16));
        intro.setPreferredSize(new Dimension(394, 205));
        intro.setMaximumSize(new Dimension(394, 205));

        JLabel title = Ui.label("Scan as you shop", Font.PLAIN, 24, new Color(35, 38, 37));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = Ui.label(
                "<html>Point your camera at a product barcode. We'll<br>add it to your basket instantly.</html>",
                Font.PLAIN, 12, new Color(100, 105, 102));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel steps = new JPanel(new GridLayout(1, 2, 8, 0));
        steps.setOpaque(false);
        steps.setAlignmentX(Component.LEFT_ALIGNMENT);
        steps.setMaximumSize(new Dimension(360, 84));
        steps.add(createStepCard("icon/home/barcode.png", "01", "Find the barcode", "On any grocery item"));
        steps.add(createStepCard("icon/home/scan.png", "02", "Hold it steady", "Keep it inside the frame"));

        intro.add(title);
        intro.add(Box.createVerticalStrut(5));
        intro.add(subtitle);
        intro.add(Box.createVerticalStrut(12));
        intro.add(steps);

        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrap.setOpaque(false);
        wrap.add(intro);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(wrap, BorderLayout.NORTH);
        return center;
    }

    private JPanel createStepCard(String iconPath, String number, String title, String subtitle) {
        JPanel card = Ui.roundedPanel(new Color(246, 246, 239), 14);
        card.setBorder(new EmptyBorder(10, 10, 10, 10));
        card.setLayout(new BorderLayout(0, 4));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(Ui.iconBox(iconPath, 26, new Color(229, 248, 239)), BorderLayout.WEST);
        top.add(Ui.label(number, Font.BOLD, 10, Theme.DARK), BorderLayout.EAST);

        JPanel words = new JPanel();
        words.setOpaque(false);
        words.setLayout(new BoxLayout(words, BoxLayout.Y_AXIS));
        words.add(Ui.label(title, Font.BOLD, 10));
        words.add(Ui.label(subtitle, Font.PLAIN, 9, Color.GRAY));

        card.add(top, BorderLayout.NORTH);
        card.add(words, BorderLayout.SOUTH);
        return card;
    }

    // ===== ส่วนล่าง: กล่องตะกร้า + ปุ่ม Start + ชื่อแอป =====
    private JPanel createBottomSection(Runnable onStart) {
        JPanel basketInfo = Ui.roundedPanel(Color.WHITE, 16);
        basketInfo.setLayout(new BorderLayout(10, 0));
        basketInfo.setBorder(new EmptyBorder(9, 12, 9, 12));
        basketInfo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        basketInfo.add(Ui.iconBox("icon/home/cart.png", 27, new Color(229, 248, 239)), BorderLayout.WEST);
        basketInfo.add(basketText, BorderLayout.CENTER);

        JButton startButton = Ui.primaryIconButton("Start scanning", "icon/home/scan2.png");
        startButton.setPreferredSize(new Dimension(330, 48));
        startButton.addActionListener(e -> onStart.run());

        JPanel startRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        startRow.setOpaque(false);
        startRow.add(startButton);

        JPanel brandRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        brandRow.setOpaque(false);
        brandRow.add(Ui.label("<html><span style='color:#24D69D'>●</span> <span style='color:#111111'>Smart Scan</span></html>",
                Font.PLAIN, 13));

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.add(basketInfo);
        bottom.add(Box.createVerticalStrut(10));
        bottom.add(startRow);
        bottom.add(Box.createVerticalStrut(34));
        bottom.add(brandRow);
        return bottom;
    }
}
