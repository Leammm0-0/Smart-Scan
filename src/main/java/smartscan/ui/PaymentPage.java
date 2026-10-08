package smartscan.ui;

import smartscan.model.Cart;
import smartscan.service.QRCodeGenerator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;

/** หน้า 4: Payment (QR PromptPay + ตัวนับเวลา) */
public class PaymentPage extends JPanel {
    private static final int QR_SIZE = 260;
    private static final int QR_LIFETIME_SECONDS = 300; // 5 นาที

    private final Cart cart;
    private final QRCodeGenerator qrGenerator;

    private final JLabel amountLabel = Ui.label("฿0", Font.BOLD, 24, Theme.DARK);
    private final JLabel qrLabel = new JLabel("QR", SwingConstants.CENTER);
    private final JLabel timerLabel = Ui.label("QR expires in 5:00 min", Font.BOLD, 12);

    private Timer timer;
    private int secondsLeft = QR_LIFETIME_SECONDS;

    public PaymentPage(Cart cart, QRCodeGenerator qrGenerator, Runnable onBack, Runnable onDone) {
        this.cart = cart;
        this.qrGenerator = qrGenerator;

        setLayout(new BorderLayout());
        setBackground(Theme.PAGE_DARK);
        setBorder(new EmptyBorder(18, 16, 18, 16));

        JPanel card = Ui.roundedPanel(Color.WHITE, 28);
        card.setLayout(new BorderLayout(0, 13));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.add(createTopBar(onBack), BorderLayout.NORTH);
        card.add(createCenter(), BorderLayout.CENTER);

        JButton home = Ui.primaryButton("←  Back to home");
        home.addActionListener(e -> onDone.run());
        card.add(home, BorderLayout.SOUTH);
        add(card, BorderLayout.CENTER);
    }

    // ================= เปิด / ปิดหน้านี้ =================

    /** เรียกตอนเข้าหน้านี้: แสดงยอด สร้าง QR และเริ่มนับเวลา */
    public void start() {
        amountLabel.setText(Ui.money(cart.calculateTotal()));
        refreshQR();
    }

    /** เรียกตอนออกจากหน้านี้: หยุดตัวนับเวลา */
    public void stop() {
        if (timer != null) {
            timer.stop();
        }
    }

    private void refreshQR() {
        BufferedImage image = qrGenerator.generate(cart.calculateTotal(), QR_SIZE);
        qrLabel.setIcon(image == null ? null : new ImageIcon(image));
        qrLabel.setText(image == null ? "QR generation error" : "");
        qrLabel.setEnabled(true);

        secondsLeft = QR_LIFETIME_SECONDS;
        updateTimerText();

        stop();
        timer = new Timer(1000, e -> {
            secondsLeft--;
            updateTimerText();
            if (secondsLeft <= 0) {
                timer.stop();
                timerLabel.setText("QR expired - press Refresh");
                qrLabel.setEnabled(false);
            }
        });
        timer.start();
    }

    private void updateTimerText() {
        int seconds = Math.max(0, secondsLeft);
        timerLabel.setText(String.format("QR expires in %d:%02d min", seconds / 60, seconds % 60));
    }

    // ================= สร้างหน้าตา =================

    private JPanel createTopBar(Runnable onBack) {
        JButton back = Ui.backButton();
        back.addActionListener(e -> onBack.run());

        JLabel title = Ui.label("Payment", Font.BOLD, 19);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(back, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);
        top.add(Box.createHorizontalStrut(38), BorderLayout.EAST);
        return top;
    }

    private JPanel createCenter() {
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel hint = new JLabel("Scan QR or choose a payment method", SwingConstants.CENTER);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        hint.setForeground(Color.GRAY);

        center.add(hint);
        center.add(Box.createVerticalStrut(18));
        center.add(createAmountBox());
        center.add(Box.createVerticalStrut(14));
        center.add(createQrBox());
        center.add(Box.createVerticalStrut(12));
        center.add(createExpiryRow());
        return center;
    }

    /** กล่อง "Pay with QR  ฿xx" */
    private JPanel createAmountBox() {
        JLabel icon = new JLabel("▦");
        icon.setFont(new Font("Dialog", Font.BOLD, 30));
        icon.setForeground(Theme.DARK);

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(Ui.label("Pay with QR", Font.PLAIN, 10, Color.GRAY));
        texts.add(amountLabel);

        JPanel box = Ui.roundedPanel(Theme.SOFT, 15);
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        box.setLayout(new BorderLayout(12, 0));
        box.setBorder(new EmptyBorder(12, 14, 12, 14));
        box.add(icon, BorderLayout.WEST);
        box.add(texts, BorderLayout.CENTER);
        return box;
    }

    /** กล่องใส่รูป QR */
    private JPanel createQrBox() {
        qrLabel.setPreferredSize(new Dimension(QR_SIZE, QR_SIZE));

        JPanel box = Ui.roundedPanel(new Color(252, 252, 250), 18);
        box.setLayout(new BorderLayout());
        box.setBorder(new EmptyBorder(14, 14, 14, 14));
        box.add(qrLabel, BorderLayout.CENTER);

        JLabel bankHint = Ui.label("Open your banking app and scan the QR code to pay", Font.PLAIN, 10, Color.GRAY);
        bankHint.setHorizontalAlignment(SwingConstants.CENTER);
        box.add(bankHint, BorderLayout.SOUTH);
        return box;
    }

    /** แถว "QR expires in ..." + ปุ่ม Refresh */
    private JPanel createExpiryRow() {
        JButton refresh = Ui.smallButton("↻ Refresh");
        refresh.addActionListener(e -> refreshQR());

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36)); // ไม่ให้แถวนี้ถูกยืดสูง
        row.add(timerLabel, BorderLayout.WEST);
        row.add(refresh, BorderLayout.EAST);
        return row;
    }
}
