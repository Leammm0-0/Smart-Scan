package smartscan.service;

import smartscan.model.Cart;
import smartscan.model.Product;
import smartscan.repository.ProductRepository;

/** รับ barcode (ข้อความ) -> ค้นหาสินค้า -> ใส่ตะกร้า */
public class SmartScanner {
    private final ProductRepository repository;
    private final Cart cart;

    public SmartScanner(ProductRepository repository, Cart cart) {
        this.repository = repository;
        this.cart = cart;
    }

    /** สแกน barcode: ถ้าเจอสินค้าจะเพิ่มลงตะกร้า 1 ชิ้น แล้วคืนสินค้านั้น (ไม่เจอคืน null) */
    public Product scanBarcode(String barcode) {
        Product product = searchProduct(barcode);
        if (product != null) {
            addToCart(product, 1);
        }
        return product;
    }

    public Product searchProduct(String barcode) {
        return repository.findByBarcode(barcode);
    }

    public void addToCart(Product product, int quantity) {
        cart.addItem(product, quantity);
    }
}
