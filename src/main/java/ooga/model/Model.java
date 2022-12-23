package ooga.model;

import ooga.controller.Controller;
import ooga.model.entities.Entity;
import ooga.model.state.DirectionState;
import ooga.model.state.MovementState;

import java.util.Map;

/**
 * @author Nick Ward
 */
public class Model {
    private Controller myController;
    private Map<String, Entity> myModelEntities;
    private Entity myHeroModel;
    private static final int ADDITIONAL_HP = 1;

    /**
     * Constructor for the Model class
     * @param controller the controller that this model is associated with
     */
    public Model(Controller controller) {
        myController = controller;
        setupGame();
    }

    /**
     * Method to set up the model for a new game
     */
    private void setupGame() {
        myModelEntities = myController.getModelEntities();
        myHeroModel = myModelEntities.get(myController.getMainHeroName());
    }

    /**
     * Method to change the entity direction and movement states
     * @param entityName the name of the entity whose states are being changed
     * @param movement the new movement state
     * @param direction the new direction state
     */
    public void changeEntityState(String entityName, DirectionState direction, MovementState movement) {
        Entity entity = myModelEntities.get(entityName);
        entity.changeMovement(movement);
        entity.changeDirection(direction);
    }

    /**
     * Method to change the entity movement state
     * @param entityName - entity's unique name String
     * @param movement - MovementState to change the entity to
     */
    public void changeEntityState(String entityName, MovementState movement) {
        Entity entity = myModelEntities.get(entityName);
        if(entity.isMoving()){
            entity.changeMovement(movement);
        }
    }

    /**
     * Changes HP of hero upon L "increase life" cheat key
     * @author Mayari Merchant
     */
    public void addLife(){
        myHeroModel.changeHp(ADDITIONAL_HP);
    }


    /**
     * Actives the attack of the hero
     */
    public void attack() {
        myHeroModel.attack().activateAttack();
    }

    /**
     * Checks if any new attacks have been created and adds them to the game
     */
    public void checkForNewAttacks() {
        for (Entity entity : myModelEntities.values()) {
            entity.checkAttack(myHeroModel.coordinates());
        }
    }
}
