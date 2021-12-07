import javafx.scene.paint.Color;

public class Pixel {

    private final Color color;
    private final double x;
    private final double y;

    public Pixel(Color color, double x, double y) {
        this.color = color;
        this.x = x;
        this.y = y;
    }

    public Color getColor() {
        return color;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

}
