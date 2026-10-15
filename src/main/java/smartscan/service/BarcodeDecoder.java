package smartscan.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** อ่าน barcode จากรูปภาพ 1 ภาพ (ใช้ ZXing) */
public class BarcodeDecoder {
    private final MultiFormatReader reader = new MultiFormatReader();

    public BarcodeDecoder() {
        List<BarcodeFormat> formats = Arrays.asList(
                BarcodeFormat.EAN_13, BarcodeFormat.EAN_8,
                BarcodeFormat.UPC_A, BarcodeFormat.UPC_E,
                BarcodeFormat.CODE_128, BarcodeFormat.CODE_39, BarcodeFormat.CODE_93,
                BarcodeFormat.ITF, BarcodeFormat.QR_CODE);

        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, formats);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        reader.setHints(hints);
    }

    /** คืนข้อความใน barcode หรือ null ถ้าอ่านไม่ได้ */
    public String decode(BufferedImage image) {
        try {
            BufferedImage enlarged = enlarge(cropCenter(image));
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(enlarged)));

            Result result = reader.decodeWithState(bitmap);
            String text = result.getText();
            return (text == null || text.isBlank()) ? null : text;

        } catch (NotFoundException e) {
            return null; // ไม่เจอ barcode ในภาพนี้ (เกิดบ่อยตามปกติ)
        } catch (Exception e) {
            System.out.println("Scan error: " + e.getMessage());
            return null;
        } finally {
            reader.reset();
        }
    }

    /** ตัดเอาเฉพาะกรอบกลางภาพ (ตรงกับกรอบสีเขียวบนหน้าจอ) */
    private BufferedImage cropCenter(BufferedImage image) {
        int width = (int) (image.getWidth() * 0.75);
        int height = (int) (image.getHeight() * 0.50);
        int x = (image.getWidth() - width) / 2;
        int y = (image.getHeight() - height) / 2;
        return image.getSubimage(x, y, width, height);
    }

    /** ขยายภาพ 2 เท่า ช่วยให้อ่าน barcode เล็กๆ ได้ดีขึ้น */
    private BufferedImage enlarge(BufferedImage image) {
        BufferedImage result = new BufferedImage(image.getWidth() * 2, image.getHeight() * 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = result.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.drawImage(image, 0, 0, result.getWidth(), result.getHeight(), null);
        g2.dispose();
        return result;
    }
}
