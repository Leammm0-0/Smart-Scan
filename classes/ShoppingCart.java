package classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShoppingCart {
    private final List<CartItem> items = new ArrayList<>();

    public void addItem(Product product) {
        addItem(product, 1);
    }

    public void addItem(Product product, int quantity) {
        if (product == null || quantity <= 0) return;
        CartItem existing = find(product.getBarcode());
        if (existing == null) items.add(new CartItem(product, quantity));
        else existing.setQuantity(existing.getQuantity() + quantity);
    }

    public void increase(String barcode) {
        CartItem item = find(barcode);
        if (item != null) item.setQuantity(item.getQuantity() + 1);
    }

    public void decrease(String barcode) {
        CartItem item = find(barcode);
        if (item == null) return;
        item.setQuantity(item.getQuantity() - 1);
        if (item.getQuantity() <= 0) items.remove(item);
    }

    public CartItem find(String barcode) {
        for (CartItem item : items) {
            if (item.getProduct().getBarcode().equals(barcode)) return item;
        }
        return null;
    }

    public List<CartItem> getItems() { return Collections.unmodifiableList(items); }
    public int getTotalQuantity() {
        int total = 0;
        for (CartItem item : items) total += item.getQuantity();
        return total;
    }
    public double getSubtotal() {
        double total = 0;
        for (CartItem item : items) total += item.getSubtotal();
        return total;
    }
    public double getDiscount() { return 0.0; }
    public double getTotal() { return Math.max(0, getSubtotal() - getDiscount()); }
    public void clear() { items.clear(); }
}
