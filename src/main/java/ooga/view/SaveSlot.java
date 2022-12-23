package ooga.view;

import javafx.scene.control.Label;
import ooga.controller.SaveFileParser;

import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * @author Melanie Wang
 */

public class SaveSlot extends Slot{
    private SubLabel time;

    private String timeContent;
    private ResourceBundle labels;

    private String gameTypeContent;
    private SubLabel gameType;

    private SubLabel mapName;

    private String mapNameContent;

    private int slotNumber;

    private SubLabel webNotice;
    private SaveFileParser saveFileParser= new SaveFileParser();

    /**
     * The save slot is the physical "slot" that loads and saves games. There will always be three slots.
     * @param label the resource bundle for the labels
     * @param number the number of the slot
     */
    public SaveSlot(ResourceBundle label, int number, boolean web){
        super(label);
        labels = label;
        slotNumber = number;
        try {
            saveFileParser.loadSaveInformation(number);
            timeContent = saveFileParser.getTimeDate();
            gameTypeContent = saveFileParser.getGameType();
            mapNameContent = saveFileParser.getMapName();
        } catch (IllegalStateException e) {
            timeContent = labels.getString("noSave");
            gameTypeContent = labels.getString("noSave");
            mapNameContent = labels.getString("noSave");
        }
        if(web) {
            webNotice= new SubLabel(labels.getString("webLoad"));
            this.getChildren().add(webNotice);
        }
        else {
            DateTimeFormatter m = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
            time = new SubLabel(String.format("%s %s", labels.getString("time"), timeContent));
            gameType = new SubLabel(String.format("%s %s", labels.getString("gameType"), gameTypeContent));
            mapName = new SubLabel(String.format("%s %s", labels.getString("mapName"), mapNameContent));
            this.getChildren().addAll(gameType, mapName, time);
        }
            this.getStyleClass().add("SaveSlot");
    }

    /**
     * Gets the save slot number
     * @return the save slot number
     */
    public int getNumber() {
        return slotNumber;
    }

    /**
     * Gets game type
     * @return game type
     */
    public String getGameType(){
        return gameTypeContent;
    }
}

