package in.pratanumandal.hertz.common;

import java.io.File;

public class Constants {

    public static final String APPLICATION_NAME = "Hertz Music Player";

    public static final File LIBRARY_LOCATION = getLibraryLocation();

    private static File getLibraryLocation() {
        return new File(System.getProperty("user.home") + "/.hertz/library.json");
    }

}
