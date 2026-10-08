package smartscan.ui;

import smartscan.model.Cart;
import smartscan.repository.ProductRepository;
import smartscan.service.CameraService;
import smartscan.service.QRCodeGenerator;
import smartscan.service.SmartScanner;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * หน้าต่างหลัก: สร้างข้อมูล/บริการ แล้วประกอบ 4 หน้าเข้าด้วยกัน
 * และมี method showXxx() สำหรับสลับหน้า (ทุกการเปลี่ยนหน้าผ่านที่นี่ที่เดียว)
 */
public class MainFrame extends JFrame {
    private static final Path PRODUCT_CSV = Paths.get("products.csv");

    private static final String HOME = "HOME";
    private static final String SCAN = "SCAN";
    private static final String REVIEW = "REVIEW";
    private static final String PAYMENT = "PAYMENT";

    // ---- ข้อมูลและบริการ ----
    private final ProductRepository repository = new ProductRepository();
    private final Cart cart = new Cart();
    private final SmartScanner smartScanner = new SmartScanner(repository, cart);
    private final CameraService camera = new CameraService();
    private final QRCodeGenerator qrGenerator = new QRCodeGenerator();

    // ---- 4 หน้า ----
    private final HomePage homePage = new HomePage(cart, this::showScan);
    private final ScanPage scanPage = new ScanPage(cart, smartScanner, camera, this::showHome, this::showReview);
    private final ReviewPage reviewPage = new ReviewPage(cart, this::showScan, this::showPayment);
    private final PaymentPage paymentPage = new PaymentPage(cart, qrGenerator, this::showReview, this::finishPayment);

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    public MainFrame() {
        setTitle("Smart Scan - Self Checkout");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(430, 760);
        setMinimumSize(new Dimension(390, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG);

        root.add(homePage, HOME);
        root.add(scanPage, SCAN);
        root.add(reviewPage, REVIEW);
        root.add(paymentPage, PAYMENT);
        setContentPane(root);

        loadProducts();
        cards.show(root, HOME);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                scanPage.stop();
                paymentPage.stop();
            }
        });
    }

    private void loadProducts() {
        try {
            repository.loadFromCsv(PRODUCT_CSV);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "อ่านไฟล์ products.csv ไม่สำเร็จ\nหาที่: " + PRODUCT_CSV.toAbsolutePath()
                            + "\n\n(ต้องรันโปรแกรมจากโฟลเดอร์หลักของโปรเจกต์)",
                    "Product file error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================= สลับหน้า =================

    public void showHome() {
        scanPage.stop();
        paymentPage.stop();
        homePage.refresh();
        cards.show(root, HOME);
    }

    public void showScan() {
        paymentPage.stop();
        scanPage.start();
        cards.show(root, SCAN);
    }

    public void showReview() {
        scanPage.stop();
        paymentPage.stop();
        reviewPage.refresh();
        cards.show(root, REVIEW);
    }

    public void showPayment() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "กรุณาสแกนสินค้าอย่างน้อย 1 ชิ้นก่อนชำระเงิน");
            return;
        }
        scanPage.stop();
        paymentPage.start();
        cards.show(root, PAYMENT);
    }

    /** กด "Back to home" ในหน้า Payment = จบการซื้อ: ล้างตะกร้าแล้วกลับหน้าแรก */
    private void finishPayment() {
        cart.clear();
        scanPage.refresh();
        showHome();
    }
}
