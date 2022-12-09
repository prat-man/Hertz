module in.pratanumandal.hertz {
    requires java.desktop;

    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;
    requires javafx.swing;

    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires commons.math3;

    requires com.google.gson;

    requires GeniusLyricsAPI;


    exports in.pratanumandal.hertz;
    opens in.pratanumandal.hertz.common to javafx.base, javafx.fxml, javafx.graphics;
    opens in.pratanumandal.hertz.controller to javafx.base, javafx.fxml, javafx.graphics;
}
