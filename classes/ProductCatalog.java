package classes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ProductCatalog {
    private final List<Product> products = new ArrayList<>();

    public ProductCatalog(String csvPath) {
        load(csvPath);
    }

    private void load(String csvPath) {
        File file = new File(csvPath);
        if (!file.exists()) return;
        try (BufferedReader br = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; }
                if (line.isBlank()) continue;
                String[] p = splitCsv(line);
                if (p.length < 7) continue;
                products.add(new Product(
                        Integer.parseInt(p[0].trim()),
                        p[1].trim(),
                        p[2].trim(),
                        Double.parseDouble(p[3].trim()),
                        p[4].trim(),
                        Integer.parseInt(p[5].trim()),
                        p[6].trim()
                ));
            }
        } catch (Exception e) {
            System.err.println("Cannot read products.csv: " + e.getMessage());
        }
    }

    private String[] splitCsv(String line) {
        // Simple CSV for this student project: avoid commas inside a field.
        return line.split(",", -1);
    }

    public Product findByBarcode(String barcode) {
        if (barcode == null) return null;
        for (Product p : products) {
            if (p.getBarcode().equals(barcode.trim())) return p;
        }
        return null;
    }

    public List<Product> getProducts() { return new ArrayList<>(products); }
}
