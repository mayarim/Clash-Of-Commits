package ooga.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.util.Duration;
import ooga.model.entities.Entity;
import ooga.model.Model;
import ooga.model.attack.Attack;
import ooga.model.obstacle.DestroyableWall;
import ooga.model.obstacle.Obstacle;
import ooga.model.powerup.PowerUp;
import ooga.model.state.DirectionState;
import ooga.model.state.MovementState;
import ooga.view.BlockView;
import ooga.view.AttackView;
import ooga.view.EntityView;
import ooga.view.MapWrapper;
import ooga.view.View;

import java.io.FileNotFoundException;
import java.util.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ooga.view.screens.StartScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static ooga.controller.MovementHandler.MOVEMENT_ACTIONS;
import static ooga.controller.MovementHandler.ATTACK_ACTIONS;
import static ooga.controller.MovementHandler.CHEAT_CODE_ACTIONS;

/**
 * @author Nick Ward, Melanie Wang, Nicki Lee
 */
public class Controller {
    private static final ResourceBundle scores = ResourceBundle.getBundle(
        "ResourceBundles.Score");
    private Timeline animation;
    private View myView;
    private int score;
    private Model myModel;
    private MapWrapper mapWrapper;
    private boolean animationPlaying;
    private static final double FRAMES_PER_SECOND = 60;
    private static final double SECOND_DELAY = 1.0 / FRAMES_PER_SECOND;
    private Map<String, EntityView> myViewEntities;
    private Map<String, Entity> myModelEntities;
    private Map<List<Double>, Obstacle> myModelObstacles;
    private Map<List<Double>, PowerUp> myModelPowerUps;
    private Map<List<Integer>, BlockView> myViewPowerUps;
    private Map<List<Double>, BlockView> myViewObstacles;
    private Map<Integer, Attack> myModelAttacks;
    private Map<Integer, AttackView> myViewAttacks;
    private String myMainHeroName;
    private MovementHandler movementHandler;
    private String myGameType;
    private String mapName;
    private Stage myStage;
    private DirectionState playerDirection;
    private List<Double> newCoordinates;
    private ResourceBundle myLabels;
    private SaveFileParser saver = new SaveFileParser();
    private boolean errorOccurred;
    private static final Logger LOG = LogManager.getLogger(Controller.class);

    /**
     * Constructor for the controller, which initializes the model and view and sets up map based on map name
     * @param stage the stage to display the game on
     * @param map the name of the map to be displayed
     * @param labels the resource bundle containing the labels for the game
     */
    public Controller(Stage stage, String map, String gameType, ResourceBundle labels) {
        this.myModelEntities = new HashMap<>();
        this.myViewEntities = new HashMap<>();
        this.myModelAttacks = new HashMap<>();
        this.myViewAttacks = new HashMap<>();
        this.myModelObstacles = new HashMap<>();
        this.myModelPowerUps = new HashMap<>();
        this.myViewPowerUps = new HashMap<>();
        this.mapName = map;
        this.myGameType = gameType;
        this.myStage = stage;
        this.myLabels = labels;
        this.score = Integer.parseInt(scores.getString("initialScore"));
        movementHandler = new MovementHandler(this, myMainHeroName);

        try {
            initializeModel();
            setupViewPowerUps();
            myView = new View(stage, this, myGameType, labels);
            myView.setViewPowerUps();
            myViewObstacles = myView.getViewObstacles();
        } catch (IllegalStateException e){
            errorOccurred = true;
            showMessage(Alert.AlertType.ERROR, labels.getString(e.getMessage()), e);
            LOG.error(e);
        }

    }

    /**
     * Initializes the model and parses all the data based on the map name given
     */
    private void initializeModel() throws IllegalStateException {
        boolean loadSave = false;
        if (mapName.equals("Save_4")){
            loadGameFromWeb();
            loadGame(Integer.parseInt(String.valueOf(mapName.charAt(mapName.length()-1))));
            loadSave = true;
        }
        else if (mapName.startsWith("Save")) {
            loadGame(Integer.parseInt(String.valueOf(mapName.charAt(mapName.length()-1))));
            loadSave = true;
        }
        parseData(mapName, loadSave);

        myModel = new Model(this);
    }

