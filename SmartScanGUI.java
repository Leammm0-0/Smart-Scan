import classes.BarcodeScanner;
import classes.CartItem;
import classes.Product;
import classes.ProductCatalog;
import classes.QRCodeGenerator;
import classes.ShoppingCart;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public class SmartScanGUI extends JFrame {
    private static final Color DARK = new Color(8, 73, 52);
    private static final Color DARKER = new Color(5, 56, 40);
    private static final Color GREEN = new Color(36, 214, 157);
    private static final Color BG = new Color(247, 247, 242);
    private static final Color SOFT = new Color(235, 244, 238);
    private static final Font FONT = new Font("Tahoma", Font.PLAIN, 14);

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final ProductCatalog catalog = new ProductCatalog("products.csv");
    private final ShoppingCart cart = new ShoppingCart();
    private final BarcodeScanner scanner = new BarcodeScanner();
    private final QRCodeGenerator qrGenerator = new QRCodeGenerator();

    private JPanel scanItemsPanel;
    private JPanel scanBasketPanel;
    private JScrollPane scanItemsScroll;
    private JPanel scanBottomPanel;
    private JLabel scanCountLabel;
    private JLabel scanTotalLabel;
    private JLabel scannerStatusLabel;
    private CameraPanel cameraPanel;
    private JPanel reviewItemsPanel;
    private JLabel reviewSubtotalLabel;
    private JLabel reviewDiscountLabel;
    private JLabel reviewTotalLabel;
    private JLabel paymentAmountLabel;
    private JLabel qrLabel;
    private JLabel timerLabel;
    private javax.swing.Timer paymentTimer;
    private int secondsLeft = 300;

    public SmartScanGUI() {
        setTitle("Smart Scan - Self Checkout");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(430, 760);
        setMinimumSize(new Dimension(390, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setFont(FONT);

        root.add(createHomePage(), "HOME");
        root.add(createScanPage(), "SCAN");
        root.add(createReviewPage(), "REVIEW");
        root.add(createPaymentPage(), "PAYMENT");
        setContentPane(root);
        cards.show(root, "HOME");

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                scanner.stop();
                if (paymentTimer != null) paymentTimer.stop();
            }
        });
    }

    private JPanel createHomePage() {
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setBackground(BG);
        page.setBorder(new EmptyBorder(26, 18, 18, 18));

        // ===== กล่องวิธีใช้ด้านบน =====
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);

        JPanel intro = roundedPanel(Color.WHITE, 22);
        intro.setLayout(new BoxLayout(intro, BoxLayout.Y_AXIS));
        intro.setBorder(new EmptyBorder(16, 16, 16, 16));
        intro.setPreferredSize(new Dimension(394, 205));
        intro.setMaximumSize(new Dimension(394, 205));

        JLabel title = new JLabel("Scan as you shop");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setFont(new Font("Tahoma", Font.PLAIN, 24));
        title.setForeground(new Color(35, 38, 37));
        intro.add(title);
        intro.add(Box.createVerticalStrut(5));

        JLabel subtitle = new JLabel("<html>Point your camera at a product barcode. We'll<br>add it to your basket instantly.</html>");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setFont(new Font("Tahoma", Font.PLAIN, 12));
        subtitle.setForeground(new Color(100, 105, 102));
        intro.add(subtitle);
        intro.add(Box.createVerticalStrut(12));

        JPanel steps = new JPanel(new GridLayout(1, 2, 8, 0));
        steps.setOpaque(false);
        steps.setAlignmentX(Component.LEFT_ALIGNMENT);
        steps.setMaximumSize(new Dimension(360, 84));
        steps.add(stepCard("icon/home/barcode.png", "01", "Find the barcode", "On any grocery item"));
        steps.add(stepCard("icon/home/scan.png", "02", "Hold it steady", "Keep it inside the frame"));
        intro.add(steps);

        JPanel introWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        introWrap.setOpaque(false);
        introWrap.add(intro);
        center.add(introWrap, BorderLayout.NORTH);
        page.add(center, BorderLayout.CENTER);

        // ===== ส่วนล่าง: ตะกร้า + ปุ่ม Start + Smart Scan =====
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

        JPanel basketInfo = roundedPanel(Color.WHITE, 16);
        basketInfo.setLayout(new BorderLayout(10, 0));
        basketInfo.setBorder(new EmptyBorder(9, 12, 9, 12));
        basketInfo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        basketInfo.add(iconBox("icon/home/cart.png", 27, new Color(229, 248, 239)), BorderLayout.WEST);
        JLabel text = new JLabel("<html><b>Your basket is empty</b><br><span style='color:#777777'>Scan your first item to get started</span></html>");
        text.setFont(new Font("Tahoma", Font.PLAIN, 11));
        basketInfo.add(text, BorderLayout.CENTER);

        JButton startButton = primaryIconButton("Start scanning", "icon/home/scan2.png");
        startButton.addActionListener(e -> openScanner());
        startButton.setPreferredSize(new Dimension(330, 48));
        startButton.setBackground(new Color(8, 73, 52)); // สีเขียว
        startButton.setForeground(Color.WHITE);           // ตัวหนังสือขาว
        startButton.setOpaque(true);
        startButton.setContentAreaFilled(true);
        startButton.setBorderPainted(false);

        JPanel brandRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        brandRow.setOpaque(false);
        JLabel brand = new JLabel("<html><span style='color:#24D69D'>●</span> <span style='color:#111111'>Smart Scan</span></html>");
        brand.setFont(new Font("Tahoma", Font.PLAIN, 13));
        brandRow.add(brand);

        bottom.add(basketInfo);
        bottom.add(Box.createVerticalStrut(10));

        JPanel startButtonRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        startButtonRow.setOpaque(false);
        startButtonRow.add(startButton);

        bottom.add(startButtonRow);
        bottom.add(Box.createVerticalStrut(34));
        bottom.add(brandRow);
        page.add(bottom, BorderLayout.SOUTH);
        return page;
    }

    private JPanel stepCard(String iconPath, String no, String title, String sub) {
        JPanel p = roundedPanel(new Color(246, 246, 239), 14);
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.setLayout(new BorderLayout(0, 4));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(iconBox(iconPath, 26, new Color(229, 248, 239)), BorderLayout.WEST);
        JLabel n = new JLabel(no);
        n.setFont(new Font("Tahoma", Font.BOLD, 10));
        n.setForeground(DARK);
        top.add(n, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        JPanel words = new JPanel();
        words.setOpaque(false);
        words.setLayout(new BoxLayout(words, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Tahoma", Font.BOLD, 10));
        JLabel subLabel = new JLabel(sub);
        subLabel.setFont(new Font("Tahoma", Font.PLAIN, 9));
        subLabel.setForeground(Color.GRAY);
        words.add(t);
        words.add(subLabel);
        p.add(words, BorderLayout.SOUTH);
        return p;
    }

    private JPanel createScanPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(new Color(16, 25, 22));

        cameraPanel = new CameraPanel();
        cameraPanel.setLayout(new BorderLayout());

        // ===== แถบบน: close / title / help =====
        JPanel overlayTop = new JPanel(new BorderLayout());
        overlayTop.setOpaque(false);
        overlayTop.setBorder(new EmptyBorder(16, 16, 4, 16));

        JButton close = roundIconButton("icon/scan/close.png");
        close.addActionListener(e -> backHome());
        overlayTop.add(close, BorderLayout.WEST);

        JLabel scanTitle = new JLabel("<html><div style='text-align:center;color:white'><b>Smart Scan</b><br><span style='font-size:9px'>Point at a barcode to add it</span></div></html>", SwingConstants.CENTER);
        scanTitle.setFont(new Font("Tahoma", Font.BOLD, 17));
        overlayTop.add(scanTitle, BorderLayout.CENTER);

        JButton help = roundIconButton("icon/scan/Help.png");
        help.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "นำบาร์โค้ดสินค้าให้อยู่ภายในกรอบสีเขียว\nถือให้นิ่งจนระบบอ่านสำเร็จ",
                "วิธีสแกน", JOptionPane.INFORMATION_MESSAGE));
        overlayTop.add(help, BorderLayout.EAST);
        cameraPanel.add(overlayTop, BorderLayout.NORTH);

        // ===== กรอบ Scan + Scanner ready =====
        JPanel scanCenter = new JPanel(new GridBagLayout());
        scanCenter.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(30, 0, 10, 0);

        JPanel guide = roundedBorderPanel(GREEN, 3, 26);
        guide.setPreferredSize(new Dimension(250, 124));
        scanCenter.add(guide, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 35, 0);
        scannerStatusLabel = statusPill("Scanner ready");
        scanCenter.add(scannerStatusLabel, gbc);
        cameraPanel.add(scanCenter, BorderLayout.CENTER);

        page.add(cameraPanel, BorderLayout.CENTER);
        page.add(createScanBasketPanel(), BorderLayout.SOUTH);
        return page;
    }

    private JPanel createScanBasketPanel() {
        scanBasketPanel = roundedPanel(Color.WHITE, 26);
        scanBasketPanel.setPreferredSize(new Dimension(430, 92));
        scanBasketPanel.setLayout(new BorderLayout(0, 8));
        scanBasketPanel.setBorder(new EmptyBorder(12, 15, 12, 15));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel t = new JLabel("Scanned items");
        t.setFont(new Font("Tahoma", Font.PLAIN, 17));
        JLabel s = new JLabel("Items in your basket");
        s.setForeground(Color.GRAY);
        s.setFont(new Font("Tahoma", Font.PLAIN, 10));
        titles.add(t);
        titles.add(s);
        head.add(titles, BorderLayout.WEST);
        scanCountLabel = badge("0 items");
        head.add(scanCountLabel, BorderLayout.EAST);
        scanBasketPanel.add(head, BorderLayout.NORTH);

        scanItemsPanel = new JPanel();
        scanItemsPanel.setBackground(Color.WHITE);
        scanItemsPanel.setLayout(new BoxLayout(scanItemsPanel, BoxLayout.Y_AXIS));
        scanItemsScroll = new JScrollPane(scanItemsPanel);
        scanItemsScroll.setBorder(null);
        scanItemsScroll.getVerticalScrollBar().setUnitIncrement(16);
        scanItemsScroll.setVisible(false);
        scanBasketPanel.add(scanItemsScroll, BorderLayout.CENTER);

        scanBottomPanel = new JPanel(new BorderLayout(10, 0));
        scanBottomPanel.setOpaque(false);
        JPanel total = new JPanel();
        total.setOpaque(false);
        total.setLayout(new BoxLayout(total, BoxLayout.Y_AXIS));
        JLabel run = new JLabel("RUNNING TOTAL");
        run.setFont(new Font("Tahoma", Font.PLAIN, 9));
        run.setForeground(Color.GRAY);
        scanTotalLabel = new JLabel("฿0");
        scanTotalLabel.setForeground(DARK);
        scanTotalLabel.setFont(new Font("Tahoma", Font.BOLD, 22));
        total.add(run);
        total.add(scanTotalLabel);
        scanBottomPanel.add(total, BorderLayout.WEST);

        JButton review = primaryButton("Review Order  →");
        review.setPreferredSize(new Dimension(155, 44));
        review.addActionListener(e -> openReview());
        scanBottomPanel.add(review, BorderLayout.EAST);
        scanBottomPanel.setVisible(false);
        scanBasketPanel.add(scanBottomPanel, BorderLayout.SOUTH);
        return scanBasketPanel;
    }

    private JPanel createReviewPage() {
        JPanel page = new JPanel(new BorderLayout(0, 12));
        page.setBackground(new Color(10, 45, 34));
        page.setBorder(new EmptyBorder(20, 16, 20, 16));

        JPanel card = roundedPanel(Color.WHITE, 28);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JButton back = circleButton("←");
        back.setForeground(new Color(40, 40, 40));
        back.setBackground(new Color(245, 245, 245));
        back.addActionListener(e -> openScanner());
        top.add(back, BorderLayout.WEST);
        JLabel title = new JLabel("Review Order", SwingConstants.CENTER);
        title.setFont(new Font("Tahoma", Font.BOLD, 19));
        top.add(title, BorderLayout.CENTER);
        top.add(Box.createHorizontalStrut(38), BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        JLabel yourItems = new JLabel("<html><b>Your Items</b><br><span style='color:#777777;font-size:9px'>Check your items before payment</span></html>");
        center.add(yourItems, BorderLayout.NORTH);
        reviewItemsPanel = new JPanel();
        reviewItemsPanel.setBackground(Color.WHITE);
        reviewItemsPanel.setLayout(new BoxLayout(reviewItemsPanel, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(reviewItemsPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        center.add(scroll, BorderLayout.CENTER);
        card.add(center, BorderLayout.CENTER);

        JPanel lower = new JPanel();
        lower.setOpaque(false);
        lower.setLayout(new BoxLayout(lower, BoxLayout.Y_AXIS));
        JPanel summary = roundedPanel(new Color(250, 250, 248), 16);
        summary.setLayout(new GridLayout(3, 2, 4, 8));
        summary.setBorder(new EmptyBorder(14, 14, 14, 14));
        summary.add(new JLabel("Subtotal"));
        reviewSubtotalLabel = rightLabel("฿0"); summary.add(reviewSubtotalLabel);
        summary.add(new JLabel("Discount"));
        reviewDiscountLabel = rightLabel("-฿0.00"); reviewDiscountLabel.setForeground(new Color(200, 50, 50)); summary.add(reviewDiscountLabel);
        JLabel totalText = new JLabel("Total"); totalText.setFont(new Font("Tahoma", Font.BOLD, 14)); summary.add(totalText);
        reviewTotalLabel = rightLabel("฿0"); reviewTotalLabel.setForeground(DARK); reviewTotalLabel.setFont(new Font("Tahoma", Font.BOLD, 17)); summary.add(reviewTotalLabel);
        lower.add(summary);
        lower.add(Box.createVerticalStrut(16));
        JButton pay = primaryButton("Continue to payment  →");
        pay.addActionListener(e -> openPayment());
        lower.add(pay);
        card.add(lower, BorderLayout.SOUTH);
        page.add(card, BorderLayout.CENTER);
        return page;
    }

    private JPanel createPaymentPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(new Color(10, 45, 34));
        page.setBorder(new EmptyBorder(18, 16, 18, 16));

        JPanel card = roundedPanel(Color.WHITE, 28);
        card.setLayout(new BorderLayout(0, 13));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JButton back = circleButton("←");
        back.setForeground(new Color(40, 40, 40));
        back.setBackground(new Color(245,245,245));
        back.addActionListener(e -> { if (paymentTimer != null) paymentTimer.stop(); openReview(); });
        top.add(back, BorderLayout.WEST);
        JLabel title = new JLabel("Payment", SwingConstants.CENTER);
        title.setFont(new Font("Tahoma", Font.BOLD, 19));
        top.add(title, BorderLayout.CENTER);
        top.add(Box.createHorizontalStrut(38), BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        JLabel hint = new JLabel("Scan QR or choose a payment method", SwingConstants.CENTER);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        hint.setForeground(Color.GRAY);
        center.add(hint);
        center.add(Box.createVerticalStrut(18));

        JPanel amount = roundedPanel(SOFT, 15);
        amount.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        amount.setLayout(new BorderLayout(12, 0));
        amount.setBorder(new EmptyBorder(12, 14, 12, 14));
        JLabel qi = new JLabel("▦"); qi.setFont(new Font("Dialog", Font.BOLD, 30)); qi.setForeground(DARK);
        amount.add(qi, BorderLayout.WEST);
        JPanel at = new JPanel(); at.setOpaque(false); at.setLayout(new BoxLayout(at, BoxLayout.Y_AXIS));
        JLabel p = new JLabel("Pay with QR"); p.setFont(new Font("Tahoma", Font.PLAIN, 10)); p.setForeground(Color.GRAY);
        paymentAmountLabel = new JLabel("฿0"); paymentAmountLabel.setFont(new Font("Tahoma", Font.BOLD, 24)); paymentAmountLabel.setForeground(DARK);
        at.add(p); at.add(paymentAmountLabel);
        amount.add(at, BorderLayout.CENTER);
        center.add(amount);
        center.add(Box.createVerticalStrut(14));

        JPanel qrBox = roundedPanel(new Color(252,252,250), 18);
        qrBox.setLayout(new BorderLayout());
        qrBox.setBorder(new EmptyBorder(14, 14, 14, 14));
        qrLabel = new JLabel("QR", SwingConstants.CENTER);
        qrLabel.setPreferredSize(new Dimension(260,260));
        qrBox.add(qrLabel, BorderLayout.CENTER);
        JLabel bankHint = new JLabel("Open your banking app and scan the QR code to pay", SwingConstants.CENTER);
        bankHint.setFont(new Font("Tahoma", Font.PLAIN, 10));
        bankHint.setForeground(Color.GRAY);
        qrBox.add(bankHint, BorderLayout.SOUTH);
        center.add(qrBox);
        center.add(Box.createVerticalStrut(12));

        JPanel expiry = new JPanel(new BorderLayout());
        expiry.setOpaque(false);
        timerLabel = new JLabel("QR expires in 5:00 min");
        timerLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
        expiry.add(timerLabel, BorderLayout.WEST);
        JButton refresh = smallButton("↻ Refresh");
        refresh.addActionListener(e -> refreshPaymentQR());
        expiry.add(refresh, BorderLayout.EAST);
        center.add(expiry);
        card.add(center, BorderLayout.CENTER);

        JButton home = primaryButton("←  Back to home");
        home.addActionListener(e -> {
            if (paymentTimer != null) paymentTimer.stop();
            cart.clear();
            refreshScanItems();
            cards.show(root, "HOME");
        });
        card.add(home, BorderLayout.SOUTH);
        page.add(card, BorderLayout.CENTER);
        return page;
    }

    private void openScanner() {
        if (paymentTimer != null) paymentTimer.stop();
        cards.show(root, "SCAN");
        refreshScanItems();
        cameraPanel.startRepaintTimer();
        scanner.start(new BarcodeScanner.ScanListener() {
            @Override public void onScanned(String barcode) {
                SwingUtilities.invokeLater(() -> addByBarcode(barcode));
            }
            @Override public void onStatus(String message) {
                SwingUtilities.invokeLater(() -> setStatusText(message));
            }
        });
    }

    private void backHome() {
        scanner.stop();
        cameraPanel.stopRepaintTimer();
        cards.show(root, "HOME");
    }

    private void addByBarcode(String barcode) {
        Product p = catalog.findByBarcode(barcode);
        if (p == null) {
            setStatusText("ไม่พบสินค้า: " + barcode);
            return;
        }
        cart.addItem(p);
        setStatusText("เพิ่ม " + p.getName() + " แล้ว");
        refreshScanItems();
    }

    private void refreshScanItems() {
        if (scanItemsPanel == null) return;
        scanItemsPanel.removeAll();
        List<CartItem> items = cart.getItems();
        boolean hasItems = !items.isEmpty();

        if (hasItems) {
            for (CartItem item : items) {
                scanItemsPanel.add(createItemRow(item, true));
            }
        }

        scanCountLabel.setText(cart.getTotalQuantity() + " items");
        scanTotalLabel.setText(money(cart.getTotal()));

        // ถ้ายังไม่มีสินค้า: เหลือเฉพาะหัว Scanned items + 0 items
        scanItemsScroll.setVisible(hasItems);
        scanBottomPanel.setVisible(hasItems);
        scanBasketPanel.setPreferredSize(new Dimension(430, hasItems ? 290 : 92));

        scanItemsPanel.revalidate();
        scanItemsPanel.repaint();
        scanBasketPanel.revalidate();
        scanBasketPanel.repaint();
        root.revalidate();
    }

    private JPanel createItemRow(CartItem item, boolean compact) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(new EmptyBorder(8, 4, 8, 4));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, compact ? 72 : 84));

        JLabel pic = new JLabel();
        pic.setPreferredSize(new Dimension(compact ? 48 : 56, compact ? 48 : 56));
        pic.setHorizontalAlignment(SwingConstants.CENTER);
        pic.setIcon(loadProductIcon(item.getProduct(), compact ? 45 : 53, compact ? 45 : 53));
        if (pic.getIcon() == null) {
            pic.setText("IMG"); pic.setOpaque(true); pic.setBackground(new Color(240,240,240));
        }
        row.add(pic, BorderLayout.WEST);

        JPanel info = new JPanel(); info.setOpaque(false); info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel n = new JLabel(item.getProduct().getName()); n.setFont(new Font("Tahoma", Font.BOLD, 12));
        JLabel d = new JLabel(item.getProduct().getDescription()); d.setFont(new Font("Tahoma", Font.PLAIN, 9)); d.setForeground(Color.GRAY);
        JLabel price = new JLabel(money(item.getProduct().getPrice())); price.setFont(new Font("Tahoma", Font.BOLD, 12));
        info.add(n); info.add(d); info.add(price);
        row.add(info, BorderLayout.CENTER);

        JPanel qty = roundedPanel(new Color(246,248,245), 14);
        qty.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 4));
        JButton minus = tinyButton("−");
        JLabel q = new JLabel(String.valueOf(item.getQuantity())); q.setFont(new Font("Tahoma", Font.BOLD, 12));
        JButton plus = tinyButton("+");
        minus.addActionListener(e -> { cart.decrease(item.getProduct().getBarcode()); refreshScanItems(); refreshReview(); });
        plus.addActionListener(e -> { cart.increase(item.getProduct().getBarcode()); refreshScanItems(); refreshReview(); });
        qty.add(minus); qty.add(q); qty.add(plus);
        row.add(qty, BorderLayout.EAST);
        return row;
    }

    private void openReview() {
        scanner.stop();
        cameraPanel.stopRepaintTimer();
        refreshReview();
        cards.show(root, "REVIEW");
    }

    private void refreshReview() {
        if (reviewItemsPanel == null) return;
        reviewItemsPanel.removeAll();
        if (cart.getItems().isEmpty()) {
            JLabel empty = new JLabel("ไม่มีสินค้า", SwingConstants.CENTER);
            empty.setForeground(Color.GRAY);
            empty.setBorder(new EmptyBorder(30,0,30,0));
            reviewItemsPanel.add(empty);
        } else {
            for (CartItem item : cart.getItems()) reviewItemsPanel.add(createItemRow(item, false));
        }
        reviewSubtotalLabel.setText(money(cart.getSubtotal()));
        reviewDiscountLabel.setText("-" + money(cart.getDiscount()));
        reviewTotalLabel.setText(money(cart.getTotal()));
        reviewItemsPanel.revalidate();
        reviewItemsPanel.repaint();
    }

    private void openPayment() {
        if (cart.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "กรุณาสแกนสินค้าอย่างน้อย 1 ชิ้นก่อนชำระเงิน");
            return;
        }
        scanner.stop();
        cameraPanel.stopRepaintTimer();
        paymentAmountLabel.setText(money(cart.getTotal()));
        refreshPaymentQR();
        cards.show(root, "PAYMENT");
    }

    private void refreshPaymentQR() {
        BufferedImage img = qrGenerator.generate(cart.getTotal(), 260);
        qrLabel.setIcon(img == null ? null : new ImageIcon(img));
        qrLabel.setText(img == null ? "QR generation error" : "");
        secondsLeft = 300;
        updateTimerText();
        if (paymentTimer != null) paymentTimer.stop();
        paymentTimer = new javax.swing.Timer(1000, e -> {
            secondsLeft--;
            updateTimerText();
            if (secondsLeft <= 0) {
                paymentTimer.stop();
                timerLabel.setText("QR expired - press Refresh");
                qrLabel.setEnabled(false);
            }
        });
        qrLabel.setEnabled(true);
        paymentTimer.start();
    }

    private void updateTimerText() {
        int m = Math.max(0, secondsLeft) / 60;
        int s = Math.max(0, secondsLeft) % 60;
        timerLabel.setText(String.format("QR expires in %d:%02d min", m, s));
    }

    // ===== Helper สำหรับรูปไอคอน GUI =====
    private ImageIcon loadUiIcon(String path, int w, int h) {
        try {
            File f = new File(path);
            if (!f.exists()) return null;
            BufferedImage image = ImageIO.read(f);
            if (image == null) return null;
            Image scaled = image.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            return null;
        }
    }

    private JPanel iconBox(String path, int iconSize, Color bg) {
        JPanel box = roundedPanel(bg, 9);
        box.setPreferredSize(new Dimension(34, 34));
        box.setMinimumSize(new Dimension(34, 34));
        box.setMaximumSize(new Dimension(34, 34));
        box.setLayout(new GridBagLayout());
        JLabel icon = new JLabel(loadUiIcon(path, iconSize, iconSize));
        box.add(icon);
        return box;
    }

    private JButton primaryIconButton(String text, String iconPath) {
        JButton b = primaryButton(text);
        b.setIcon(loadUiIcon(iconPath, 19, 19));
        b.setIconTextGap(8);
        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        return b;
    }

    private JButton roundIconButton(String iconPath) {
        JButton b = new JButton(loadUiIcon(iconPath, 16, 16)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(250, 250, 250, 235));
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setPreferredSize(new Dimension(34, 34));
        b.setMinimumSize(new Dimension(34, 34));
        b.setMaximumSize(new Dimension(34, 34));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(0, 0, 0, 0));
        return b;
    }

    private JPanel roundedBorderPanel(Color borderColor, int thickness, int radius) {
        return new JPanel() {
            { setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(thickness));
                int inset = thickness;
                g2.drawRoundRect(inset, inset, getWidth() - inset * 2 - 1, getHeight() - inset * 2 - 1, radius, radius);
                g2.dispose();
            }
        };
    }

    private JLabel statusPill(String text) {
        JLabel label = new JLabel("<html><span style='color:#24D69D'>●</span> <span style='color:white'>" + text + "</span></html>", SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(7, 66, 47, 225));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        label.setOpaque(false);
        label.setPreferredSize(new Dimension(132, 28));
        label.setMinimumSize(new Dimension(132, 28));
        label.setMaximumSize(new Dimension(170, 28));
        label.setFont(new Font("Tahoma", Font.PLAIN, 10));
        return label;
    }

    private void setStatusText(String text) {
        if (scannerStatusLabel != null) {
            scannerStatusLabel.setText("<html><span style='color:#24D69D'>●</span> <span style='color:white'>" + text + "</span></html>");
        }
    }

    private ImageIcon loadProductIcon(Product p, int w, int h) {
        try {
            File f = new File(p.getImagePath());
            if (!f.exists()) return null;
            BufferedImage image = ImageIO.read(f);
            if (image == null) return null;
            Image scaled = image.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            return null;
        }
    }

    private String money(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.001) return "฿" + (int)Math.rint(value);
        return String.format("฿%.2f", value);
    }

    private JPanel roundedPanel(Color color, int radius) {
        return new JPanel() {
            { setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),radius,radius);
                g2.dispose();
                super.paintComponent(g);
            }
        };
    }

    private JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Tahoma", Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setBackground(DARK);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(13, 16, 13, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton circleButton(String text) {
        JButton b = new JButton(text);
        b.setPreferredSize(new Dimension(38,38));
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(20, 32, 28));
        b.setFocusPainted(false);
        b.setMargin(new Insets(0,0,0,0));
        return b;
    }

    private JButton smallButton(String text) {
        JButton b = new JButton(text);
        b.setForeground(DARK);
        b.setBackground(SOFT);
        b.setFocusPainted(false);
        return b;
    }

    private JButton tinyButton(String text) {
        JButton b = new JButton(text);
        b.setPreferredSize(new Dimension(27,25));
        b.setMargin(new Insets(0,0,0,0));
        b.setFocusPainted(false);
        return b;
    }

    private JLabel badge(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setOpaque(true);
        l.setBackground(new Color(229, 248, 239));
        l.setForeground(DARK);
        l.setFont(new Font("Tahoma", Font.BOLD, 10));
        l.setBorder(new EmptyBorder(6,10,6,10));
        return l;
    }

    private JLabel rightLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.RIGHT);
        return l;
    }

    private class CameraPanel extends JPanel {
        private javax.swing.Timer repaintTimer;
        CameraPanel() { setBackground(new Color(22,28,26)); }
        void startRepaintTimer() {
            if (repaintTimer != null && repaintTimer.isRunning()) return;
            repaintTimer = new javax.swing.Timer(45, e -> repaint());
            repaintTimer.start();
        }
        void stopRepaintTimer() { if (repaintTimer != null) repaintTimer.stop(); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            BufferedImage image = scanner.getLatestImage();
            if (image == null) {
                g.setColor(new Color(220,220,220));
                g.setFont(new Font("Tahoma", Font.BOLD, 16));
                String text = "Opening camera...";
                int tw = g.getFontMetrics().stringWidth(text);
                g.drawString(text, (getWidth()-tw)/2, getHeight()/2);
                return;
            }
            double scale = Math.max(getWidth() / (double)image.getWidth(), getHeight() / (double)image.getHeight());
            int w = (int)(image.getWidth() * scale);
            int h = (int)(image.getHeight() * scale);
            int x = (getWidth() - w) / 2;
            int y = (getHeight() - h) / 2;
            g.drawImage(image, x, y, w, h, null);
            g.setColor(new Color(0,0,0,70));
            g.fillRect(0,0,getWidth(),getHeight());
        }
    }
}
