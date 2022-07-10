package in.pratanumandal.hertz.gui.core;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Objects;

public class Track {

    private final File file;

    private SimpleStringProperty title;
    private SimpleStringProperty artist;
    private SimpleStringProperty album;
    private SimpleStringProperty year;

    public Track(File file) {
        this.file = file;

        this.title = new SimpleStringProperty(FilenameUtils.removeExtension(file.getName()));
        this.artist = new SimpleStringProperty();
        this.album = new SimpleStringProperty();
        this.year = new SimpleStringProperty();

        Media media = new Media(file.toURI().toString());

        media.getMetadata().addListener((MapChangeListener<String, Object>) change -> {
            if (change.wasAdded()) {
                String key = change.getKey();
                Object value = change.getValueAdded();

                switch (key) {
                    case "title":
                        this.title.set(String.valueOf(value));
                        break;

                    case "artist":
                        this.artist.set(String.valueOf(value));
                        break;

                    case "album":
                        this.album.set(String.valueOf(value));
                        break;

                    case "year":
                        this.year.set(String.valueOf(value));
                        break;
                }
            }
        });

        try {
            new MediaPlayer(media);
        } catch(Exception e) {
            // DO NOTHING
        }
    }

    public File getFile() {
        return file;
    }

    public String getTitle() {
        return title.get();
    }

    @FXML public SimpleStringProperty titleProperty() {
        return title;
    }

    public String getArtist() {
        return artist.get();
    }

    @FXML public SimpleStringProperty artistProperty() {
        return artist;
    }

    public String getAlbum() {
        return album.get();
    }

    @FXML public SimpleStringProperty albumProperty() {
        return album;
    }

    public String getYear() {
        return year.get();
    }

    @FXML public SimpleStringProperty yearProperty() {
        return year;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Track track = (Track) o;
        return Objects.equals(file.getAbsolutePath(), track.file.getAbsolutePath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(file.getAbsolutePath());
    }

}