    /**
     * Begins the animation of the game
     */
    public void startAnimation() {
        if (!errorOccurred){
            animation = new Timeline();
            animation.setCycleCount(Timeline.INDEFINITE);
            animation.getKeyFrames().add(new KeyFrame(Duration.seconds(SECOND_DELAY), e->step(SECOND_DELAY)));
            animation.play();
        }
    }

    /**
     * Pauses the animation of the game
     */
    public void pauseAnimation(){
        animationPlaying = true;
        animation.pause();
    }

    /**
     * Resumes the animation of the game
     */
    public void playAnimation(){
        animationPlaying = false;
        animation.play();
    }

    /**
     * Stops the animation of the game
     */
    public void stopAnimation() {
        animation.stop();
    }

    /**
     * Steps the animation of the game
     * @param elapsedTime the time elapsed since the last step
     */
    private void step(double elapsedTime) {
        try {
            myView.step(elapsedTime);
            updateEntityPosition(elapsedTime);
            updateAttackPosition(elapsedTime);
            myModel.checkForNewAttacks();
            updatePlayerHealth();
            updatePlayerScore();
            myView.getGameScreen().nextScene();
        } catch (IllegalStateException e) {
            showMessage(Alert.AlertType.ERROR, myLabels.getString(e.getMessage()), e);
        }
    }

    /**
     * Updates the position of all entities in the game with their view and model counterparts
     * @param elapsedTime the time elapsed since the last step
     */
    private void updateEntityPosition(double elapsedTime) {
        List<EntityView> nowDead = new ArrayList<>();
        for (String entityName : myModelEntities.keySet()) {
            Entity modelEntity = myModelEntities.get(entityName);
            EntityView viewEntity = myViewEntities.get(entityName);
            if (modelEntity.getHp() > 0) {
                List<Double> newPosition = modelEntity.move(elapsedTime);
                viewEntity.setX(newPosition.get(0));
                viewEntity.setY(newPosition.get(1));
            } else {
                nowDead.add(viewEntity);
                score += Integer.parseInt(scores.getString(modelEntity.getMyAttributes().getOrDefault("EntityType", "Enemy").toLowerCase()));
            }
        }
        for (EntityView deadEntityView : nowDead) {
            removeEntity(deadEntityView.getKey());
        }
    }

    /**
     * Updates the position of all attacks in the game with their view and model counterparts
     * @param elapsedTime the time elapsed since the last step
     */
    private void updateAttackPosition(double elapsedTime) {
        List<Integer> attackIDs = myModelAttacks.keySet().stream().toList();
        for (Integer attackID : attackIDs) {
            Attack modelAttack = myModelAttacks.get(attackID);
            if (myViewAttacks.containsKey(attackID)) {
                AttackView viewAttack = myViewAttacks.get(attackID);
                List<Double> newPosition = modelAttack.move(elapsedTime);
                viewAttack.setX(newPosition.get(0) - viewAttack.getFitWidth() / 2);
                viewAttack.setY(newPosition.get(1) - viewAttack.getFitHeight() / 2);
            } else {
                AttackView newAttackView = createViewAttack(modelAttack, attackID);
                myViewAttacks.put(attackID, newAttackView);
                myView.getGameScreen().addAttackToScene(newAttackView);
            }
        }
    }

    /**
     * Checks if any obstacles have been destroyed and removes them from the game
     */
    private void updateObstacles() {
        List<List<Double>> coordinates = myViewObstacles.keySet().stream().toList();
        for (List<Double> coordinate : coordinates) {
            newCoordinates = new ArrayList<>();
            for (int index = coordinate.size() - 1; index >= 0; index--) {
                newCoordinates.add(coordinate.get(index));
            }

            if (myModelObstacles.get(newCoordinates).getClass() == DestroyableWall.class) {
                if (((DestroyableWall) myModelObstacles.get(newCoordinates)).determineHP() <= 0) {
                    removeObstacle(coordinate, newCoordinates);
                }
            }
        }
    }

