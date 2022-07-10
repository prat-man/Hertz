package in.pratanumandal.hertz.gui.visualization;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;

public class BarsVisualization extends AbstractVisualization {

    public static final int SPECTRUM_THRESHOLD = -70;
    public static final double SPECTRUM_INTERVAL = 0.05;
    public static final int FREQUENCIES = 1000;
    public static final int BANDS = 50;
    public static final double INTER_BAND_SPACE = 1.0;

    private double[] buffer;
    private double[] doubleBuffer;

    public BarsVisualization(MediaPlayer mediaPlayer, Canvas canvas) {
        super(mediaPlayer, canvas);
    }

    @Override
    public void initialize() {
        // update media player settings
        mediaPlayer.setAudioSpectrumThreshold(SPECTRUM_THRESHOLD);
        mediaPlayer.setAudioSpectrumInterval(SPECTRUM_INTERVAL);
        mediaPlayer.setAudioSpectrumNumBands(FREQUENCIES);

        // initialize buffers
        this.buffer = new double[BANDS];
        this.doubleBuffer = new double[BANDS];
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

            if (bandMagnitudes[x] > doubleBuffer[x])
                doubleBuffer[x] = bandMagnitudes[x];
            else doubleBuffer[x] -= 0.5;
        }

        // calculate factors
        double xFactor = canvas.getWidth() / BANDS;
        double yFactor = canvas.getHeight() / (-mediaPlayer.getAudioSpectrumThreshold());

        // render the visualization
        Platform.runLater(() -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();

            // clear the canvas
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

            // initialize colors
            gc.setFill(Color.GREENYELLOW);
            gc.setStroke(Color.WHITE);

            // paint the bars
            gc.beginPath();
            for (int x = 0; x < BANDS; x++) {
                if (buffer[x] > 1) {
                    gc.rect(x * xFactor,
                            canvas.getHeight() - buffer[x] * yFactor,
                            xFactor - INTER_BAND_SPACE,
                            buffer[x] * yFactor);
                }
            }
            gc.closePath();
            gc.fill();

            // paint the dashes
            gc.beginPath();
            for (int x = 0; x < BANDS; x++) {
                if (doubleBuffer[x] > 1) {
                    gc.moveTo(x * xFactor + 1.0,
                            canvas.getHeight() - doubleBuffer[x] * yFactor);
                    gc.lineTo((x + 1) * xFactor - INTER_BAND_SPACE - 1.0,
                            canvas.getHeight() - doubleBuffer[x] * yFactor);
                }
            }
            gc.closePath();
            gc.stroke();
        });
    }

}
