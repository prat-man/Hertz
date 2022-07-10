package in.pratanumandal.hertz.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

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

}
