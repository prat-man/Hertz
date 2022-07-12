package in.pratanumandal.hertz.visualization;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.exception.OutOfRangeException;

public class WavesVisualization extends AbstractVisualization {

    public static final int SPECTRUM_THRESHOLD = -70;
    public static final double SPECTRUM_INTERVAL = 0.05;
    public static final int FREQUENCIES = 1000;
    public static final int BANDS = 50;

    private double[] buffer;

    public WavesVisualization(MediaPlayer mediaPlayer, Canvas canvas) {
        super(mediaPlayer, canvas);
    }

    @Override
    public void initialize() {
        // update media player settings
        mediaPlayer.setAudioSpectrumThreshold(SPECTRUM_THRESHOLD);
        mediaPlayer.setAudioSpectrumInterval(SPECTRUM_INTERVAL);
        mediaPlayer.setAudioSpectrumNumBands(FREQUENCIES);

        // initialize buffer
        this.buffer = new double[BANDS];
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
        double xFactor = canvas.getWidth() / BANDS;
        double yFactor = canvas.getHeight() / (-mediaPlayer.getAudioSpectrumThreshold());

        // interpolate to create smooth plot
        double[] bufferX = new double[buffer.length + 1];
        double[] bufferY = new double[buffer.length + 1];
        for (int x = 0; x < buffer.length + 1; x++) {
            bufferX[x] = x * xFactor;
            if (x < buffer.length) bufferY[x] = buffer[x];
        }

        SplineInterpolator interpolator = new SplineInterpolator();
        PolynomialSplineFunction function = interpolator.interpolate(bufferX, bufferY);

        // render the visualization
        Platform.runLater(() -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();

            // clear the canvas
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

            // initialize colors
            Stop[] stops = new Stop[] {
                    new Stop(0.0, Color.CYAN),
                    new Stop(1.0, Color.BLUE)
            };
            RadialGradient gradient = new RadialGradient(0, 0, 0.5, 1.0, 1.0, true, CycleMethod.NO_CYCLE, stops);
            gc.setFill(gradient);

            // paint the waves
            gc.beginPath();

            gc.moveTo(0, canvas.getHeight());

            for (int x = 0; x < canvas.getWidth(); x++) {
                try {
                    gc.lineTo(x, canvas.getHeight() - function.value(x) * yFactor);
                }
                catch (OutOfRangeException e) {
                    break;
                }
            }

            gc.lineTo(canvas.getWidth(), canvas.getHeight());

            gc.closePath();
            gc.fill();
        });
    }

}