    private void updatePowerUps() {
        List<List<Double>> coordinates = myModelPowerUps.keySet().stream().toList();
        for (List<Double> coordinate : coordinates) {
            if (!myModelPowerUps.get(coordinate).available()) {
                removePowerUp(coordinate);
            }
        }
    }

    /**
     * Parses all the data in the data files based on a certain map name
     * @param map the name of the map to be parsed
     */
    private void parseData(String map, boolean loadSave) throws IllegalStateException {
        MapParser mapParser = new MapParser(map);
        mapWrapper = mapParser.getMapWrapper();
        Map<Integer, String> stateToImageMap = mapParser.getStateToImageMap();
        mapWrapper.setStateToImageMap(stateToImageMap);
        mapWrapper.setVisualProperties(mapParser.getMapProperties());
        mapWrapper.setObstacleStateMap(mapParser.getObstacleStateMap());

        if (!loadSave) {
            EntityMapParser entityMapParser = new EntityMapParser(myLabels.getString(String.format("EntityMap_%s", map)));
            myModelEntities = entityMapParser.getEntities();
            PowerUpParser powerUpMapParser = new PowerUpParser(myLabels.getString(String.format("PowerUpMap_%s", map)));
            myModelPowerUps = powerUpMapParser.getPowerUps();
        }

        for (Entity entity : myModelEntities.values()) {
            if (entity.getMyAttributes().get("EntityType").equals("MainHero") || entity.getMyAttributes().get("EntityType").equals("Link")) {
                myMainHeroName = entity.getMyAttributes().get("Name");
            }
        }
        setupViewEntities();
        setupModelObstacles(mapParser);
        Attack.setMyController(this);
    }

    /**
     * Sets up the model obstacles based on the map parser
     * @param num
     */
    public void saveGame(int num) throws IllegalStateException {
        saver.saveGame(num, myModelEntities, myModelPowerUps, mapName, myGameType, String.valueOf(myModelEntities.get(myMainHeroName).getHp()), String.valueOf(score));
    }

    /**
     * saves game to online database (slot 4)
     * @param num the number of the slot
     */
    public void saveGameToWeb(int num) throws IllegalStateException {
        saver.saveGameToWeb(num, myModelEntities, myModelPowerUps, mapName, myGameType, String.valueOf(myModelEntities.get(myMainHeroName).getHp()), String.valueOf(score));
    }

    /**
     * calls upon the save file parser to load the game from the web
     */
    public void loadGameFromWeb() throws IllegalStateException {
        saver.loadGameFromWeb();
    }

    /**
     * Loads a game from a save file
     * @param num
     */
    public void loadGame(int num){
        saver.loadGame(num);
        this.myGameType = saver.getGameType();

        myModelEntities = saver.getEntities();
        myModelPowerUps = saver.getPowerUps();
        for (Entity entity : myModelEntities.values()) {
            if (entity.getMyAttributes().get("EntityType").equals("MainHero") || entity.getMyAttributes().get("EntityType").equals("Link")) {
                myMainHeroName = entity.getMyAttributes().get("Name");
            }
        }
        myModelEntities.get(myMainHeroName).setHP(Integer.parseInt(saver.getHeroHP()));
        score = Integer.parseInt(saver.getSaveScore());
        mapName = saver.getMapName();
    }

    /**
     * Sets up the view entities based on the model entities
     */
    private void setupModelObstacles(MapParser parser) throws IllegalStateException {
        for (int row = 0; row < mapWrapper.getRowSize(0); row++) {
            for (int col = 0; col < mapWrapper.getColumnSize(); col++) {
                try {
                    int thisState = mapWrapper.getState(col,row);
                    if (mapWrapper.getObstacleFromState(thisState).contains("Wall") || mapWrapper.getObstacleFromState(thisState).contains("PowerUp")) {
                        ResourceBundle obstacleBundle = ResourceBundle.getBundle("ResourceBundles.Obstacle");
                        String obstacleStateString = parser.getObstacleStateMap().get(thisState);
                        Class obstacleClass = Class.forName(obstacleBundle.getString(obstacleStateString));
                        makeObstacle(obstacleClass, row, col);
                    }
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("noClassFound", e);
                }
            }
        }
    }

