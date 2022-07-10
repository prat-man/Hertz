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

    requires GeniusLyricsAPI;


    opens in.pratanumandal.hertz.gui.core to javafx.base, javafx.fxml, javafx.graphics;
    exports in.pratanumandal.hertz;
}
