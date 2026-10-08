package smartscan;

import smartscan.ui.MainFrame;
import smartscan.ui.Theme;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** จุดเริ่มต้นของโปรแกรม */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        Theme.installFonts(); // ตั้งฟอนต์ที่รองรับภาษาไทย

        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
