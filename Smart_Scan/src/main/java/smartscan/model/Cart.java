package smartscan.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** ตะกร้าสินค้า เก็บ CartItem หลายรายการ */
public class Cart {
    private final List<CartItem> items = new ArrayList<>();

    /** เพิ่มสินค้า ถ้ามีในตะกร้าแล้วจะบวกจำนวนเพิ่ม */
    public void addItem(Product product, int quantity) {
        if (product == null || quantity <= 0) {
            return;
        }
        CartItem existing = findItem(product);
        if (existing == null) {
            items.add(new CartItem(product, quantity));
        } else {
            existing.setQuantity(existing.getQuantity() + quantity);
        }
    }

    public void removeItem(Product product) {
        items.removeIf(item -> item.getProduct().getBarcode().equals(product.getBarcode()));
    }

    /** ตั้งจำนวนใหม่ ถ้าจำนวน <= 0 จะลบรายการนั้นออก */
    public void updateQuantity(Product product, int quantity) {
        if (quantity <= 0) {
            removeItem(product);
            return;
        }
        CartItem existing = findItem(product);
        if (existing != null) {
            existing.setQuantity(quantity);
        }
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        items.clear();
    }

    /** จำนวนชิ้นทั้งหมด (นับรวมทุกรายการ) */
    public int getTotalQuantity() {
        int total = 0;
        for (CartItem item : items) {
            total += item.getQuantity();
        }
        return total;
    }

    /** ราคารวมก่อนหักส่วนลด */
    public double getSubtotal() {
        double total = 0;
        for (CartItem item : items) {
            total += item.getSubtotal();
        }
        return total;
    }

    /** ส่วนลด (ตอนนี้ยังเป็น 0 ถ้าจะเพิ่มโปรโมชั่น ให้แก้ตรงนี้ที่เดียว) */
    public double getDiscount() {
        return 0.0;
    }

    /** ยอดที่ต้องจ่ายจริง = ราคารวม - ส่วนลด */
    public double calculateTotal() {
        return Math.max(0, getSubtotal() - getDiscount());
    }

    private CartItem findItem(Product product) {
        for (CartItem item : items) {
            if (item.getProduct().getBarcode().equals(product.getBarcode())) {
                return item;
            }
        }
        return null;
    }
}
