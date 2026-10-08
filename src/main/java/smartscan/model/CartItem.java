package smartscan.model;

/** สินค้า 1 รายการในตะกร้า = สินค้า + จำนวน */
public class CartItem {
    private final Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    /** ราคารวมของรายการนี้ = ราคาต่อชิ้น x จำนวน */
    public double getSubtotal() {
        return product.getPrice() * quantity;
    }
}
