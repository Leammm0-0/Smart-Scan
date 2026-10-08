package smartscan.service;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * เปิดกล้องใน thread แยก วนอ่านภาพ แล้วส่ง barcode กลับผ่าน Listener
 * ใช้ JavaCV (รองรับ Mac M1/M2/M3 ต่างจาก webcam-capture ที่ไม่รองรับ)
 *
 * หมายเหตุ: method ใน Listener ถูกเรียกจาก thread ของกล้อง ไม่ใช่ thread ของ Swing
 */
public class CameraService {
    // ===== ตั้งค่ากล้อง =====
    private static final String MAC_CAMERA_DEVICE = "0:none"; // กล้องตัวแรกของ Mac (ถ้าไปจับกล้อง iPhone ให้ลองเปลี่ยนเป็น "1:none")
    private static final int CAMERA_INDEX = 0;                // Windows/Linux: กล้องตัวแรก
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final int FRAME_RATE = 30;

    /** อ่าน barcode ทุกๆ กี่มิลลิวินาที */
    private static final long SCAN_INTERVAL_MS = 300;

    /** ถ้ากล้องไม่มีภาพมาเกินกี่มิลลิวินาที ให้ถือว่าใช้กล้องไม่ได้ */
    private static final long NO_FRAME_TIMEOUT_MS = 5000;

    public interface Listener {
        void onBarcode(String barcode);
        void onStatus(String message);

        /** กล้องเปิดไม่ได้ หรือเปิดแล้วแต่ไม่มีภาพ (เช่น เครื่องไม่มีกล้อง) */
        default void onCameraFailed() {
        }
    }

    /** ข้อมูลของการเปิดกล้อง 1 รอบ (แยกรอบกัน เพื่อไม่ให้รอบเก่าที่กำลังปิดกล้องมากวนรอบใหม่) */
    private static class Session {
        final AtomicBoolean active = new AtomicBoolean(true);
        final CountDownLatch finished = new CountDownLatch(1);
    }

    private final BarcodeDecoder decoder = new BarcodeDecoder();
    private Session session;
    private volatile BufferedImage latestImage;

    public synchronized boolean isRunning() {
        return session != null && session.active.get();
    }

    /** ภาพล่าสุดจากกล้อง (ให้หน้าจอเอาไปวาด) ถ้ากล้องยังไม่พร้อมจะเป็น null */
    public BufferedImage getLatestImage() {
        return latestImage;
    }

    public synchronized void start(Listener listener) {
        if (isRunning()) {
            return; // เปิดอยู่แล้ว
        }
        Session previous = session;
        Session current = new Session();
        session = current;

        Thread thread = new Thread(() -> {
            waitUntilFinished(previous); // รอให้กล้องรอบก่อนปิดสนิทก่อน
            run(current, listener);
        }, "SmartScan-Camera");
        thread.setDaemon(true);
        thread.start();
    }

    public synchronized void stop() {
        if (session != null) {
            session.active.set(false); // loop ใน run() จะจบเอง แล้วปิดกล้องให้
        }
        latestImage = null;
    }

    private static void waitUntilFinished(Session previous) {
        if (previous == null) {
            return;
        }
        try {
            previous.finished.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void run(Session session, Listener listener) {
        FrameGrabber grabber = null;
        Java2DFrameConverter converter = new Java2DFrameConverter();

        try {
            if (!session.active.get()) {
                return; // ระหว่างรอ มีคนสั่งหยุดไปแล้ว
            }

            grabber = createGrabber();
            grabber.start();
            listener.onStatus("Scanner ready");

            long startTime = System.currentTimeMillis();
            boolean reportedNoFrame = false;
            long lastScanTime = 0;
            while (session.active.get()) {
                Frame frame = grabber.grab();
                if (frame == null) {
                    Thread.sleep(10);
                    // เปิดกล้องได้แต่ไม่มีภาพเลย -> แจ้งให้หน้าจอรู้ (แจ้งครั้งเดียว)
                    if (latestImage == null && !reportedNoFrame
                            && System.currentTimeMillis() - startTime > NO_FRAME_TIMEOUT_MS) {
                        reportedNoFrame = true;
                        listener.onStatus("ไม่พบภาพจากกล้อง");
                        listener.onCameraFailed();
                    }
                    continue;
                }

                BufferedImage image = converter.convert(frame);
                if (image == null) {
                    continue;
                }
                if (session.active.get()) {
                    latestImage = image;
                }

                long now = System.currentTimeMillis();
                if (now - lastScanTime < SCAN_INTERVAL_MS) {
                    continue;
                }
                lastScanTime = now;

                String barcode = decoder.decode(image);
                if (barcode != null) {
                    listener.onBarcode(barcode);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            if (session.active.get()) {
                listener.onStatus("เปิดกล้องไม่ได้ (เช็กสิทธิ์ Camera)");
                listener.onCameraFailed();
            }

        } finally {
            try {
                if (grabber != null) {
                    grabber.stop();
                    grabber.release();
                }
            } catch (Exception ignored) {
            }
            converter.close();
            session.active.set(false);
            session.finished.countDown();
        }
    }

    /** Mac ใช้ FFmpeg + avfoundation, ระบบอื่นใช้ OpenCV */
    private FrameGrabber createGrabber() {
        FrameGrabber grabber;

        boolean isMac = System.getProperty("os.name", "").toLowerCase().contains("mac");
        if (isMac) {
            grabber = new FFmpegFrameGrabber(MAC_CAMERA_DEVICE);
            grabber.setFormat("avfoundation");
        } else {
            grabber = new OpenCVFrameGrabber(CAMERA_INDEX);
        }

        grabber.setImageWidth(WIDTH);
        grabber.setImageHeight(HEIGHT);
        grabber.setFrameRate(FRAME_RATE);
        return grabber;
    }
}
