package in.pratanumandal.hertz.gui.visualization;

import javafx.application.Platform;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class VisualizationUtils {

    /**
     * Map magnitudes into bands based on a logarithmic scale
     *
     * @param magnitudes
     * @param bands
     * @return
     */
    public static double[] magnitudeToLogScaleBands(double[] magnitudes, int bands) {
        double[] bandMagnitudes = new double[bands];
        for (int x = 0; x < magnitudes.length; x++) {
            int index = (int) Math.round((bands - 1) * Math.log(x + 1) / Math.log(magnitudes.length) + 1);
            if (magnitudes[x] > bandMagnitudes[index - 1]) {
                bandMagnitudes[index - 1] = magnitudes[x];
            }
        }
        return bandMagnitudes;
    }

    /**
     * Fill zeros in magnitudes based on neighbouring magnitudes
     *
     * @param magnitudes
     */
    public static double[] magnitudesZeroFill(double[] magnitudes) {
        for (int x = 0; x < magnitudes.length; x++) {
            if (x == magnitudes.length - 1) {
                if (magnitudes[x] == 0 && magnitudes[x - 2] != 0 && magnitudes[x - 1] != 0) {
                    magnitudes[x] = interpolate(magnitudes, x - 2, x - 1, x);
                }
            }
            else if (magnitudes[x] != 0 && magnitudes[x + 1] == 0) {
                int xNext = x + 2;
                while (xNext < magnitudes.length && magnitudes[xNext] == 0) {
                    xNext++;
                }

                if (xNext != magnitudes.length) {
                    magnitudes[x + 1] = interpolate(magnitudes, x, xNext, x + 1);
                }
            }
        }

        return magnitudes;
    }

    /**
     * Interpolate for xI on a straight line containing points x1 and x2.
     * Add randomness to improve visualization.
     *
     * @param magnitudes
     * @param x1
     * @param x2
     * @param xI
     * @return
     */
    private static double interpolate(double[] magnitudes, int x1, int x2, int xI) {
        double slope = (magnitudes[x2] - magnitudes[x1]) / (x2 - x1);
        double yIntercept = magnitudes[x1] - slope * x1;
        double yI = slope * xI + yIntercept;

        if (yI == 0) return yI;
        else return yI + (Math.random() - 0.5) * 4.0;
    }

    public static void copyCanvas(Canvas source, Canvas target) {
        Platform.runLater(() -> {
            WritableImage image = new WritableImage((int) source.getWidth(), (int) source.getHeight());

            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);

            source.snapshot(parameters, image);

            GraphicsContext gc = target.getGraphicsContext2D();
            gc.drawImage(image, 0, 0);
        });
    }

}