    /**
     * Sets up the view entities based on the model entities
     */
    private void setupViewEntities() {
        myModelEntities.forEach((key, value) -> {
            myViewEntities.put(key, createViewEntity(value));
        });
    }

    /**
     * Sets up the view powerups based on the model powerups
     */
    public void setupViewPowerUps() {
        myModelPowerUps.forEach((key, value) -> {
            List<Integer> coordinates = Arrays.asList(key.get(0).intValue(), key.get(1).intValue());
            myViewPowerUps.put(coordinates, createViewPowerUp(value));
        });
    }

    /**
     * Creates a view entity based on a model entity
     * @param entity the model entity to be converted to a view entity
     * @return the view entity created
     */
    private EntityView createViewEntity(Entity entity){
        Map<String, String> entityAttributes = entity.getMyAttributes();
        String imageName = entityAttributes.get("Name");
        double xPos = Double.parseDouble(entityAttributes.get("XPosition"));
        double yPos = Double.parseDouble(entityAttributes.get("YPosition"));
        int size = Integer.parseInt(entityAttributes.get("Size"));
        String spriteLocation = entityAttributes.get("Sprites");
        String startingDirection = entityAttributes.get("Direction");
        return new EntityView(spriteLocation, startingDirection, imageName, xPos, yPos, size, size);
    }

    private BlockView createViewPowerUp(PowerUp powerup) {
        int x = powerup.getKey().get(0);
        int y = powerup.getKey().get(1);
        int size = mapWrapper.getVisualProperties().get(0).intValue();
        String path = powerup.getImagePath();
        return new BlockView(x,y,size,0, path, "PowerUp");
    }

    /**
     * Creates a view attack based on a model attack
     * @param attack the model attack to be converted to a view attack
     * @return the view attack created
     */
    private AttackView createViewAttack(Attack attack, int attackID){
        String imagePath = new AttackParser(attack.getMyEntity()).getImagePath();
        imagePath = String.format("%s%s.png", imagePath, attack.getDirection().getDirectionString());
        String attackType = attack.getClass().getSimpleName();
        double size = Double.parseDouble("" + attack.getMyAttributes().get("Size"));
        return new AttackView(imagePath, attackType, attack.coordinates().get(0), attack.coordinates().get(1), (int) size, (int) size, attackID);
    }

    /**
     * Creates an obstacle based on a class and a location
     * @param obstacleClass the class of the obstacle to be created
     * @param xPosition the x position of the obstacle
     * @param yPosition the y position of the obstacle
     */
    private Obstacle makeObstacle(Class<? extends Obstacle> obstacleClass, double xPosition, double yPosition) throws IllegalStateException {
        try {
            Obstacle newObstacle = obstacleClass.getConstructor(Double.class, Double.class).newInstance(xPosition, yPosition);
            myModelObstacles.put(Arrays.asList(xPosition, yPosition), newObstacle);
            return newObstacle;
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new IllegalStateException("noClassFound", e);
        }
    }

    /**
     * Removes an entity in model and view based on the entity name
     * @param entityName
     */
    public void removeEntity(String entityName){
        if (entityName != myMainHeroName) {
            myView.getGameScreen().removeEntityFromScene(entityName);
            myModelEntities.remove(entityName);
            myViewEntities.remove(entityName);
        }
        else {
            myView.getGameScreen().nextScene();
            stopAnimation();
        }
    }

    /**
     * Removes an attack in model and view based on the attack ID
     * @param attackID
     */
    public void removeAttack(Integer attackID) {
        myView.getGameScreen().removeAttackFromScene(myViewAttacks.get(attackID));
        myViewAttacks.remove(attackID);
        myModelAttacks.remove(attackID);
    }

