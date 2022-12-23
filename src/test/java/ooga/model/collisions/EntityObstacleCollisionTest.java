package ooga.model.collisions;

import javafx.stage.Stage;
import ooga.controller.Controller;
import ooga.controller.EntityParser;
import ooga.model.hero.MainHero;
import ooga.model.obstacle.DestroyableWall;
import ooga.view.EntityView;
import ooga.view.screens.StartScreen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.DukeApplicationTest;

import java.util.Map;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

class EntityObstacleCollisionTest extends DukeApplicationTest {

    private EntityParser heroParser;
    private MainHero hero;
    private EntityView heroView;
    private Map<String, Map<?,?>> viewEntities;
    private DestroyableWall obstacle;
    private EntityObstacleCollision myCollision;

    @Override
    public void start(Stage stage) {
        StartScreen ss = new StartScreen(stage);
        stage.setScene(ss.makeScene());
        ResourceBundle labels = ResourceBundle.getBundle("ResourceBundles.LabelsBundle");
        Controller controller = new Controller(stage, "MainMap", "The Beginning", labels);
    }

    @BeforeEach
    void setUp() {
        heroParser = new EntityParser("TestHero", new String[]{"MainHero", "1", "1"});
        hero = new MainHero(heroParser.getAttributeMap());
        heroView = new EntityView("sprites/hero/SOUTH.gif", "TestHeroView");
        viewEntities = Map.of("viewEntities", Map.of("TestHero", heroView));
        obstacle = new DestroyableWall(5.0,5.0);
        myCollision = new EntityObstacleCollision(viewEntities);
    }

    @Test
    void collideTest() {
        myCollision.collide(hero, obstacle);
        assertInstanceOf(EntityObstacleCollision.class, myCollision);
    }


}