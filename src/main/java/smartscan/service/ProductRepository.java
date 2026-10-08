package smartscan.repository;

import smartscan.model.Product;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ที่เก็บข้อมูลสินค้า โหลดจากไฟล์ CSV
 * รูปแบบ: id,barcode,name,price,description,stock,imagePath
 */
public class ProductRepository {
    private final Map<String, Product> products = new LinkedHashMap<>();

    /** โหลดสินค้าจากไฟล์ CSV ถ้าไม่พบไฟล์/อ่านไม่ได้จะ throw IOException */
    public void loadFromCsv(Path path) throws IOException {
        products.clear();

        for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            line = line.replace("\uFEFF", "").trim(); // ตัด BOM + ช่องว่าง

            if (line.isEmpty() || line.startsWith("id,")) {
                continue; // ข้ามบรรทัดว่างและหัวตาราง
            }

            String[] data = line.split(",", -1); // หมายเหตุ: ห้ามมีเครื่องหมาย , อยู่ในชื่อ/คำอธิบาย
            if (data.length < 7) {
                continue;
            }

            try {
                Product product = new Product(
                        Integer.parseInt(data[0].trim()),
                        data[1].trim(),
                        data[2].trim(),
                        Double.parseDouble(data[3].trim()),
                        data[4].trim(),
                        Integer.parseInt(data[5].trim()),
                        data[6].trim());
                products.put(product.getBarcode(), product);
            } catch (NumberFormatException ex) {
                System.out.println("ข้ามบรรทัดที่ข้อมูลไม่ถูกต้อง: " + line);
            }
        }
    }

    /** คืน null ถ้าไม่พบสินค้า */
    public Product findByBarcode(String barcode) {
        if (barcode == null) {
            return null;
        }
        return products.get(barcode.trim());
    }

    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }
}
