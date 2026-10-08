package smartscan.ui;

import smartscan.model.Cart;
import smartscan.model.CartItem;
import smartscan.model.Product;
import smartscan.service.CameraService;
import smartscan.service.SmartScanner;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/** หน้า 2: Scan (ภาพกล้อง + กรอบสแกน + ตะกร้าด้านล่าง) */
public class ScanPage extends JPanel {
    /** barcode เดิมที่ค้างอยู่หน้ากล้อง จะถูกนับซ้ำได้อีกครั้งหลังผ่านไปกี่มิลลิวินาที */
    private static final long SAME_BARCODE_DELAY_MS = 1800;

    private final Cart cart;
    private final SmartScanner smartScanner;
    private final CameraService camera;
    private final CameraView cameraView;

    private final JPanel itemsPanel = new JPanel();
    private final JScrollPane itemsScroll = new JScrollPane(itemsPanel);
    private final JPanel basketPanel = Ui.roundedPanel(Color.WHITE, 26);
    private final JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
    private final JLabel countLabel = Ui.badge("0 items");
    private final JLabel totalLabel = Ui.label("฿0", Font.BOLD, 22, Theme.DARK);
    private final JLabel statusPill = Ui.statusPill("Scanner ready");

    // ช่องพิมพ์ barcode เอง (โผล่เฉพาะตอนกล้องใช้ไม่ได้)
    private final JPanel manualEntry = new JPanel(new BorderLayout());
    private final JTextField manualField = new JTextField();

    private String lastBarcode = "";
    private long lastScanTime = 0;

    public ScanPage(Cart cart, SmartScanner smartScanner, CameraService camera,
                    Runnable onClose, Runnable onReview) {
        this.cart = cart;
        this.smartScanner = smartScanner;
        this.camera = camera;
        this.cameraView = new CameraView(camera);

        setLayout(new BorderLayout());
        setBackground(Theme.CAMERA_BG);

        cameraView.setLayout(new BorderLayout());
        cameraView.add(createTopBar(onClose), BorderLayout.NORTH);
        cameraView.add(createGuideFrame(), BorderLayout.CENTER);
        cameraView.add(createManualEntry(), BorderLayout.SOUTH);

        add(cameraView, BorderLayout.CENTER);
        add(createBasketPanel(onReview), BorderLayout.SOUTH);
    }

    // ================= เปิด / ปิดหน้านี้ =================

    /** เรียกตอนเข้าหน้านี้: เปิดกล้องและเริ่มสแกน */
    public void start() {
        refresh();
        Ui.setPillText(statusPill, "Opening camera...");
        lastBarcode = "";
        showManualEntry(false);
        cameraView.startRepaintTimer();

        camera.start(new CameraService.Listener() {
            @Override
            public void onBarcode(String barcode) {
                SwingUtilities.invokeLater(() -> handleBarcode(barcode));
            }

            @Override
            public void onStatus(String message) {
                SwingUtilities.invokeLater(() -> Ui.setPillText(statusPill, message));
            }

            @Override
            public void onCameraFailed() {
                SwingUtilities.invokeLater(() -> showManualEntry(true));
            }
        });
    }

    /** เรียกตอนออกจากหน้านี้: ปิดกล้อง */
    public void stop() {
        camera.stop();
        cameraView.stopRepaintTimer();
    }

    // ================= สแกนเจอ barcode =================

    private void handleBarcode(String barcode) {
        if (!camera.isRunning()) {
            return; // ออกจากหน้านี้ไปแล้ว ไม่รับ barcode ที่ค้างมา
        }

        long now = System.currentTimeMillis();
        if (barcode.equals(lastBarcode) && now - lastScanTime < SAME_BARCODE_DELAY_MS) {
            return; // barcode เดิมที่ยังค้างอยู่หน้ากล้อง ไม่เพิ่มซ้ำ
        }
        lastBarcode = barcode;
        lastScanTime = now;

        Product product = smartScanner.scanBarcode(barcode);
        if (product == null) {
            Ui.setPillText(statusPill, "ไม่พบสินค้า: " + barcode);
            return;
        }

        Ui.setPillText(statusPill, "เพิ่ม " + product.getName() + " แล้ว");
        refresh();
    }

    // ================= พิมพ์ barcode เอง (ใช้ตอนกล้องใช้ไม่ได้) =================

    public void showManualEntry(boolean visible) {
        manualEntry.setVisible(visible);
        if (visible) {
            manualField.requestFocusInWindow();
        }
        revalidate();
    }

    /** เพิ่มสินค้าจากเลข barcode ที่พิมพ์ (ทำงานเหมือนสแกนเจอ) */
    private void addManually() {
        String barcode = manualField.getText().trim();
        if (barcode.isEmpty()) {
            return;
        }

        Product product = smartScanner.scanBarcode(barcode);
        if (product == null) {
            Ui.setPillText(statusPill, "ไม่พบสินค้า: " + barcode);
        } else {
            Ui.setPillText(statusPill, "เพิ่ม " + product.getName() + " แล้ว");
            refresh();
        }

        manualField.setText("");
        manualField.requestFocusInWindow();
    }

