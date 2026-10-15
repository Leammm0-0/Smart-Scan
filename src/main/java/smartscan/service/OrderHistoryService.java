package smartscan.service;

import smartscan.model.Cart;
import smartscan.model.CartItem;
import smartscan.model.Product;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** บันทึกประวัติออเดอร์ทั้งหมดต่อกันไว้ในไฟล์เดียว */
public class OrderHistoryService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter ORDER_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final Path historyFile;

    public OrderHistoryService(Path historyFile) {
        this.historyFile = historyFile;
    }

    /**
     * เพิ่มออเดอร์ใหม่ต่อท้ายไฟล์เดิม โดยไม่ลบประวัติเก่า
     * ควรเรียก method นี้ก่อน cart.clear()
     */
    public void saveOrder(Cart cart) throws IOException {
        if (cart == null || cart.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        StringBuilder text = new StringBuilder();

        text.append("============================================================\n");
        text.append("Order ID : ").append(now.format(ORDER_ID_FORMAT)).append('\n');
        text.append("Date     : ").append(now.format(DATE_FORMAT)).append('\n');
        text.append("Time     : ").append(now.format(TIME_FORMAT)).append('\n');
        text.append("Items\n");

        int number = 1;
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            text.append(String.format(
                    "%d. %s | ฿%.2f x %d = ฿%.2f%n",
                    number++,
                    product.getName(),
                    product.getPrice(),
                    item.getQuantity(),
                    item.getSubtotal()));
        }

        text.append(String.format("Subtotal : ฿%.2f%n", cart.getSubtotal()));
        text.append(String.format("Discount : ฿%.2f%n", cart.getDiscount()));
        text.append(String.format("Total    : ฿%.2f%n", cart.calculateTotal()));
        text.append("============================================================\n\n");

        Files.writeString(
                historyFile,
                text.toString(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }

    public Path getHistoryFile() {
        return historyFile;
    }
}
