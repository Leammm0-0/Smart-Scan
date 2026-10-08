package smartscan.ui;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Enumeration;

/** สีและฟอนต์ของทั้งแอป (อยากเปลี่ยนสีธีม/ฟอนต์ แก้ที่ไฟล์นี้ที่เดียว) */
public final class Theme {
    public static final Color DARK = new Color(8, 73, 52);          // เขียวเข้ม (ปุ่ม/ตัวเลขสำคัญ)
    public static final Color GREEN = new Color(36, 214, 157);      // เขียวสว่าง (กรอบสแกน)
    public static final Color BG = new Color(247, 247, 242);        // พื้นหลังหน้า Home
    public static final Color SOFT = new Color(235, 244, 238);      // เขียวอ่อน
    public static final Color PAGE_DARK = new Color(10, 45, 34);    // พื้นหลังหน้า Review / Payment
    public static final Color CAMERA_BG = new Color(16, 25, 22);    // พื้นหลังหน้า Scan

    // ฟอนต์ที่รองรับภาษาไทย เรียงตามลำดับที่อยากใช้ (ตัวแรกที่เครื่องมี จะถูกเลือก)
    private static final String[] PREFERRED_FONTS =
            {"Tahoma", "Leelawadee UI", "Thonburi", "Nirmala UI", "Arial"};

    private static String fontFamily = Font.SANS_SERIF;

    private Theme() {
    }

    /** สร้างฟอนต์ตามธีม เช่น Theme.font(Font.BOLD, 14) */
    public static Font font(int style, int size) {
        return new Font(fontFamily, style, size);
    }

    /** เลือกฟอนต์ไทยแล้วตั้งเป็นฟอนต์เริ่มต้นของ Swing (เรียกครั้งเดียวตอนเริ่มโปรแกรม) */
    public static void installFonts() {
        fontFamily = findThaiFont();

        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                Font old = (Font) value;
                UIManager.put(key, new FontUIResource(
                        new Font(fontFamily, old.getStyle(), Math.max(old.getSize(), 14))));
            }
        }
    }

    private static String findThaiFont() {
        String[] installed = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String want : PREFERRED_FONTS) {
            for (String have : installed) {
                if (have.equalsIgnoreCase(want)) {
                    return have;
                }
            }
        }
        return Font.SANS_SERIF;
    }
}
