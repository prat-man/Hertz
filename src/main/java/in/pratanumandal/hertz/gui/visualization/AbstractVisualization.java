package in.pratanumandal.hertz.gui.visualization;

import javafx.scene.canvas.Canvas;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.MediaPlayer;

import java.util.stream.IntStream;

public abstract class AbstractVisualization implements AudioSpectrumListener {

    protected final MediaPlayer mediaPlayer;
    protected final Canvas canvas;

    private boolean initialized;

    public AbstractVisualization(MediaPlayer mediaPlayer, Canvas canvas) {
        this.mediaPlayer = mediaPlayer;
        this.canvas = canvas;
        this.initialized = false;
    }

    @Override
    public void spectrumDataUpdate(double timestamp, double duration, float[] magnitudes, float[] phases) {
        // store spectrum threshold to avoid interference with initialization
        int spectrumThreshold = mediaPlayer.getAudioSpectrumThreshold();

        // initialize visualization
        if (!this.initialized) {
            initialize();
            this.initialized = true;
        }

        // correct magnitudes based on audio spectrum threshold
        double[] correctedMagnitudes = IntStream.range(0, magnitudes.length)
                .mapToDouble(i -> magnitudes[i] - spectrumThreshold)
                .toArray();

        // update the visualization
        Thread thread = new Thread(() -> updateVisualization(correctedMagnitudes));
        thread.start();
    }

    public abstract void initialize();

    public abstract void updateVisualization(double[] magnitudes);

}
