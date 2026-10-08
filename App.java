import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.Enumeration;

public class App {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Force a Windows font that supports Thai.
        // This prevents Thai product names/descriptions from showing as square boxes.
        setThaiUIFont();

        SwingUtilities.invokeLater(() -> {
            SmartScanGUI app = new SmartScanGUI();
            app.setVisible(true);
        });
    }

    private static void setThaiUIFont() {
        String family = findThaiFont();
        FontUIResource normal = new FontUIResource(new Font(family, Font.PLAIN, 14));

        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                Font old = (Font) value;
                UIManager.put(key, new FontUIResource(new Font(
                        family, old.getStyle(), Math.max(old.getSize(), normal.getSize())
                )));
            }
        }
    }

    private static String findThaiFont() {
        String[] preferred = {"Tahoma", "Leelawadee UI", "Nirmala UI", "Arial"};
        String[] installed = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();

        for (String want : preferred) {
            for (String have : installed) {
                if (have.equalsIgnoreCase(want)) return have;
            }
        }
        return Font.SANS_SERIF;
    }
}
