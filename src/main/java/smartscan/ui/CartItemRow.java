package smartscan.ui;

import smartscan.model.Cart;
import smartscan.model.CartItem;
import smartscan.model.Product;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;

/** แถวสินค้า 1 รายการ (รูป + ชื่อ + ราคา + ปุ่ม − จำนวน +) ใช้ทั้งหน้า Scan และ Review */
public final class CartItemRow {
    private CartItemRow() {
    }

    /**
     * @param compact   true = แถวเล็ก (หน้า Scan), false = แถวใหญ่ (หน้า Review)
     * @param onChanged ถูกเรียกหลังกดปุ่ม +/− เพื่อให้หน้านั้นวาดรายการใหม่
     */
    public static JPanel create(Cart cart, CartItem item, boolean compact, Runnable onChanged) {
        Product product = item.getProduct();

        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(new EmptyBorder(8, 4, 8, 4));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, compact ? 72 : 84));

        row.add(createPicture(product, compact), BorderLayout.WEST);
        row.add(createInfo(product), BorderLayout.CENTER);

        // ครอบด้วย GridBagLayout เพื่อให้กล่องปรับจำนวนอยู่กลางแถวในแนวตั้ง ไม่ถูกยืดสูง
        JPanel east = new JPanel(new GridBagLayout());
        east.setOpaque(false);
        east.add(createQuantityBox(cart, item, onChanged));
        row.add(east, BorderLayout.EAST);
        return row;
    }

    private static JLabel createPicture(Product product, boolean compact) {
        int box = compact ? 48 : 56;
        int image = compact ? 45 : 53;

        JLabel pic = new JLabel();
        pic.setPreferredSize(new Dimension(box, box));
        pic.setHorizontalAlignment(SwingConstants.CENTER);
        pic.setIcon(Ui.loadIcon(product.getImagePath(), image, image));
        if (pic.getIcon() == null) {
            pic.setText("IMG");
            pic.setOpaque(true);
            pic.setBackground(new Color(240, 240, 240));
        }
        return pic;
    }

    private static JPanel createInfo(Product product) {
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.add(Ui.label(product.getName(), Font.BOLD, 12));
        info.add(Ui.label(product.getDescription(), Font.PLAIN, 9, Color.GRAY));
        info.add(Ui.label(Ui.money(product.getPrice()), Font.BOLD, 12));
        return info;
    }

    private static JPanel createQuantityBox(Cart cart, CartItem item, Runnable onChanged) {
        Product product = item.getProduct();

        JButton minus = Ui.tinyButton("−");
        JButton plus = Ui.tinyButton("+");
        JLabel quantity = Ui.label(String.valueOf(item.getQuantity()), Font.BOLD, 12);

        minus.addActionListener(e -> {
            cart.updateQuantity(product, item.getQuantity() - 1); // ถ้าเหลือ 0 Cart จะลบรายการให้เอง
            onChanged.run();
        });
        plus.addActionListener(e -> {
            cart.updateQuantity(product, item.getQuantity() + 1);
            onChanged.run();
        });

        JPanel box = Ui.roundedPanel(new Color(246, 248, 245), 14);
        box.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 4));
        box.add(minus);
        box.add(quantity);
        box.add(plus);
        return box;
    }
}
