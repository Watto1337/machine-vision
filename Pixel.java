import java.awt.Color;

public class Pixel {
    private Color color;
    private double x;
    private double y;

    public Pixel(Color color, double x, double y) {
        this.color = color;
        this.x = x;
        this.y = y;
    }

    public Pixel(int rgb, double x, double y) {
        this.color = new Color(rgb);
        this.x = x;
        this.y = y;
    }

    public boolean compareColor(float[] minColor, float[] maxColor) {
        float[] HSBColor = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getGreen(), null);

        for (int i = 0; i < 3; i++) {
            if (HSBColor[i] < minColor[i] || HSBColor[i] > maxColor[i]) return false;
        }

        return true;
    }

    public void setColor(Color color) {this.color = color;}

    public void setColor(int rgb) {this.color = new Color(rgb);}

    public void setX(double x) {this.x = x;}

    public void setY(double y) {this.y = y;}

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
