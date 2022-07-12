package in.pratanumandal.hertz.visualization;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public class FireVisualization extends AbstractVisualization {

    public static final int SPECTRUM_THRESHOLD = -70;
    public static final double SPECTRUM_INTERVAL = 0.05;
    public static final int FREQUENCIES = 1000;

    private double[] buffer;

    public FireVisualization(MediaPlayer mediaPlayer, Canvas canvas) {
        super(mediaPlayer, canvas);
    }

    @Override
    public void initialize() {
        // update media player settings
        mediaPlayer.setAudioSpectrumThreshold(SPECTRUM_THRESHOLD);
        mediaPlayer.setAudioSpectrumInterval(SPECTRUM_INTERVAL);

        // initialize buffer
        this.buffer = new double[FREQUENCIES];
    }

    @Override
    public void updateVisualization(double[] magnitudes) {
        int plotPoints = Math.min(FREQUENCIES, (int) canvas.getWidth());

        // map magnitudes into bands based on a logarithmic scale
        double[] bandMagnitudes = VisualizationUtils.magnitudeToLogScaleBands(magnitudes, plotPoints);

        // zero fill magnitudes
        VisualizationUtils.magnitudesZeroFill(bandMagnitudes);

        // smoothing
        for (int x = 0; x < bandMagnitudes.length; x++) {
            if (bandMagnitudes[x] > buffer[x])
                buffer[x] = bandMagnitudes[x];
            else buffer[x] -= 1.0;
        }

        // calculate factors
        double xFactor = canvas.getWidth() / plotPoints;
        double yFactor = canvas.getHeight() / (-mediaPlayer.getAudioSpectrumThreshold());

        // render the visualization
        Platform.runLater(() -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();

            // clear the canvas
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

            // initialize colors
            Stop[] stops = new Stop[] {
                    new Stop(0.0, Color.YELLOW),
                    new Stop(1.0, Color.RED)
            };
            RadialGradient gradient = new RadialGradient(0, 0, 0.5, 1.0, 1.0, true, CycleMethod.NO_CYCLE, stops);
            gc.setFill(gradient);

            // paint the visualization
            gc.beginPath();

            gc.moveTo(0, canvas.getHeight());

            for (int x = 0; x < plotPoints; x++) {
                gc.lineTo(x * xFactor, canvas.getHeight() - buffer[x] * yFactor);
            }

            gc.lineTo(canvas.getWidth(), canvas.getHeight());

            gc.closePath();
            gc.fill();
        });
    }

}
