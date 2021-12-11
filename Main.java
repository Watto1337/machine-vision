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
import java.util.List;

public class Main extends Application {

    double hMin = 22.9;
    double hMax = 221;

    double sMin = 115;
    double sMax = 195;

    double bMin = 16;
    double bMax = 101;

    float[] minColor = {(float)hMin, (float)sMin, (float)bMin};
    float[] maxColor = {(float)hMax, (float)sMax, (float)bMax};

    double averageShadeMin = 0;
    double averageShadeMax = 1;

    int outlinePrecision = 100;

    BufferedImage image;
    List<PixelGroup> lastGroups = new ArrayList<>();

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

                Pixel[] validPixels = PixelValidator.validate(
                        allPixels,
                        pixel -> { return pixel.compareColor(minColor, maxColor); }
                );
                
                final boolean objsOutline = showObjects.isSelected();
                final boolean invalidDraw = drawInvalidObjects.isSelected();
                final boolean monochromeDraw = monochromeInvalid.isSelected();
                final double blockSize = minBlockSize.getValue();
                final boolean drawPixels = draw.isSelected();
                final boolean path = drawPath.isSelected();
                List<PixelGroup> pixelGroups = objsOutline ? PixelGrouper.findPixelGroups(validPixels, 3) : new ArrayList<>();

                WritableImage drawnImage = new WritableImage(image.getWidth(), image.getHeight());
                PixelWriter writer = drawnImage.getPixelWriter();
                if (drawPixels) {
                    int incrementAmount = 1;
                    for (int i = 0, allPixelsLength = allPixels.length; i < allPixelsLength; i += incrementAmount) {
                        Pixel pixel = allPixels[i];
                        boolean accepted = validPixels[i] != null;
                        if (accepted) {
                            writer.setColor((int) pixel.getX(), (int) pixel.getY(), pixel.getColor());
                        } else if (invalidDraw) {
                            Color color;
                            if (monochromeDraw) {
                                color = pixel.getColor().grayscale();
                            } else {
                                color = pixel.getColor();
                            }
                            writer.setColor((int) pixel.getX(), (int) pixel.getY(), color);
                        }
                    }
                }


                Platform.runLater(() -> {
                    Canvas canvas = new Canvas(image.getWidth(), image.getHeight());
                    canvas.setScaleX(6);
                    canvas.setScaleY(6);
                    GraphicsContext context = canvas.getGraphicsContext2D();
                    context.drawImage(drawnImage, 0, 0);
                    pane.setCenter(canvas);

                    context.setStroke(Color.rgb(255, 0, 255, 0.3));
                    for (PixelGroup group : pixelGroups) {
                        List<Pixel> pixels = group.getPixels();
                        if (pixels.size() > blockSize) {
                            if (path) {
                                PixelGroup matchingGroup = null;
                                for (PixelGroup lastGroup : lastGroups) {
                                    if (PixelGrouper.groupsMatch(group, lastGroup, 0.3, 25)) {
                                        matchingGroup = lastGroup;
                                        break;
                                    }
                                }
                                if (matchingGroup != null) {
                                    group.setPrevious(matchingGroup);
                                    drawPath(context, group, 1, 10000);
                                }
                            }
                            context.setFill(Color.rgb(0, 0, 0, 0.5));
                            //context.fillRect(group.getMinX(), group.getMinY(), group.getMaxX() - group.getMinX(), group.getMaxY() - group.getMinY());

                            double centerX = group.getMinX() + (group.getMaxX() - group.getMinX()) / 2;
                            double centerY = group.getMinY() + (group.getMaxY() - group.getMinY()) / 2;

                            int[] minColor = {(int)yMin, (int)cbMin, (int)crMin};
                            int[] maxColor = {(int)yMax, (int)cbMax, (int)crMax};

                            double[][] groupOutline = GroupShape.getShape((int) centerX, (int) centerY, (int)group.getMinX(), (int)group.getMaxX(), (int)group.getMinY(), (int)group.getMaxY(), outlinePrecision, image, minColor, maxColor, (int)maxError.getValue());

                            GroupShape.smoothPolygon(groupOutline);

                            context.setStroke(Color.BLACK);
                            //context.fillPolygon(groupOutline[0], groupOutline[1], outlinePrecision);
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
        double prevX = centerX(group);
        double prevY = centerY(group);
        context.setLineWidth(lineSize);
        int i = 0;
        while (group != null) {
            double x = centerX(group);
            double y = centerY(group);
            context.strokeLine(prevX, prevY, x, y);
            prevX = x;
            prevY = y;
            group = group.getPrevious();
            if (++i >= maxLines) break;
        }
    }

    private double centerX(PixelGroup group) { return (group.getMinX() + group.getMaxX()) / 2; }
    private double centerY(PixelGroup group) { return (group.getMinY() + group.getMaxY()) / 2; }

    private double averageShade(Color color) { return (color.getRed() + color.getGreen() + color.getBlue()) / 3D; }
}