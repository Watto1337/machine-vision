import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class PixelGroup {
    private double minX = Integer.MAX_VALUE;
    private double maxX = Integer.MIN_VALUE;
    private double minY = Integer.MAX_VALUE;
    private double maxY = Integer.MIN_VALUE;

    private final ArrayList<Pixel> pixels = new ArrayList<>();

    private PixelGroup previous = null;

    public PixelGroup(ArrayList<PixelGroup> groups) {
        for (PixelGroup group : groups) {
            maxX = Math.max(maxX, group.maxX);
            maxY = Math.max(maxY, group.maxY);
            minX = Math.min(minX, group.minX);
            minY = Math.min(minY, group.minY);
            pixels.addAll(group.pixels);
        }
    }

    public PixelGroup(Pixel original) {
        addPixel(original);
    }

    public double[][] getOutline(int precision, BufferedImage image, float[] minColor, float[] maxColor, int maxError) {
        double[][] shape = new double[2][precision];

        int x = (int)getCenterX();
        int y = (int)getCenterY();

        for (int i = 0; i < precision; i++) {
            double angle = Math.PI * 2.0 * (double)i / (double)precision;

            int len = 1;
            double[] vec = {Math.cos(angle), Math.sin(angle)};

            Pixel currPoint = new Pixel(image.getRGB(x, y), x, y);

            int error = 0;

            while (fallsWithinBoundingBox(currPoint) && error < maxError) {

                if (currPoint.compareColor(minColor, maxColor)) error = 0;
                else error++;

                len++;

                currPoint.setColor(image.getRGB((int)currPoint.getX(), (int)currPoint.getY()));
                currPoint.setX(x + len * vec[0]);
                currPoint.setY(y + len * vec[1]);
            }

            currPoint.setX(x + (len - error - 1) * vec[0]);
            currPoint.setY(y + (len - error - 1) * vec[1]);

            shape[0][i] = currPoint.getX();
            shape[1][i] = currPoint.getY();
        }

        return shape;
    }

    public static void smoothPolygon(double[][] polygon) {
        int numPoints = polygon[0].length;

        for (int i = 0; i < numPoints; i++) {
            int prev;
            if (i == 0) prev = numPoints - 1;
            else prev = i - 1;

            int next;
            if (i == numPoints - 1) next = 0;
            else next = i + 1;

            double distAround = Math.sqrt((polygon[0][prev] - polygon[0][next]) * (polygon[0][prev] - polygon[0][next]) +
                    (polygon[1][prev] - polygon[1][next]) * (polygon[1][prev] - polygon[1][next]));

            double avgDistThrough = (Math.sqrt((polygon[0][prev] - polygon[0][i]) * (polygon[0][prev] - polygon[0][i]) +
                    (polygon[1][prev] - polygon[1][i]) * (polygon[1][prev] - polygon[1][i])) +
                    Math.sqrt((polygon[0][i] - polygon[0][next]) * (polygon[0][i] - polygon[0][next]) +
                            (polygon[1][i] - polygon[1][next]) * (polygon[1][i] - polygon[1][next]))) / 2;

            if (distAround < avgDistThrough) {
                polygon[0][i] = polygon[0][prev];
                polygon[1][i] = polygon[1][prev];
            }
        }
    }

    public void addPixel(Pixel pixel) {
        double x = pixel.getX();
        double y = pixel.getY();

        if (x > maxX) maxX = x;
        if (x < minX) minX = x;
        if (y > maxY) maxY = y;
        if (y < minY) minY = y;

        pixels.add(pixel);
    }

    private boolean fallsWithinBoundingBox(double x, double y) {
        return  x > minX && x < maxX &&
                y > minY && y < maxY;
    }

    private boolean fallsWithinBoundingBox(Pixel pixel) {
        return fallsWithinBoundingBox(pixel.getX(), pixel.getY());
    }

    public boolean accepts(Pixel pixel, double maxLength) {
        if (fallsWithinBoundingBox(pixel.getX(), pixel.getY())) return true;

        double dx = Math.max(minX - pixel.getX(), Math.max(0, pixel.getX() - maxX));
        double dy = Math.max(minY - pixel.getY(), Math.max(0, pixel.getY() - maxY));
        return Math.sqrt(dx*dx + dy*dy) <= maxLength;
    }

    public ArrayList<Pixel> getPixels() {
        return pixels;
    }

    public double getMinX() {
        return minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxY() {
        return maxY;
    }

    private double getCenterX() { return (getMinX() + getMaxX()) / 2; }

    private double getCenterY() { return (getMinY() + getMaxY()) / 2; }

    public PixelGroup getPrevious() {
        return previous;
    }

    public void setPrevious(PixelGroup previous) {
        this.previous = previous;
    }
}
