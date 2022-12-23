package ooga.view.screens;


import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ooga.controller.Controller;
import ooga.view.GameSlot;
import ooga.view.MapWrapper;

import java.util.ResourceBundle;

/**
 * This screen is for when a player wishes to start a brand new game.
 * @author Melanie Wang
 */
public class OpenNewGameScreen extends SceneCreator {
    private GameSlot slot1;
    private GameSlot slot2;
    private GameSlot slot3;
    private GameSlot slot4;
    private StackPane background;
    private ResourceBundle labels;
    private ResourceBundle styles;
    private Stage stage;
    private int screenSize;

    /**
     * Constructor for OpenNewGameScreen
     * @param stage the stage the screen is set on
     * @param label the labels used in the screen (for language specificity)
     */
    public OpenNewGameScreen(Stage stage, ResourceBundle label){
        this.stage = stage;
        this.labels = label;
        styles = getStyles();
        screenSize = getScreenSize();
    }

    /**
     * Creates the scene for the screen
     * @return the scene
     */
    @Override
    public Scene makeScene(){
        background = new StackPane();
        slot1 = new GameSlot(labels.getString("game1"), labels);
        slot1.setId("slot1");
        slot2 = new GameSlot(labels.getString("game2"), labels);
        slot1.setId("slot2");
        slot3 = new GameSlot(labels.getString("game3"), labels);
        slot1.setId("slot3");
        slot4 = new GameSlot(labels.getString("game4"), labels);
        slot1.setId("slot4");
        VBox slots = new VBox(slot1,slot2,slot3,slot4);
        slots.getStyleClass().add("vbox");
        slots.setId("slotbox");
        slots.setAlignment(Pos.CENTER);
        background.setAlignment(Pos.CENTER);
        background.getChildren().add(slots);
        Scene scene = new Scene(background, screenSize, screenSize);
        handleEvents();
        scene.getStylesheets().add(styles.getString("openNewGameCSS"));
        return scene;
    }

    /**
     * Handles clicking on the slots
     */
    private void handleEvents(){
        slot1.setOnMouseClicked(event -> {
            Controller controller = new Controller(stage, labels.getString("game1Map"), labels.getString("game1"), labels);
            controller.startAnimation();
        });
        slot2.setOnMouseClicked(event -> {
            Controller controller = new Controller(stage, labels.getString("game2Map"), labels.getString("game2"), labels);
            controller.startAnimation();
        });
        slot3.setOnMouseClicked(event -> {
            Controller controller = new Controller(stage, labels.getString("game3Map"), labels.getString("game3"), labels);
            controller.startAnimation();
        });
        slot4.setOnMouseClicked(event -> {
            Controller controller = new Controller(stage, labels.getString("game4Map"), labels.getString("game4"), labels);
            controller.startAnimation();
        });
    }
}
