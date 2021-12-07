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
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.controlsfx.control.RangeSlider;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    double yMin = 22.9;
    double yMax = 221;

    double crMin = 115;
    double crMax = 195;

    double cbMin = 16;
    double cbMax = 101;

    double averageShadeMin = 0;
    double averageShadeMax = 1;

    BufferedImage image;
    List<PixelGroup> lastGroups = new ArrayList<>();

    long minTime = 1000 / 60;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {
        final Webcam webcam = Webcam.getDefault();
        webcam.open();

        Slider minY = new Slider(16, 235, yMin);
        Slider maxY = new Slider(16, 235, yMax);
        Slider minCr = new Slider(16, 240, crMin);
        Slider maxCr = new Slider(16, 240, crMax);
        Slider minCb = new Slider(16, 240, cbMin);
        Slider maxCb = new Slider(16, 240, cbMax);
        Slider threshold = new Slider(0, 100, 70);
        threshold.setBlockIncrement(1);
        Slider maxError = new Slider(0, 5, 2);

        Label minYVal = new Label();
        minYVal.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(Math.round(minY.getValue() * 10000) / 10000D), minY.valueProperty()));
        Label maxYVal = new Label();
        maxYVal.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(Math.round(maxY.getValue() * 10000) / 10000D), maxY.valueProperty()));
        Label minCrVal = new Label();
        minCrVal.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(Math.round(minCr.getValue() * 10000) / 10000D), minCr.valueProperty()));
        Label maxCrVal = new Label();
        maxCrVal.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(Math.round(maxCr.getValue() * 10000) / 10000D), maxCr.valueProperty()));
        Label minCbVal = new Label();
        minCbVal.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(Math.round(minCb.getValue() * 10000) / 10000D), minCb.valueProperty()));
        Label maxCbVal = new Label();
        maxCbVal.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(Math.round(maxCb.getValue() * 10000) / 10000D), maxCb.valueProperty()));
        Label thresholdView = new Label();
        thresholdView.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf((int) threshold.getValue()), threshold.valueProperty()));
        Label maxErrorVal = new Label();
        maxErrorVal.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf((int) maxError.getValue()), maxError.valueProperty()));

        GridPane minMaxColors = new GridPane();
        minMaxColors.addRow(0, new Label("Min Y: "), minY, minYVal);
        minMaxColors.addRow(1, new Label("Max Y: "), maxY, maxYVal);
        minMaxColors.addRow(2, new Label("Min Cb: "), minCr, minCrVal);
        minMaxColors.addRow(3, new Label("Max Cb: "), maxCr, maxCrVal);
        minMaxColors.addRow(4, new Label("Min Cr: "), minCb, minCbVal);
        minMaxColors.addRow(5, new Label("Max Cr: "), maxCb, maxCbVal);
        minMaxColors.addRow(6, new Label("Threshold"), threshold, thresholdView);
        minMaxColors.addRow(7, new Label("Max Error"), maxError, maxErrorVal);

        Slider minAverageShade = new Slider(0, 1, averageShadeMin);
        Slider maxAverageShade = new Slider(0, 1, averageShadeMax);
        Label minAverageShadeVal = new Label();
        minAverageShadeVal.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(Math.round(minAverageShade.getValue() * 10000) / 10000D), minAverageShade.valueProperty()));
        Label maxAverageShadeVal = new Label();
        maxAverageShadeVal.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(Math.round(maxAverageShade.getValue() * 10000) / 10000D), maxAverageShade.valueProperty()));

        GridPane minMaxShade = new GridPane();
        minMaxShade.addRow(0, new Label("Min Average Shade: "), minAverageShade, minAverageShadeVal);
        minMaxShade.addRow(1, new Label("Max Average Shade: "), maxAverageShade, maxAverageShadeVal);

        Slider minBlockSize = new Slider(1, 500, 50);
        Label minBlockSizeVal = new Label();
        minBlockSizeVal.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(Math.round(minBlockSize.getValue())), minBlockSize.valueProperty()));

        CheckBox showObjects = new CheckBox("Show Objects");
        showObjects.setSelected(true);
        CheckBox draw = new CheckBox("Draw Pixels");
        draw.setSelected(true);
        CheckBox drawInvalidObjects = new CheckBox("Draw Invalid Pixels");
        drawInvalidObjects.setSelected(true);
        CheckBox monochromeInvalid = new CheckBox("Draw Invalid Pixels Monochrome");
        monochromeInvalid.setSelected(true);
        CheckBox playing = new CheckBox("Record");
        playing.setSelected(true);
        CheckBox drawPath = new CheckBox("Draw Path");

        VBox sliders = new VBox(
                minMaxColors,
                new Separator(), minMaxShade,
                new Separator(), showObjects, draw, drawInvalidObjects, monochromeInvalid, playing, drawPath,
                new Separator(), new HBox(new Label("Minimum Pixel Group Size:  "), minBlockSize, minBlockSizeVal)
        );
        sliders.setPadding(new Insets(20));
        sliders.setPrefWidth(450);

        final BorderPane pane = new BorderPane();
        pane.setRight(sliders);
        Thread thread = new Thread(() -> {
            while (true) {
                long start = System.currentTimeMillis();
                if (playing.isSelected()) {
                    image = webcam.getImage();
                }
                Pixel[] allPixels = PixelValidator.loadPixelsFromImage(image);
                yMin = minY.getValue();
                yMax = maxY.getValue();
                crMin = minCr.getValue();
                crMax = maxCr.getValue();
                cbMin = minCb.getValue();
                cbMax = maxCb.getValue();
                int thresholdVal = (int) threshold.getValue();
                averageShadeMin = minAverageShade.getValue();
                averageShadeMax = maxAverageShade.getValue();
                Pixel[] validPixels = PixelValidator.validate(
                        allPixels,
                        pixel -> {
                            Color color = pixel.getColor();
                            double averageShade = averageShade(color);

                            double r = color.getRed() * 255;
                            double g = color.getGreen() * 255;
                            double b = color.getBlue() * 255;

                            int y = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                            int cb = (int) (128 - 0.169 * r - 0.331 * g + 0.500 * b);
                            int cr = (int) (128 + 0.500 * r - 0.419 * g - 0.081 * b);

                            return y > yMin && y < yMax &&
                                    cb > cbMin && cb < cbMax &&
                                    cr > crMin && cr < crMax &&
                                    averageShade > averageShadeMin && averageShade < averageShadeMax;
                        }
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
                            context.fillRect(group.getMinX(), group.getMinY(), group.getMaxX() - group.getMinX(), group.getMaxY() - group.getMinY());

                            double centerX = group.getMinX() + (group.getMaxX() - group.getMinX()) / 2;
                            double centerY = group.getMinY() + (group.getMaxY() - group.getMinY()) / 2;

                            int[] minColor = {(int)yMin, (int)crMin, (int)cbMin};
                            int[] maxColor = {(int)yMax, (int)crMax, (int)cbMax};

                            double[][] groupOutline = GroupShape.getShape((int) centerX, (int) centerY, 20, image, minColor, maxColor, (int)maxError.getValue());

                            context.setFill(Color.BLUE);
                            context.fillPolygon(groupOutline[0], groupOutline[1], 20);
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

    private double centerX(PixelGroup group) {
        return group.getMinX() + (group.getMaxX() - group.getMinX()) / 2;
    }
    private double centerY(PixelGroup group) {
        return group.getMinY() + (group.getMaxY() - group.getMinY()) / 2;
    }

    private double averageShade(Color color) {
        return (color.getRed() + color.getGreen() + color.getBlue()) / 3D;
    }

}
