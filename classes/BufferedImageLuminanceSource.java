package classes;

import com.google.zxing.LuminanceSource;
import java.awt.image.BufferedImage;

public final class BufferedImageLuminanceSource extends LuminanceSource {
    private final byte[] luminances;

    public BufferedImageLuminanceSource(BufferedImage image) {
        super(image.getWidth(), image.getHeight());
        int w = image.getWidth();
        int h = image.getHeight();
        int[] pixels = new int[w * h];
        image.getRGB(0, 0, w, h, pixels, 0, w);
        luminances = new byte[w * h];
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int r = (pixel >> 16) & 0xff;
            int g = (pixel >> 8) & 0xff;
            int b = pixel & 0xff;
            luminances[i] = (byte) ((r + 2 * g + b) / 4);
        }
    }

    @Override
    public byte[] getRow(int y, byte[] row) {
        if (y < 0 || y >= getHeight()) throw new IllegalArgumentException("Row outside image");
        int width = getWidth();
        if (row == null || row.length < width) row = new byte[width];
        System.arraycopy(luminances, y * width, row, 0, width);
        return row;
    }

    @Override
    public byte[] getMatrix() { return luminances; }
}
