package in.pratanumandal.hertz.gui.utils;

import in.pratanumandal.hertz.gui.utils.colorthief.ColorThief;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;

public class GUIUtils {

    public static void setTheme(Stage stage) {
        String theme = GUIUtils.class.getResource("/style/main.css").toExternalForm();
        stage.getScene().getStylesheets().add(theme);
    }

    public static void setIcon(Stage stage) {
        Image image = new Image(GUIUtils.class.getResourceAsStream("/images/icon.png"));
        stage.getIcons().add(image);
    }

    public static Color getDominantColor(Image image) {
        BufferedImage bufferedImage = new BufferedImage((int) image.getWidth(),
                (int) image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);

        SwingFXUtils.fromFXImage(image, bufferedImage);

        int[] rgb = ColorThief.getColor(bufferedImage);

        return Color.rgb(rgb[0], rgb[1], rgb[2]);
    }

    public static String toRGBCode(Color color) {
        return String.format( "#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    public static Color getTextColor(Color color) {
        Color invertedColor = Color.rgb(
                (int) (255 - color.getRed() * 255),
                (int) (255 - color.getGreen() * 255),
                (int) (255 - color.getBlue() * 255));

        if (invertedColor.getBrightness() > 0.5) {
            return Color.WHITE;
        }

        return Color.BLACK;
    }

}
