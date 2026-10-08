package smartscan.ui;

import smartscan.model.Cart;
import smartscan.model.CartItem;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.Box;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

/** หน้า 3: Review Order (ตรวจรายการ + สรุปยอด) */
public class ReviewPage extends JPanel {
    private final Cart cart;

    private final JPanel itemsPanel = new JPanel();
    private final JLabel subtotalLabel = new JLabel("฿0", SwingConstants.RIGHT);
    private final JLabel discountLabel = new JLabel("-฿0", SwingConstants.RIGHT);
    private final JLabel totalLabel = new JLabel("฿0", SwingConstants.RIGHT);

    public ReviewPage(Cart cart, Runnable onBack, Runnable onPay) {
        this.cart = cart;

        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.PAGE_DARK);
        setBorder(new EmptyBorder(20, 16, 20, 16));

        JPanel card = Ui.roundedPanel(Color.WHITE, 28);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.add(createTopBar(onBack), BorderLayout.NORTH);
        card.add(createItemList(), BorderLayout.CENTER);
        card.add(createSummary(onPay), BorderLayout.SOUTH);
        add(card, BorderLayout.CENTER);
    }

    /** วาดรายการและยอดรวมใหม่ตามตะกร้า (เรียกทุกครั้งที่เข้าหน้านี้) */
    public void refresh() {
        itemsPanel.removeAll();

        if (cart.isEmpty()) {
            JLabel empty = new JLabel("ไม่มีสินค้า", SwingConstants.CENTER);
            empty.setForeground(Color.GRAY);
            empty.setBorder(new EmptyBorder(30, 0, 30, 0));
            itemsPanel.add(empty);
        } else {
            for (CartItem item : cart.getItems()) {
                itemsPanel.add(CartItemRow.create(cart, item, false, this::refresh));
            }
        }

        subtotalLabel.setText(Ui.money(cart.getSubtotal()));
        discountLabel.setText("-" + Ui.money(cart.getDiscount()));
        totalLabel.setText(Ui.money(cart.calculateTotal()));

        itemsPanel.revalidate();
        itemsPanel.repaint();
    }

    private JPanel createTopBar(Runnable onBack) {
        JButton back = Ui.backButton();
        back.addActionListener(e -> onBack.run());

        JLabel title = Ui.label("Review Order", Font.BOLD, 19);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(back, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);
        top.add(Box.createHorizontalStrut(38), BorderLayout.EAST); // ดันชื่อให้อยู่กลางพอดี
        return top;
    }

    private JPanel createItemList() {
        JLabel heading = new JLabel("<html><b>Your Items</b><br>"
                + "<span style='color:#777777;font-size:9px'>Check your items before payment</span></html>");

        itemsPanel.setBackground(Color.WHITE);
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(itemsPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(heading, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);
        return center;
    }

    /** กล่องสรุป Subtotal / Discount / Total + ปุ่ม Continue to payment */
    private JPanel createSummary(Runnable onPay) {
        discountLabel.setForeground(new Color(200, 50, 50));
        totalLabel.setForeground(Theme.DARK);
        totalLabel.setFont(Theme.font(Font.BOLD, 17));

        JLabel totalText = new JLabel("Total");
        totalText.setFont(Theme.font(Font.BOLD, 14));

        JPanel summary = Ui.roundedPanel(new Color(250, 250, 248), 16);
        summary.setLayout(new GridLayout(3, 2, 4, 8));
        summary.setBorder(new EmptyBorder(14, 14, 14, 14));
        summary.add(new JLabel("Subtotal"));
        summary.add(subtotalLabel);
        summary.add(new JLabel("Discount"));
        summary.add(discountLabel);
        summary.add(totalText);
        summary.add(totalLabel);

        JButton pay = Ui.primaryButton("Continue to payment  →");
        pay.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48)); // ให้ปุ่มเต็มความกว้าง
        pay.addActionListener(e -> onPay.run());

        JPanel lower = new JPanel();
        lower.setOpaque(false);
        lower.setLayout(new BoxLayout(lower, BoxLayout.Y_AXIS));
        lower.add(summary);
        lower.add(Box.createVerticalStrut(16));
        lower.add(pay);
        return lower;
    }
}
