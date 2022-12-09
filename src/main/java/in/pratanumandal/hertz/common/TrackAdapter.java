package in.pratanumandal.hertz.common;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import javafx.scene.media.MediaException;

import java.io.File;
import java.io.IOException;

public class TrackAdapter extends TypeAdapter<Track> {

    @Override
    public void write(JsonWriter jsonWriter, Track track) throws IOException {
        jsonWriter.beginObject();

        if (track.getFile() != null) {
            jsonWriter.name("file");
            jsonWriter.value(track.getFile().getAbsolutePath());
        }

        if (track.getTitle() != null) {
            jsonWriter.name("title");
            jsonWriter.value(track.getTitle());
        }

        if (track.getArtist() != null) {
            jsonWriter.name("artist");
            jsonWriter.value(track.getArtist());
        }

        if (track.getAlbum() != null) {
            jsonWriter.name("album");
            jsonWriter.value(track.getAlbum());
        }

        if (track.getYear() != null) {
            jsonWriter.name("year");
            jsonWriter.value(track.getYear());
        }

        jsonWriter.endObject();
    }

    @Override
    public Track read(JsonReader jsonReader) throws IOException {
        File file = null;
        String title = null;
        String artist = null;
        String album = null;
        String year = null;

        jsonReader.beginObject();

        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            String value = jsonReader.nextString();

            switch (name) {
                case "file":
                    file = new File(value);
                    break;

                case "title":
                    title = value;
                    break;

                case "artist":
                    artist = value;
                    break;

                case "album":
                    album = value;
                    break;

                case "year":
                    year = value;
                    break;
            }
        }

        jsonReader.endObject();

        try {
            return new Track(file, title, artist, album, year);
        }
        catch (MediaException e) {
            return null;
        }
    }

}