    /**
     * Removes an obstacle in model and view based on the obstacle location
     * @param viewCoordinate
     * @param modelCoordinate
     */
    private void removeObstacle(List<Double> viewCoordinate, List<Double> modelCoordinate) {
        double blockSize = mapWrapper.getVisualProperties().get(0);
        myView.getGameScreen().removeObstacleFromScene(myViewObstacles.get(viewCoordinate));
        myViewObstacles.remove(viewCoordinate);
        myModelObstacles.remove(modelCoordinate);
    }

    private void removePowerUp(List<Double> coordinate) {
        myModelPowerUps.remove(coordinate);
        List<Integer> intCoordinate = Arrays.asList(coordinate.get(0).intValue(), coordinate.get(1).intValue());
        myView.getGameScreen().removePowerUpFromScene(myViewPowerUps.get(intCoordinate));
        myViewPowerUps.remove(intCoordinate);
    }

    /**
     * Translates the collision of an entity with an obstacle
     * @param viewObject1
     * @param viewObject2
     */
    public void passCollision(Object viewObject1, Object viewObject2) throws IllegalStateException {
        CollisionHandler handler = new CollisionHandler(getViewModelMaps());
        Map<?,?> modelMap1 = getCorrectModelMap(viewObject1);
        Map<?,?> modelMap2 = getCorrectModelMap(viewObject2);
        handler.translateCollision(viewObject1, viewObject2, modelMap1, modelMap2);
        updateObstacles();
        updatePowerUps();
    }

