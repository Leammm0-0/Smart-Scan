package smartscan.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** สร้าง QR PromptPay (มาตรฐาน Thai QR Payment) ตามยอดเงิน */
public class QRCodeGenerator {
    // ===== แก้เฉพาะบรรทัดนี้ =====
    // เบอร์มือถือ 10 หลัก เช่น 0812345678  หรือ เลขบัตรประชาชน 13 หลัก
    private static final String PROMPTPAY_ID = "0616284553";

    /** สร้างรูป QR ขนาด size x size พิกเซล (ถ้าสร้างไม่ได้คืน null) */
    public BufferedImage generate(double amount, int size) {
        try {
            String payload = createPromptPayPayload(PROMPTPAY_ID, amount);
            BitMatrix matrix = new MultiFormatWriter().encode(payload, BarcodeFormat.QR_CODE, size, size);
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    image.setRGB(x, y, matrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
                }
            }
            return image;
        } catch (Exception e) {
            System.err.println("Cannot create PromptPay QR: " + e.getMessage());
            return null;
        }
    }

    public static String createPromptPayPayload(String promptPayId, double amount) {
        String id = promptPayId == null ? "" : promptPayId.replaceAll("[^0-9]", "");
        if (id.isEmpty()) throw new IllegalArgumentException("Please set PROMPTPAY_ID in QRCodeGenerator.java");
        if (amount <= 0) throw new IllegalArgumentException("Amount must be greater than 0");

        String proxy;
        if (id.length() == 10 && id.startsWith("0")) {
            // เบอร์มือถือไทย: 0812345678 -> 0066812345678
            proxy = field("01", "0066" + id.substring(1));
        } else if (id.length() == 13) {
            proxy = field("02", id);
        } else {
            throw new IllegalArgumentException("PromptPay ID must be a 10-digit Thai phone number or 13-digit National ID");
        }

        String merchantAccount = field("00", "A000000677010111") + proxy;
        String payload = field("00", "01")             // Payload Format Indicator
                + field("01", "12")                    // Dynamic QR
                + field("29", merchantAccount)         // PromptPay merchant account info
                + field("53", "764")                   // THB
                + field("54", String.format(Locale.US, "%.2f", amount))
                + field("58", "TH")
                + "6304";                              // CRC ID + length

        return payload + crc16(payload);
    }

    private static String field(String id, String value) {
        int len = value.getBytes(StandardCharsets.UTF_8).length;
        return id + String.format(Locale.US, "%02d", len) + value;
    }

    private static String crc16(String text) {
        int crc = 0xFFFF;
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        for (byte b : bytes) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                crc = (crc & 0x8000) != 0 ? ((crc << 1) ^ 0x1021) : (crc << 1);
                crc &= 0xFFFF;
            }
        }
        return String.format(Locale.US, "%04X", crc);
    }
}
