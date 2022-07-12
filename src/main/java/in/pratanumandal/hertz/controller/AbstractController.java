package in.pratanumandal.hertz.controller;

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
