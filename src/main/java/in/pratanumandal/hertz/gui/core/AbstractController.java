package in.pratanumandal.hertz.gui.core;

import javafx.stage.Stage;

public abstract class AbstractController {

    protected Stage parentStage;

    public Stage getParentStage() {
        return parentStage;
    }

    public void setParentStage(Stage parentStage) {
        this.parentStage = parentStage;
    }

}
