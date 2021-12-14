import com.github.sarxos.webcam.Webcam;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import java.awt.Color;
import javafx.stage.Stage;
import org.controlsfx.control.RangeSlider;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Main extends Application {

    final static javafx.scene.paint.Color BLACK = javafx.scene.paint.Color.BLACK;

    double hMin = 0;
    double hMax = 0.2;

    double sMin = 0;
    double sMax = 0.4;

    double bMin = 0.5;
    double bMax = 1;

    float[] minColor = {(float)hMin, (float)sMin, (float)bMin};
    float[] maxColor = {(float)hMax, (float)sMax, (float)bMax};

    int outlinePrecision = 100;

    int maxError = 10;

    BufferedImage image;
    ArrayList<PixelGroup> lastGroups = new ArrayList<>();

    long minTime = 1000 / 60;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        final Webcam webcam = Webcam.getDefault();
        webcam.open();

        final BorderPane pane = new BorderPane();
        Thread thread = new Thread(() -> {
            while (true) {
                long start = System.currentTimeMillis();
                image = webcam.getImage();

                Pixel[] allPixels = PixelValidator.loadPixelsFromImage(image);

                Color firstPixelColor = allPixels[0].getColor();

                float[] pixelHSB = Color.RGBtoHSB(firstPixelColor.getRed(), firstPixelColor.getGreen(), firstPixelColor.getBlue(), null);

                //System.out.println(pixelHSB[0] + " " + pixelHSB[1] + " " + pixelHSB[2]);

                Pixel[] validPixels = PixelValidator.validate(
                        allPixels,
                        pixel -> { return pixel.compareColor(minColor, maxColor); }
                );
                
                //final boolean objsOutline = showObjects.isSelected();
                //final boolean invalidDraw = drawInvalidObjects.isSelected();
                //final boolean monochromeDraw = monochromeInvalid.isSelected();
                //final double blockSize = minBlockSize.getValue();
                //final boolean drawPixels = draw.isSelected();
                //final boolean path = drawPath.isSelected();
                ArrayList<PixelGroup> pixelGroups = PixelGrouper.findPixelGroups(validPixels, 3);

                WritableImage drawnImage = new WritableImage(image.getWidth(), image.getHeight());
                PixelWriter writer = drawnImage.getPixelWriter();

                for (int i = 0; i < allPixels.length; i++) {
                    Pixel pixel = allPixels[i];
                    boolean accepted = validPixels[i] != null;

                    int pixelColor = pixel.getColor().getRGB();

                    javafx.scene.paint.Color color = javafx.scene.paint.Color.rgb((pixelColor >> 16) & 0xff, (pixelColor >> 8) & 0xff, pixelColor & 0xff);

                    if (accepted) {
                        writer.setColor((int) pixel.getX(), (int) pixel.getY(), color);
                    } else {
                        //writer.setColor((int) pixel.getX(), (int) pixel.getY(), color.grayscale());
                    }
                }

                Platform.runLater(() -> {
                    Canvas canvas = new Canvas(image.getWidth(), image.getHeight());
                    canvas.setScaleX(6);
                    canvas.setScaleY(6);

                    GraphicsContext context = canvas.getGraphicsContext2D();
                    context.drawImage(drawnImage, 0, 0);

                    pane.setCenter(canvas);

                    for (PixelGroup group : pixelGroups) {
                        ArrayList<Pixel> pixels = group.getPixels();

                        if (pixels.size() > 100) {
                            context.setFill(BLACK);
                            //context.fillRect(group.getMinX(), group.getMinY(), group.getMaxX() - group.getMinX(), group.getMaxY() - group.getMinY());

                            double centerX = group.getCenterX();
                            double centerY = group.getCenterY();

                            double[][] groupOutline = group.getOutline(outlinePrecision, image, minColor, maxColor, maxError);

                            PixelGroup.smoothPolygon(groupOutline);

                            context.setStroke(BLACK);
                            context.strokePolygon(groupOutline[0], groupOutline[1], outlinePrecision);
                        }
                    }
                    lastGroups.clear();
                    lastGroups.addAll(pixelGroups);
                });
                long time = System.currentTimeMillis() - start;
                if (time < minTime) {
                    try {
                        Thread.sleep(minTime - time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
        primaryStage.setScene(new Scene(pane));
        primaryStage.show();
    }

    private void drawPath(GraphicsContext context, PixelGroup group, int lineSize, int maxLines) {
        double prevX = group.getCenterX();
        double prevY = group.getCenterY();
        context.setLineWidth(lineSize);
        int i = 0;
        while (group != null) {
            double x = group.getCenterX();
            double y = group.getCenterY();
            context.strokeLine(prevX, prevY, x, y);
            prevX = x;
            prevY = y;
            group = group.getPrevious();
            if (++i >= maxLines) break;
        }
    }
}