package in.pratanumandal.hertz.gui.core;

import in.pratanumandal.hertz.gui.utils.GUIUtils;
import in.pratanumandal.hertz.gui.utils.detachabletabs.TabPaneDetacher;
import in.pratanumandal.hertz.gui.visualization.AmoebaVisualization;
import in.pratanumandal.hertz.gui.visualization.BarsVisualization;
import in.pratanumandal.hertz.gui.visualization.FireVisualization;
import in.pratanumandal.hertz.gui.visualization.Visualizations;
import in.pratanumandal.hertz.gui.visualization.WavesVisualization;
import in.pratanumandal.hertz.utils.Utils;
import in.pratanumandal.hertz.utils.debouncer.Debouncer;
import in.pratanumandal.hertz.utils.lyrics.Genius;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainController extends AbstractController {

    @FXML private BorderPane rootPane;
    @FXML private MenuBar menuBar;
    @FXML private CheckMenuItem albumArtMenu;
    @FXML private CheckMenuItem lyricsMenu;

    @FXML private TabPane tabPane;

    @FXML private Pane canvasPane;
    @FXML private Canvas canvas;

    @FXML private AnchorPane visualizationPane;

    @FXML private TextArea lyrics;

    @FXML private TableView libraryTable;
    @FXML private TableColumn titleColumn;
    @FXML private TableColumn artistColumn;
    @FXML private TableColumn albumColumn;
    @FXML private TableColumn yearColumn;
    @FXML private TextField search;

    @FXML private HBox libraryLoadingPane;
    @FXML private ProgressIndicator libraryLoadingIndicator;
    @FXML private Label libraryLoadingLabel;

    @FXML private HBox controlPane;
    @FXML private HBox controlPaneContainer;

    @FXML private ImageView albumArt;

    @FXML private VBox infoPane;
    @FXML private Label title;
    @FXML private Label artist;

    @FXML private HBox seekBarPane;
    @FXML private Slider seekBar;
    @FXML private HBox timePane;
    @FXML private Label currentTime;
    @FXML private Label duration;

    @FXML private Slider volumeBar;
    @FXML private ImageView muteImage;

    @FXML private Button playPauseButton;

    private MediaPlayer mediaPlayer;

    private InvalidationListener seekBarChangeListener;

    private ObservableList<Track> tracksList;
    private Track track;

    private Visualizations visualization = Visualizations.BARS;

    private boolean mute;
    private boolean showInverseTime;

    private SimpleIntegerProperty filesLoaded = new SimpleIntegerProperty();

    private Debouncer<Object> lyricsUpdater;

    @FXML
    public void initialize() {
        TabPaneDetacher.create().makeTabsDetachable(tabPane);

        canvas.widthProperty().bind(canvasPane.widthProperty());
        canvas.heightProperty().bind(canvasPane.heightProperty());

        seekBarChangeListener = (obs) -> {
            if (mediaPlayer != null) {
                mediaPlayer.seek(mediaPlayer.getTotalDuration().multiply(seekBar.getValue() / 100.0));
            }
        };

        seekBar.valueProperty().addListener(seekBarChangeListener);

        albumArt.fitWidthProperty().bind(controlPane.heightProperty());
        albumArt.fitHeightProperty().bind(controlPane.heightProperty());
        albumArt.managedProperty().bind(albumArt.visibleProperty());

        volumeBar.valueProperty().addListener((obs) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(volumeBar.getValue() / 100.0);
            }
        });

        titleColumn.prefWidthProperty().bind(libraryTable.widthProperty().multiply(0.35));
        artistColumn.prefWidthProperty().bind(libraryTable.widthProperty().multiply(0.25));
        albumColumn.prefWidthProperty().bind(libraryTable.widthProperty().multiply(0.25));
        yearColumn.prefWidthProperty().bind(libraryTable.widthProperty().multiply(0.15));

        libraryTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        libraryTable.setRowFactory((tableView) -> {
            TableRow<Track> row = new TableRow<>();

            // play track on double click
            row.setOnMouseClicked((event) -> {
                if (event.getClickCount() == 2 && !row.isEmpty() && this.track != row.getItem()) {
                    this.track = row.getItem();
                    playTrack();
                }
            });

            // create context menu
            MenuItem playItem = new MenuItem("Play");
            playItem.setOnAction(event -> {
                ObservableList<Track> tracks = libraryTable.getSelectionModel().getSelectedItems();
                Track track = tracks.get(0);
                if (this.track != track) {
                    this.track = track;
                    playTrack();
                }
            });

            MenuItem removeItem = new MenuItem("Remove");
            removeItem.setOnAction(event -> {
                ObservableList<Track> tracks = libraryTable.getSelectionModel().getSelectedItems();

                int lastTrackIndex = tracksList.indexOf(tracks.get(tracks.size() - 1));
                Track nextTrack = (lastTrackIndex < tracksList.size() - 1) ? tracksList.get(lastTrackIndex + 1) : null;

                boolean hasCurrentTrack = tracks.contains(track);

                tracksList.removeAll(tracks);
                libraryTable.getSelectionModel().clearSelection();

                if (hasCurrentTrack) {
                    if (nextTrack != null) {
                        this.track = nextTrack;
                        playTrack();
                        libraryTable.getSelectionModel().select(track);
                    }
                    else {
                        // TODO: Implement track removal algorithm for last track
                        stopTrack();

                    }
                }
            });

            SeparatorMenuItem separatorItem = new SeparatorMenuItem();

            MenuItem currentTrack = new MenuItem("Go to current track");
            currentTrack.setOnAction(event -> {
                libraryTable.getSelectionModel().clearSelection();
                libraryTable.getSelectionModel().select(track);
                libraryTable.scrollTo(track);
            });

            ContextMenu rowMenu = new ContextMenu();
            rowMenu.getItems().addAll(playItem, removeItem, separatorItem, currentTrack);

            // only display context menu for non-empty rows:
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(rowMenu));

            return row;
        });

        tracksList = FXCollections.observableArrayList();

        FilteredList<Track> filteredTracksList = new FilteredList<>(tracksList);
        search.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredTracksList.setPredicate(track -> {
                if (newVal == null || newVal.isEmpty()) {
                    return true;
                }

                String[] keywords = newVal.split("\\s+");

                for (String keyword : keywords) {
                    if (!(StringUtils.containsIgnoreCase(track.getTitle(), keyword) ||
                            StringUtils.containsIgnoreCase(track.getArtist(), keyword) ||
                            StringUtils.containsIgnoreCase(track.getAlbum(), keyword) ||
                            StringUtils.containsIgnoreCase(track.getYear(), keyword))) {
                        return false;
                    }
                }

                return true;
            });
        });

        SortedList<Track> sortedTracksList = new SortedList<>(filteredTracksList);
        sortedTracksList.comparatorProperty().bind(libraryTable.comparatorProperty());

        libraryTable.setItems(sortedTracksList);

        infoPane.managedProperty().bind(infoPane.visibleProperty());

        timePane.managedProperty().bind(timePane.visibleProperty());

        visualizationPane.setOnMouseClicked((event) -> {
            if (event.getClickCount() == 2 /*&& mediaPlayer != null &&
                    (mediaPlayer.getStatus().equals(MediaPlayer.Status.PLAYING) ||
                    mediaPlayer.getStatus().equals(MediaPlayer.Status.PAUSED))*/) {

                Stage visualizationStage = (Stage) visualizationPane.getScene().getWindow();

                visualizationPane.getChildren().remove(canvasPane);

                Scene scene = new Scene(canvasPane);

                Stage stage = new Stage();
                stage.setScene(scene);

                GUIUtils.setTheme(stage);
                GUIUtils.setIcon(stage);

                stage.initOwner(visualizationStage);
                stage.initStyle(StageStyle.UNDECORATED);
                stage.setMaximized(true);
                stage.setFullScreen(true);
                stage.setAlwaysOnTop(true);

                stage.fullScreenProperty().addListener((obs) -> {
                    stage.close();
                    visualizationPane.getChildren().add(canvasPane);
                });

                stage.focusedProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal) {
                        stage.setFullScreen(false);
                    }
                });

                stage.setX(visualizationStage.getX());
                stage.setY(visualizationStage.getY());

                stage.show();
            }
        });

        libraryLoadingPane.managedProperty().bind(libraryLoadingPane.visibleProperty());
        libraryLoadingIndicator.prefWidthProperty().bind(libraryLoadingLabel.heightProperty());
        libraryLoadingIndicator.prefHeightProperty().bind(libraryLoadingLabel.heightProperty());

        filesLoaded.addListener((obs, oldVal, newVal) -> {
            libraryLoadingLabel.setText("Discovered " + newVal + " files");
        });

        lyricsUpdater = new Debouncer<>((key) -> {
            Track track = this.track;
            String lyricsText = Genius.search(track.getTitle() + " " + track.getArtist());
            if (lyricsText != null && track == this.track) {
                Platform.runLater(() -> this.lyrics.setText(lyricsText));
            }
        }, 1000);
    }

    @FXML
    private void addFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Add Files");

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.m4a");
        fileChooser.getExtensionFilters().add(extFilter);

        List<File> files = fileChooser.showOpenMultipleDialog(parentStage);
        if (files != null) {
            addFiles(files);
        }
    }

    @FXML
    private void addFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Add Folder");

        File file = directoryChooser.showDialog(parentStage);
        if (file != null) {
            addFiles(Collections.singletonList(file));
        }
    }

    private void addFiles(List<File> files) {
        Thread thread = new Thread(() -> {
            libraryLoadingPane.setVisible(true);
            recursivelyAddFiles(files);
            libraryLoadingPane.setVisible(false);
            Platform.runLater(() -> filesLoaded.set(0));
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void recursivelyAddFiles(List<File> files) {
        for (File file : files) {
            if (file.isDirectory()) {
                recursivelyAddFiles(Arrays.asList(file.listFiles()));
            }
            else if (Utils.isExtension(file, ".mp3", ".wav", ".m4a")) {
                Track track = new Track(file);

                if (!tracksList.contains(track)) {
                    Platform.runLater(() -> {
                        tracksList.add(track);
                        filesLoaded.set(filesLoaded.get() + 1);
                    });
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void playTrack() {
        stopTrack();

        Media media = new Media(track.getFile().toURI().toString());

        showAlbumArt(null);

        this.title.setText(FilenameUtils.removeExtension(track.getFile().getName()));
        this.artist.setText("");

        media.getMetadata().addListener((MapChangeListener<String, Object>) change -> {
            if (change.wasAdded()) {
                String key = change.getKey();
                Object value = change.getValueAdded();
                switch (key) {
                    case "title":
                        this.title.setText(String.valueOf(value));
                        updateLyrics();
                        break;

                    case "artist":
                        this.artist.setText(String.valueOf(value));
                        break;

                    case "image":
                        showAlbumArt((Image) value);
                        break;
                }
            }
        });

        mediaPlayer = new MediaPlayer(media);

        mediaPlayer.setVolume(volumeBar.getValue() / 100.0);
        mediaPlayer.setMute(mute);

        mediaPlayer.setOnEndOfMedia(() -> {
            stopTrack();
            nextTrack();
        });

        showVisualization();

        mediaPlayer.currentTimeProperty().addListener((obs) -> {
            updateSeekBar();
            updateTime();
        });

        playPauseTrack();

        infoPane.setVisible(true);
        timePane.setVisible(true);
    }

    @FXML
    public void playPauseTrack() {
        if (mediaPlayer == null) return;

        switch (mediaPlayer.getStatus()) {
            case UNKNOWN:
            case READY:
            case PAUSED:
                mediaPlayer.play();
                playPauseButton.getStyleClass().removeAll("play");
                playPauseButton.getStyleClass().add("pause");
                break;

            case STALLED:
            case PLAYING:
                mediaPlayer.pause();
                playPauseButton.getStyleClass().removeAll("pause");
                playPauseButton.getStyleClass().add("play");
                break;

            case STOPPED:
            case DISPOSED:
                playTrack();
                break;
        }
    }

    @FXML
    public void stopTrack() {
        MediaPlayer mediaPlayer = this.mediaPlayer;

        if (mediaPlayer != null &&
                mediaPlayer.getStatus() != MediaPlayer.Status.STOPPED &&
                mediaPlayer.getStatus() != MediaPlayer.Status.DISPOSED) {

            mediaPlayer.stop();
            mediaPlayer.setOnStopped(() -> mediaPlayer.dispose());

            mediaPlayer.setAudioSpectrumListener(null);
            clearVisualization();

            resetSeekBar();
            resetTime();
            resetLyrics();

            playPauseButton.getStyleClass().removeAll("pause");
            playPauseButton.getStyleClass().add("play");

            timePane.setVisible(false);
        }
    }

    @FXML
    public void previousTrack() {
        int index = tracksList.indexOf(track);
        if (index > 0) {
            track = tracksList.get(index - 1);
            playTrack();
        }
    }

    @FXML
    public void nextTrack() {
        int index = tracksList.indexOf(track);
        if (index < tracksList.size() - 1) {
            track = tracksList.get(index + 1);
            playTrack();
        }
    }

    @FXML
    public void toggleMute() {
        mute = !mute;
        if (mute) {
            muteImage.getStyleClass().removeAll("unmute");
            muteImage.getStyleClass().add("mute");
        }
        else {
            muteImage.getStyleClass().removeAll("mute");
            muteImage.getStyleClass().add("unmute");
        }
        if (mediaPlayer != null) {
            mediaPlayer.setMute(mute);
        }
    }

    @FXML
    public void toggleAlbumArt() {
        if (albumArt.getImage() == null || !albumArtMenu.isSelected()) {
            albumArt.setVisible(false);

            controlPaneContainer.setStyle("-fx-background-color: transparent");

            title.setStyle("-fx-text-fill: black");
            artist.setStyle("-fx-text-fill: black");
        } else {
            albumArt.setVisible(true);

            Color backgroundColor = GUIUtils.getDominantColor(albumArt.getImage());
            controlPaneContainer.setStyle("-fx-background-color: linear-gradient(from 25% 25% to 100% 100%, transparent, " + GUIUtils.toRGBCode(backgroundColor) + ")");

            Color foregroundColor = GUIUtils.getTextColor(backgroundColor);
            title.setStyle("-fx-text-fill: " + GUIUtils.toRGBCode(foregroundColor));
            artist.setStyle("-fx-text-fill: " + GUIUtils.toRGBCode(foregroundColor));
        }
    }

    private void showAlbumArt(Image image) {
        Platform.runLater(() -> {
            albumArt.setImage(image);
            toggleAlbumArt();
        });
    }

    @FXML
    public void toggleLyrics() {
        updateLyrics();
    }

    @FXML
    public void toggleShowInverseTime() {
        showInverseTime = !showInverseTime;
    }

    @FXML
    public void noVisualization() {
        this.visualization = Visualizations.NONE;
        showVisualization();
    }

    @FXML
    public void amoebaVisualization() {
        this.visualization = Visualizations.AMOEBA;
        showVisualization();
    }

    @FXML
    public void barsVisualization() {
        this.visualization = Visualizations.BARS;
        showVisualization();
    }

    @FXML
    public void fireVisualization() {
        this.visualization = Visualizations.FIRE;
        showVisualization();
    }

    @FXML
    public void wavesVisualization() {
        this.visualization = Visualizations.WAVES;
        showVisualization();
    }

    /**
     * Switch to selected visualization
     */
    private void showVisualization() {
        if (mediaPlayer != null) {
            switch (visualization) {
                case NONE:
                    mediaPlayer.setAudioSpectrumListener(null);
                    clearVisualization();
                    break;

                case AMOEBA:
                    mediaPlayer.setAudioSpectrumListener(new AmoebaVisualization(mediaPlayer, canvas));
                    break;

                case BARS:
                    mediaPlayer.setAudioSpectrumListener(new BarsVisualization(mediaPlayer, canvas));
                    break;

                case FIRE:
                    mediaPlayer.setAudioSpectrumListener(new FireVisualization(mediaPlayer, canvas));
                    break;

                case WAVES:
                    mediaPlayer.setAudioSpectrumListener(new WavesVisualization(mediaPlayer, canvas));
                    break;
            }
        }
    }

    private void clearVisualization() {
        Platform.runLater(() -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        });
    }

    private void resetSeekBar() {
        Platform.runLater(() -> seekBar.setValue(0));
    }

    private void updateSeekBar() {
        Platform.runLater(() -> {
            seekBar.valueProperty().removeListener(seekBarChangeListener);

            seekBar.setValue((mediaPlayer.getCurrentTime().toMillis() /
                    mediaPlayer.getTotalDuration().toMillis()) * 100.0);

            seekBar.valueProperty().addListener(seekBarChangeListener);
        });
    }

    private void resetTime() {
        Platform.runLater(() -> {
            currentTime.setText("");
            duration.setText("");
        });
    }

    private void updateTime() {
        Platform.runLater(() -> {
            if (showInverseTime) {
                currentTime.setText("-" +
                        Utils.millisToString(mediaPlayer.getTotalDuration().toMillis() -
                                mediaPlayer.getCurrentTime().toMillis()));
            } else {
                currentTime.setText(Utils.millisToString(mediaPlayer.getCurrentTime().toMillis()));
            }

            duration.setText(Utils.millisToString(mediaPlayer.getTotalDuration().toMillis()));
        });
    }

    private void resetLyrics() {
        Platform.runLater(() -> this.lyrics.setText(""));
    }

    private void updateLyrics() {
        if (lyricsMenu.isSelected()) {
            lyricsUpdater.call(this);
        }
        else {
            resetLyrics();
        }
    }

    /**
     * Adjust minimum height of parent stage
     */
    private void adjustMinHeight() {
        parentStage.setMinHeight(parentStage.getHeight() - rootPane.getHeight() +
                controlPaneContainer.getHeight() + seekBarPane.getHeight() +
                menuBar.getHeight());
    }

    /**
     * Set parent stage and setup parent stage specific configurations
     *
     * @param parentStage the parent stage
     */
    @Override
    public void setParentStage(Stage parentStage) {
        super.setParentStage(parentStage);

        parentStage.showingProperty().addListener((obsShowing) -> {
            adjustMinHeight();
            controlPaneContainer.heightProperty().addListener((obs) -> adjustMinHeight());
            seekBarPane.heightProperty().addListener((obs) -> adjustMinHeight());
        });

        ChangeListener clearVisualizationListener = (obs, oldVal, newVal) -> {
            if (mediaPlayer != null && mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
                clearVisualization();
            }
        };

        parentStage.widthProperty().addListener(clearVisualizationListener);
        parentStage.heightProperty().addListener(clearVisualizationListener);
    }

}