    /**
     * Gives the correct model map based on the view object
     * @param obj the view object
     * @return the correct model map
     */
    private Map<?,?> getCorrectModelMap(Object obj) throws IllegalStateException {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("ResourceBundles.ViewToModel");
            String viewType = obj.getClass().getSimpleName();
            if (obj.getClass() == BlockView.class) {
                viewType += ((BlockView) obj).getBlockViewType();
            }
            String objType = bundle.getString(viewType);
            Object mapObject = Controller.class.getDeclaredMethod(String.format("getModel%s", objType)).invoke(this);
            return (Map<?,?>) mapObject;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("noClassFound", e);
        }
    }

    /**
     * Checks if the key pressed is associated with a movement or attack action and passes it into handleKeyPress appropriately
     * Calls changeHeroAttack if attack action was pressed
     * @param keyCode
     */
    public void checkKeyPress(KeyCode keyCode) {
        if (MOVEMENT_ACTIONS.containsKey(keyCode)) {
            movementHandler.handleKeyPress(MOVEMENT_ACTIONS.get(keyCode));
        } else if (ATTACK_ACTIONS.containsKey(keyCode)) {
            changeHeroAttack(keyCode);
            movementHandler.handleKeyPress(ATTACK_ACTIONS.get(keyCode));
        } else if(CHEAT_CODE_ACTIONS.containsKey(keyCode)) {
            movementHandler.handleKeyPress(CHEAT_CODE_ACTIONS.get(keyCode));
        }
    }

    /**
     * Changes the type of attack associated with the hero
     * @param keyCode
     */
    private void changeHeroAttack(KeyCode keyCode) {
        if (!keyCode.equals(KeyCode.Z)) {
            myModelEntities.get(myMainHeroName).setAttackType("LongRange");
        } else {
            myModelEntities.get(myMainHeroName).setAttackType("ShortRange");
        }
    }



    /**
     * Checks if the key released is associated with a movement or attack action and passes it into handleKeyRelease appropriately
     * @param keyCode
     */
    public void checkKeyRelease(KeyCode keyCode) {
        if (MOVEMENT_ACTIONS.containsKey(keyCode)) {
            movementHandler.handleKeyRelease(MOVEMENT_ACTIONS.get(keyCode));
        } else if (ATTACK_ACTIONS.containsKey(keyCode)) {
            movementHandler.handleKeyRelease(ATTACK_ACTIONS.get(keyCode));
        }
        /* else if(cheatCodeActions.containsKey(keyCode)) {
            movementHandler.handleKeyRelease(cheatCodeActions.get(keyCode));
        }*/
    }


    /**
     * Tells the model to attack and changes the state to attack
     */
    public void attack(){
        myModel.attack();
        myView.changeEntityState(myMainHeroName, playerDirection, MovementState.ATTACK);
    }
    public void crossAttack(){
        //myModel.crossAttack();
        myView.changeEntityState(myMainHeroName,playerDirection,MovementState.ATTACK);
    }

    public void changeEntityState(String entityName, DirectionState direction, MovementState movement){
        playerDirection = direction;
        myModel.changeEntityState(myMainHeroName, direction, movement);
        myView.changeEntityState(myMainHeroName, direction, movement);
    }
    public void changeEntityState(String entityName, MovementState movement){
        myModel.changeEntityState(myMainHeroName, playerDirection, movement);
        myView.changeEntityState(myMainHeroName, playerDirection, movement);
    }




    /**
     * Method that returns the name of the main hero, if it exists
     * @return the name of the main hero
     */
    public String getMainHeroName() throws IllegalStateException {
        if (myMainHeroName == null) {
            throw new IllegalStateException("noMainHeroFound");
        } else {
            return myMainHeroName;
        }
    }

    /**
     * Returns the mapWrapper
     * @return
     */
    public MapWrapper getMapWrapper() {
        return mapWrapper;
    }

    /**
     * Returns the model entities
     * @return myModelEntities
     */
    public Map<String, Entity> getModelEntities() {
        return myModelEntities;
    }

    /**
     * Returns the view entities
     * @return myViewEntities
     */
    public Map<String, EntityView> getViewEntities() {
        return myViewEntities;
    }

    /**
     * Returns the model obstacles
     * @return myModelObstacles
     */
    public Map<List<Double>, Obstacle> getModelObstacles() {
        return myModelObstacles;
    }

    /**
     * Returns the model attacks
     * @return myModelAttacks
     */
    public Map<Integer, Attack> getModelAttacks() {
        return myModelAttacks;
    }

    /**
     * Returns the view attacks
     * @return myViewAttacks
     */
    public Map<Integer, AttackView> getViewAttacks() {
        return myViewAttacks;
    }

    /**
     * Returns the view powerups
     * @return myViewPowerUps
     */
    public Map<List<Integer>, BlockView> getViewPowerUps() { return myViewPowerUps; }

    public Map<List<Double>, PowerUp> getModelPowerUps() { return myModelPowerUps;}

    /**
     * Returns the corresponding map based on string input
     * @return the corresponding map
     */
    private Map<String, Map<?,?>> getViewModelMaps() {
        return Map.of("modelEntities", myModelEntities, "viewEntities", myViewEntities,
                "modelAttacks", myModelAttacks, "viewAttacks", myViewAttacks,
                "modelObstacles", myModelObstacles, "viewObstacles", myViewObstacles,
                "modelPowerUps", myModelPowerUps, "viewPowerUps", myViewPowerUps);
    }

    /**
     * updates the player's health bar on the HUD
     */
    private void updatePlayerHealth() {
        myView.updateHealth(myModelEntities.get(myMainHeroName).getHp());
    }

    /**
     * Updates the player's score on the HUD
     */
    public void updatePlayerScore() {
        myView.updateScore(this.score);
    }

    public void showMessage(Alert.AlertType type, String message, Exception e) {
        new Alert(type, message).showAndWait();
    }
    public void playPause(){
        if(animationPlaying){
            pauseAnimation();
        } else playAnimation();
        myView.updatePlayPause(animationPlaying);
    }

    /**
     * getter for MovementHandler - sole function is to allow the MovementHandlerTest's
     * Controller and MovementHandler to be appropriately linked for accurate testing
     * @return MovementHandler
     */
    public MovementHandler getMovementHandler() {
        return movementHandler;
    }

    public void addLife(){
        myModel.addLife();
    }
    public void doubleScore(){
        score*=2;
        myView.updateScore(score);
    }
    public void quitToTitle(){
        myStage.close();
        Stage newStage = new Stage();
        StartScreen s = new StartScreen(newStage);
        newStage.setScene(s.makeScene());
        newStage.show();
        Stage toClose = (Stage)(myView.getScene().getWindow());
        toClose.close();
    }
}
