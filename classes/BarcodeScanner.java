package classes;

import com.github.sarxos.webcam.Webcam;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

public class BarcodeScanner {
    public interface ScanListener {
        void onScanned(String barcode);
        void onStatus(String message);
    }

    private Webcam webcam;
    private Thread worker;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile BufferedImage latestImage;
    private String lastCode = "";
    private long lastScanTime = 0;

    public boolean start(ScanListener listener) {
        if (running.get()) return true;
        try {
            webcam = Webcam.getDefault();
            if (webcam == null) {
                listener.onStatus("ไม่พบกล้อง Webcam");
                return false;
            }
            Dimension[] sizes = webcam.getViewSizes();
            Dimension target = sizes.length > 0 ? sizes[sizes.length - 1] : new Dimension(480, 640);
            for (Dimension d : sizes) {
                if (d.width == 480 && d.height == 640) { target = d; break; }
            }
            webcam.setViewSize(target);
            webcam.open();
            running.set(true);
            listener.onStatus("Scanner ready");
            worker = new Thread(() -> scanLoop(listener), "barcode-scanner");
            worker.setDaemon(true);
            worker.start();
            return true;
        } catch (Exception e) {
            listener.onStatus("เปิดกล้องไม่ได้: " + e.getMessage());
            stop();
            return false;
        }
    }

    private void scanLoop(ScanListener listener) {
        MultiFormatReader reader = new MultiFormatReader();
        while (running.get()) {
            try {
                BufferedImage image = webcam.getImage();
                if (image != null) {
                    latestImage = image;
                    try {
                        LuminanceSource source = new BufferedImageLuminanceSource(image);
                        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                        Result result = reader.decodeWithState(bitmap);
                        String code = result.getText();
                        long now = System.currentTimeMillis();
                        if (!code.equals(lastCode) || now - lastScanTime > 1800) {
                            lastCode = code;
                            lastScanTime = now;
                            listener.onScanned(code);
                        }
                    } catch (Exception ignored) {
                    } finally {
                        reader.reset();
                    }
                }
                Thread.sleep(60);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ignored) {
            }
        }
    }

    public BufferedImage getLatestImage() { return latestImage; }

    public void stop() {
        running.set(false);
        try {
            if (worker != null) worker.interrupt();
            if (webcam != null && webcam.isOpen()) webcam.close();
        } catch (Exception ignored) {}
        webcam = null;
        latestImage = null;
    }
}
