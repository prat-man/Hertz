package in.pratanumandal.hertz.gui.core;

import in.pratanumandal.hertz.common.Constants;
import in.pratanumandal.hertz.gui.utils.GUIUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;

public class HertzApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(HertzApplication.class.getResource("/fxml/main.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle(Constants.APPLICATION_NAME);

        MainController controller = loader.getController();
        controller.setParentStage(primaryStage);

        primaryStage.setScene(new Scene(root));

        primaryStage.setOnCloseRequest(event -> {
            System.exit(0);
        });

        GUIUtils.setTheme(primaryStage);
        GUIUtils.setIcon(primaryStage);

        primaryStage.sizeToScene();

        primaryStage.show();

        primaryStage.setMaximized(true);
    }

    private static void loadFonts() {
        Font.loadFont(HertzApplication.class.getResourceAsStream("/fonts/OpenSans-Regular.ttf"), 12);
        Font.loadFont(HertzApplication.class.getResourceAsStream("/fonts/OpenSans-SemiBold.ttf"), 12);
        Font.loadFont(HertzApplication.class.getResourceAsStream("/fonts/VeraMono.ttf"), 12);
    }

    public static void main(String[] args) {
        loadFonts();
        launch(args);
    }

}
