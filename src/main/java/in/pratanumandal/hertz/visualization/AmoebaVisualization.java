package in.pratanumandal.hertz.visualization;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;

public class AmoebaVisualization extends AbstractVisualization {

    public static final int SPECTRUM_THRESHOLD = -70;
    public static final double SPECTRUM_INTERVAL = 0.04;
    public static final int FREQUENCIES = 1000;

    public static final int DEGREES = 360;

    public static final int BANDS = DEGREES + 40;

    private double[] buffer;
    private int hue;

    public AmoebaVisualization(MediaPlayer mediaPlayer, Canvas canvas) {
        super(mediaPlayer, canvas);
    }

    @Override
    public void initialize() {
        // update media player settings
        mediaPlayer.setAudioSpectrumThreshold(SPECTRUM_THRESHOLD);
        mediaPlayer.setAudioSpectrumInterval(SPECTRUM_INTERVAL);

        // initialize buffer
        this.buffer = new double[FREQUENCIES];
        this.hue = 0;
    }

    @Override
    public void updateVisualization(double[] magnitudes) {
        // map magnitudes into bands based on a logarithmic scale
        double[] bandMagnitudes = VisualizationUtils.magnitudeToLogScaleBands(magnitudes, BANDS);

        // zero fill magnitudes
        VisualizationUtils.magnitudesZeroFill(bandMagnitudes);

        // smoothing
        for (int x = 0; x < bandMagnitudes.length; x++) {
            if (bandMagnitudes[x] > buffer[x])
                buffer[x] = bandMagnitudes[x];
            else buffer[x] -= 1.0;
        }

        // calculate factors
        double minDimension = Math.min(canvas.getWidth(), canvas.getHeight());
        double xFactor = minDimension / 150.0;
        double yFactor = minDimension / 150.0;
        double xCenter = canvas.getWidth() / 2.0;
        double yCenter = canvas.getHeight() / 2.2;
        double yCenterInner = canvas.getHeight() / 1.8;

        // render the visualization
        Platform.runLater(() -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();

            // clear the canvas
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

            Color color = Color.hsb(hue, 1, 1);
            gc.setStroke(color);

            // paint outer shape
            gc.setFill(color);

            gc.beginPath();

            for (int angle = 0; angle < DEGREES / 2; angle++) {
                double x = buffer[angle] * xFactor * Math.sin(Math.toRadians(angle)) + xCenter;
                double y = buffer[angle] * yFactor * Math.cos(Math.toRadians(angle)) + yCenter;
                if (angle == 0) gc.moveTo(x, y);
                else gc.lineTo(x, y);
            }

            for (int angle = DEGREES / 2 - 1; angle >= 0; angle--) {
                double x = buffer[angle] * xFactor * Math.sin(Math.toRadians(DEGREES - angle)) + xCenter;
                double y = buffer[angle] * yFactor * Math.cos(Math.toRadians(DEGREES - angle)) + yCenter;
                gc.lineTo(x, y);
            }

            gc.closePath();
            gc.fill();

            // paint inner shape
            gc.setFill(Color.BLACK);

            gc.beginPath();

            for (int angle = DEGREES / 2; angle < DEGREES; angle++) {
                double x = buffer[angle] * xFactor * Math.sin(Math.toRadians(angle)) + xCenter;
                double y = buffer[angle] * yFactor * Math.cos(Math.toRadians(angle)) + yCenterInner;
                if (angle == DEGREES / 2) gc.moveTo(x, y);
                else gc.lineTo(x, y);
            }

            for (int angle = DEGREES - 1; angle >= DEGREES / 2; angle--) {
                double x = buffer[angle] * xFactor * Math.sin(Math.toRadians(DEGREES - angle)) + xCenter;
                double y = buffer[angle] * yFactor * Math.cos(Math.toRadians(DEGREES - angle)) + yCenterInner;
                gc.lineTo(x, y);
            }

            gc.closePath();
            gc.fill();
            gc.stroke();
        });

        hue = (hue + 1) % 360;
    }

}
