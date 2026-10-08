package smartscan.model;

/** สินค้า 1 ชนิด (ข้อมูลมาจากไฟล์ products.csv) */
public class Product {
    private final int id;
    private final String barcode;
    private final String name;
    private final double price;
    private final String description;
    private int stock;
    private final String imagePath;

    public Product(int id, String barcode, String name, double price,
                   String description, int stock, String imagePath) {
        this.id = id;
        this.barcode = barcode;
        this.name = name;
        this.price = price;
        this.description = description;
        this.stock = stock;
        this.imagePath = imagePath;
    }

    public int getId() { return id; }
    public String getBarcode() { return barcode; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }
    public int getStock() { return stock; }
    public String getImagePath() { return imagePath; }

    public boolean isAvailable() { return stock > 0; }
    public void updateStock(int quantity) { stock += quantity; }
    public String getProductInfo() { return name + " - ฿" + String.format("%.2f", price); }
}
