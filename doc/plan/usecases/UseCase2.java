// USE CASE: Create HUD

public HUDView extends HBox{
    public HUDView{

        }
}


public HUDController implements CreateHUD{
    private Model model;
    private HUDView view;
    public HUDController(HUDView hView,Model m){
        model = m;
        view = hView;
    }

    public HUDView packageHUD(){


        }




        HUDView packageHUD();

        //updates the time elapsed in the game
        updateGameTimeView();

        //updates the current score of the game
        updateScoreView();

        //updates the powerups currently applied to the character
        updatePowerupsView(Image img);

        //updates the health view of the player
        updateHealthView();

        //creates the time for the HUD
        InfoPair createTimeView();

        //creates the score for the HUD
        InfoPair createScoreView();

        //creates the powerup icons for the HUD
        ImageGroup createPowerupsView();

        //creates the health (hearts) for the HUD
        HealthView createHealthView();
        }