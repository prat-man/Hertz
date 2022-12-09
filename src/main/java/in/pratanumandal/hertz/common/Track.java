package in.pratanumandal.hertz.common;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Objects;

public class Track {

    private final File file;
    private final Media media;

    private final SimpleStringProperty title;
    private final SimpleStringProperty artist;
    private final SimpleStringProperty album;
    private final SimpleStringProperty year;
    private final SimpleObjectProperty<Image> albumArt;

    public Track(File file) {
        this(file, FilenameUtils.removeExtension(file.getName()), null, null, null);
    }

    public Track(File file, String title, String artist, String album, String year) {
        this.file = file;

        this.title = new SimpleStringProperty(title);
        this.artist = new SimpleStringProperty(artist);
        this.album = new SimpleStringProperty(album);
        this.year = new SimpleStringProperty(year);
        this.albumArt = new SimpleObjectProperty<>();

        this.media = new Media(file.toURI().toString());

        updateMetadata();
    }

    private void updateMetadata() {
        this.media.getMetadata().addListener((MapChangeListener<String, Object>) change -> {
            if (change.wasAdded()) {
                String key = change.getKey();
                Object value = change.getValueAdded();

                switch (key) {
                    case "title":
                        String title = String.valueOf(value);
                        if (!title.isBlank()) this.title.set(title);
                        break;

                    case "artist":
                        String artist = String.valueOf(value);
                        if (!artist.isBlank()) this.artist.set(artist);
                        break;

                    case "album":
                        String album = String.valueOf(value);
                        if (!album.isBlank()) this.album.set(album);
                        break;

                    case "year":
                        String year = String.valueOf(value);
                        if (!year.isBlank()) this.year.set(year);
                        break;

                    case "image":
                        this.albumArt.set((Image) value);
                        break;
                }
            }
        });
    }

    public File getFile() {
        return file;
    }

    public Media getMedia() {
        return media;
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

    public Image getAlbumArt() {
        return albumArt.get();
    }

    @FXML public SimpleObjectProperty<Image> albumArtProperty() {
        return albumArt;
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
