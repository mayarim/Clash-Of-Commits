package ooga.view;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ooga.view.screens.ChooseGameScreen;
import ooga.view.screens.OpenNewGameScreen;
import org.junit.jupiter.api.Test;
import util.DukeApplicationTest;

import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Melanie Wang
 */
public class OpenNewGameScreenTest extends DukeApplicationTest {
    ResourceBundle labels = ResourceBundle.getBundle(
            "ResourceBundles.LabelsBundle");


    @Override
    public void start(Stage stage) {
        OpenNewGameScreen ongs = new OpenNewGameScreen(stage, labels);
        stage.setScene(ongs.makeScene());
        stage.show();
    }

    @Test
    void testGameType1Transition(){
        VBox slotBox = lookup("#slotbox").query();
        GameSlot first = (GameSlot) slotBox.getChildren().get(1);
        clickOn(first);
        ToolBar hud = lookup("#HUD").query();
        Label score = (Label) hud.getItems().get(1);
        assertEquals("Score:  0", score.getText());
    }
}
