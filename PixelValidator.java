
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;
import java.util.function.Predicate;

public class PixelValidator {

    public static Pixel[] loadPixelsFromImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        Pixel[] pixels = new Pixel[width * height];

        int pos = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = image.getRGB(x, y);
                pixels[pos++] = new Pixel(Color.rgb(
                        (rgb >> 16) & 0xFF,
                        (rgb >> 8) & 0xFF,
                        rgb & 0xFF
                ), x, y);
            }
        }

        return pixels;
    }
    public static Pixel[] validate(Pixel[] pixels, Predicate<Pixel> validator) {
        Pixel[] valid = new Pixel[pixels.length];

        for (int i = 0; i < pixels.length; i++) {
            if (validator.test(pixels[i])) {
                valid[i] = pixels[i];
            } else {
                valid[i] = null;
            }
        }

        return valid;
    }

}
