package in.pratanumandal.hertz.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import in.pratanumandal.hertz.common.Constants;
import in.pratanumandal.hertz.common.Track;
import in.pratanumandal.hertz.common.TrackAdapter;
import javafx.application.Platform;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Utils {

    public static boolean isExtension(File file, String... extensions) {
        String fileName = file.getName();
        for (String extension : extensions) {
            if (StringUtils.endsWithIgnoreCase(fileName, extension)) {
                return true;
            }
        }
        return false;
    }

    public static String millisToString(double doubleMillis) {
        long millis = (long) doubleMillis;
        long hours   = millis / (60 * 60 * 1000);
        millis       = millis % (60 * 60 * 1000);
        long minutes = millis / (60 * 1000);
        millis       = millis % (60 * 1000);
        long seconds = millis / 1000;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) sb.append(String.format("%02d:", hours));
        sb.append(String.format("%02d:%02d", minutes, seconds));

        return sb.toString();
    }

    public static void readTracks(List<Track> tracksList) {
        Thread thread = new Thread(() -> {
            String jsonTracksList = null;
            try {
                jsonTracksList = Files.readString(Constants.LIBRARY_LOCATION.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (jsonTracksList != null) {
                GsonBuilder builder = new GsonBuilder();
                builder.registerTypeAdapter(Track.class, new TrackAdapter());
                builder.setPrettyPrinting();
                Gson gson = builder.create();

                Track[] tracksArray = gson.fromJson(jsonTracksList, Track[].class);

                List<Track> loadedTracksList = Arrays.stream(tracksArray)
                        .filter(Objects::nonNull).toList();

                tracksList.addAll(loadedTracksList);
                writeTracks(tracksList);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public static void writeTracks(List<Track> tracksList) {
        Thread thread = new Thread(() -> {
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(Track.class, new TrackAdapter());
            builder.setPrettyPrinting();
            Gson gson = builder.create();

            String jsonTracksList = gson.toJson(tracksList);

            try {
                FileUtils.createParentDirectories(Constants.LIBRARY_LOCATION);
                Files.writeString(Constants.LIBRARY_LOCATION.toPath(), jsonTracksList);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

}