    private JPanel createManualEntry() {
        JButton add = Ui.primaryButton("เพิ่ม");
        add.setPreferredSize(new Dimension(70, 38));
        add.addActionListener(e -> addManually());
        manualField.addActionListener(e -> addManually()); // กด Enter ก็เพิ่มได้

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.add(manualField, BorderLayout.CENTER);
        row.add(add, BorderLayout.EAST);

        JPanel box = Ui.roundedPanel(new Color(255, 255, 255, 235), 20);
        box.setLayout(new BorderLayout(0, 8));
        box.setBorder(new EmptyBorder(12, 14, 12, 14));
        box.add(Ui.label("กล้องใช้ไม่ได้ - พิมพ์เลข Barcode แทนได้", Font.BOLD, 11, Theme.DARK), BorderLayout.NORTH);
        box.add(row, BorderLayout.CENTER);

        manualEntry.setOpaque(false);
        manualEntry.setBorder(new EmptyBorder(0, 16, 12, 16));
        manualEntry.add(box, BorderLayout.CENTER);
        manualEntry.setVisible(false);
        return manualEntry;
    }

    // ================= วาดตะกร้าใหม่ =================

    public void refresh() {
        itemsPanel.removeAll();

        boolean hasItems = !cart.isEmpty();
        for (CartItem item : cart.getItems()) {
            itemsPanel.add(CartItemRow.create(cart, item, true, this::refresh));
        }

        countLabel.setText(cart.getTotalQuantity() + " items");
        totalLabel.setText(Ui.money(cart.calculateTotal()));

        // ถ้ายังไม่มีสินค้า: เหลือเฉพาะหัว "Scanned items" + "0 items"
        itemsScroll.setVisible(hasItems);
        bottomPanel.setVisible(hasItems);
        basketPanel.setPreferredSize(new Dimension(430, hasItems ? 290 : 92));

        itemsPanel.revalidate();
        itemsPanel.repaint();
        basketPanel.revalidate();
        basketPanel.repaint();
        revalidate();
    }

    // ================= สร้างหน้าตา =================

    /** แถบบน: ปุ่มปิด / ชื่อ / ปุ่มช่วยเหลือ */
    private JPanel createTopBar(Runnable onClose) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(16, 16, 4, 16));

        JButton close = Ui.roundIconButton("icon/scan/close.png");
        close.addActionListener(e -> onClose.run());

        JLabel title = Ui.label(
                "<html><div style='text-align:center;color:white'><b>Smart Scan</b><br>"
                        + "<span style='font-size:9px'>Point at a barcode to add it</span></div></html>",
                Font.BOLD, 17);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JButton help = Ui.roundIconButton("icon/scan/Help.png");
        help.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "นำบาร์โค้ดสินค้าให้อยู่ภายในกรอบสีเขียว\nถือให้นิ่งจนระบบอ่านสำเร็จ",
                "วิธีสแกน", JOptionPane.INFORMATION_MESSAGE));

        bar.add(close, BorderLayout.WEST);
        bar.add(title, BorderLayout.CENTER);
        bar.add(help, BorderLayout.EAST);
        return bar;
    }

    /** กรอบสีเขียวกลางจอ + ป้าย Scanner ready */
    private JPanel createGuideFrame() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(30, 0, 10, 0);

        JPanel guide = Ui.roundedBorderPanel(Theme.GREEN, 3, 26);
        guide.setPreferredSize(new Dimension(250, 124));
        center.add(guide, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 35, 0);
        center.add(statusPill, gbc);
        return center;
    }

    /** กล่องตะกร้าสีขาวด้านล่าง */
    private JPanel createBasketPanel(Runnable onReview) {
        basketPanel.setPreferredSize(new Dimension(430, 92));
        basketPanel.setLayout(new BorderLayout(0, 8));
        basketPanel.setBorder(new EmptyBorder(12, 15, 12, 15));

        // หัวกล่อง
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(Ui.label("Scanned items", Font.PLAIN, 17));
        titles.add(Ui.label("Items in your basket", Font.PLAIN, 10, Color.GRAY));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(titles, BorderLayout.WEST);
        head.add(countLabel, BorderLayout.EAST);
        basketPanel.add(head, BorderLayout.NORTH);

        // รายการสินค้า
        itemsPanel.setBackground(Color.WHITE);
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsScroll.setBorder(null);
        itemsScroll.getVerticalScrollBar().setUnitIncrement(16);
        itemsScroll.setVisible(false);
        basketPanel.add(itemsScroll, BorderLayout.CENTER);

        // ยอดรวม + ปุ่ม Review Order
        JPanel total = new JPanel();
        total.setOpaque(false);
        total.setLayout(new BoxLayout(total, BoxLayout.Y_AXIS));
        total.add(Ui.label("RUNNING TOTAL", Font.PLAIN, 9, Color.GRAY));
        total.add(totalLabel);

        JButton review = Ui.primaryButton("Review Order  →");
        review.setPreferredSize(new Dimension(155, 44));
        review.addActionListener(e -> onReview.run());

        bottomPanel.setOpaque(false);
        bottomPanel.add(total, BorderLayout.WEST);
        bottomPanel.add(review, BorderLayout.EAST);
        bottomPanel.setVisible(false);
        basketPanel.add(bottomPanel, BorderLayout.SOUTH);
        return basketPanel;
    }
}
