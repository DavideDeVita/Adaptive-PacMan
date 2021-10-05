package pacman;

import java.awt.Color;
import javafx.util.Pair;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import static pacman.Direction.Left;
import static pacman.GameState.Win;
import pacman.StateObservation_Player.PlayerState;
import pacman.StateObservation_Strategies.StrategyState;

/**
 *
 * @author Falie
 */
public final class Game_PacMan implements Runnable{
    Thread renderThread;
    Board board;
    final GameLogic logic;
    boolean running;
    public final static int BEHAVIOURS=67, FAMILIES=14;
    private final static int desiredFPS=20, fullRenderEvery=75;
    private final static float onStart_Wait=1.75f, onLose_Wait=1.25f;
    private static boolean showOnView=true;
    private final static boolean periodicallyFullRender=false,
            logFPS=true, scenicWaiting=true;
    //Simulation vars
    private final static int testsToRun_PlayerState=100000, testsToRun_perBehav=10000, MIN_ENCOUNTER_PER_STATE = 500;
    //private final static int testsToRun_PlayerState=100000, testsToRun_perBehav=10000, MIN_ENCOUNTER_PER_STATE = 500;
    private final static float GAME_MAX_DURATION=300f, GAME_FORCE_ROTATION_DURATION=180f, ACTIVATE_VIEW=400f;
    private static int winCounter=0, loseCounter=0;
    private final static float LOG_PERCENT_RATIO=0.25f, OBSERVATION_PERIOD=15F;
    private final static Test test=Test.Game3;
    public static AdaptationType adaptation=null;
    
    enum Test {Playable, nonAdaptive, Adaptive_Tabular, Adaptive_statelessBehavioursScore,
                UserScore, BehavioursScore, BehavioursScore_noState, Both,
                    Placid, iWannaCry, Unfair,
                        Game1, Game2, Game3,
                        Game5, Game4, Game6,
                        Game7, Game9, Game8}
    enum AdaptationType {T, SBS}
    
    private View view;
    private JFrame jFrame;

    public Game_PacMan(Board board) {
        this.board = board;
        Setup cs_b = Setup.std();
        
        this.logic = new GameLogic(board, this, cs_b);
        if(periodicallyFullRender || showOnView)
            setView(new View(this, 3));
        this.logic.createAgents();
    }

    void setView(View view) {
        this.view=view;
        jFrame = new JFrame("Simple Pac-man");
        jFrame.setBackground(Color.BLUE);
        jFrame.setSize( board.cols * View.TILE_SIZE, (board.rows+1) * View.TILE_SIZE );
        jFrame.setLocationRelativeTo(null);
        jFrame.setResizable(false);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.add(view);
        jFrame.setVisible(true);
    }
    
    private void gameLoop() {
        resume();
    }
    
    /** Starts the game loop in a separate thread.
     */
    public void resume() {
        running = true;
        renderThread = new Thread(this);
        renderThread.start();         
    }

    /** Stops the game loop and waits for it to finish
     */
    public void pause() {
        running = false;
        while(true) {
            try {
                renderThread.join();
                break;
            } catch (InterruptedException e) {
                // just retry
            }
        }
    }

    @Override
    public void run() {
        switch (test){
            case Playable:
                playableRun();
                break;
            case nonAdaptive:
            case Game1:
            case Game5:
            case Game7:
                nonAdaptiveRun();
                break;
            case Adaptive_Tabular:
            case Game2:
            case Game4:
            case Game9:
                adaptation = AdaptationType.T;
                playableAdaptiveRun_tabular();
                break;
            case Adaptive_statelessBehavioursScore:
            case Game3:
            case Game6:
            case Game8:
                adaptation = AdaptationType.SBS;
                playableAdaptiveRun_SBS();
                break;
            case UserScore:
                simulationRun_UserScore();
                break;
            case BehavioursScore:
                simulationRun_BehavioursScores();
                break;
            case BehavioursScore_noState:
                simulationRun_BehavioursScores_noState();
                break;
            case Both:
                simulationRun_UserScore();
                winCounter=0;
                loseCounter =0;
                simulationRun_BehavioursScores();
                break;
            case Placid:
            case iWannaCry:
            case Unfair:
                customGame(test);
                break;
        }
    }
    //PlayerState
    public void simulationRun_UserScore() {
        long testBatteryStartTime=System.nanoTime();
        float deltaTime;
        int log_period=(int)(testsToRun_PlayerState*LOG_PERCENT_RATIO);
        if(log_period==0)log_period++;
        
        PacMan_Bot pacBots[] = getPacBotsArray();
        int pacBots_W[] = new int[pacBots.length], pacBots_L[] = new int[pacBots.length];
        /*** The Game Main Loop ***/
        UserScore_Learning.TD_Learning_init();
        logic.setPlayerObservation(OBSERVATION_PERIOD);
        
        int rg=Utils.random(0, pacBots.length-1);
        for(int gamesCounter=0; gamesCounter<testsToRun_PlayerState; gamesCounter++){
            
            PacMan_Bot pacBot = pacBots[ (rg + gamesCounter)%pacBots.length];
            //_Log.a("PacBot", pacBot+"");
            logic.reset();
            logic.setPacMan( pacBot );
            //_Log.a("Playing", ""+pacBots[ (rg + gamesCounter)%pacBots.length] );
            //Def
            int renderCounter=0;
            long startTime = System.nanoTime(),
                    currentTime;
            //float thisGameDuration = 0f;
            float onStart_yetToWait=onStart_Wait, onLose_yetToWait=0f;
            GameState gameState=GameState.Play;

            //Init
            if (periodicallyFullRender || showOnView){
                view.fullRender();
                view.fullRender();
                jFrame.setTitle("Simple Pac-man:        "+logic.pac_lives+" lives left");
            }
            
            UserScore_Learning.TD_Learning_newStart();
            boolean alreadyForced=false;
            
            while (running && !gameState.endGame()) {
                currentTime = System.nanoTime();
                // deltaTime is in seconds
                deltaTime = (currentTime-startTime) / 1_000_000_000f;//seconds
                startTime = currentTime;
                
                //thisGameDuration+=deltaTime;
                if( !alreadyForced && logic.fullGameTimer>GAME_FORCE_ROTATION_DURATION ){
                    if(_Log.LOG_ACTIVE) _Log.a("Forcing 180", pacBot+" is lasting too long: "
                            + "simulated "+logic.fullGameTimer+" s");
                    logic.force_180turn();
                    alreadyForced=true;
                }
                if( logic.fullGameTimer>GAME_MAX_DURATION ){
                    _Log.a("Repeating", pacBot+" is lasting too long: "
                            + "simulated "+logic.fullGameTimer+" s");
                    gamesCounter--;
                    break;
                }
                if(!showOnView && !periodicallyFullRender && logic.fullGameTimer>ACTIVATE_VIEW){
                    setView(new View(this, 3));
                    view.fullRender();
                    view.fullRender();
                    showOnView=true;
                }
                
                //If life is lost.. freeze the death screen for the first seconds
                if(scenicWaiting){
                    if ( onLose_yetToWait>0 ){
                        onLose_yetToWait-=deltaTime;
                        if(onLose_yetToWait <= 0){
                            logic.restart();
                            if (periodicallyFullRender){
                                view.fullRender();
                                //view.fullRender();
                            }
                            onStart_yetToWait = onStart_Wait;
                        }
                        else{
                            continue;
                        }
                    }
                    //If (re)starting.. freeze the screen for the first seconds
                    if ( onStart_yetToWait>0 ){
                        onStart_yetToWait-=deltaTime;
                        //continue;//Perde un frame ma è più rapido e pulito
                        if(onStart_yetToWait>0)
                            continue;
                        else{
                            onStart_yetToWait=0;
                            if(periodicallyFullRender || showOnView)
                                jFrame.setTitle("Simple Pac-man:        "+logic.pac_lives+" lives left");
                        }
                    }
                }

                gameState = logic.update( 1f/desiredFPS );
                switch (gameState){
                    case Play:
                        PlayerState state = logic.observeState_Player();
                        if(state!=null)
                            UserScore_Learning.TD_Learning(state);
                        break;
                    case Win:
                        if (periodicallyFullRender || showOnView){
                            view.fullRender();
                            jFrame.setTitle("Simple Pac-man:        Game Won");
                        }
                        if(_Log.LOG_ACTIVE){
                            _Log.i("Game", "Simple Pac-man:        Game Won\n");}

                        UserScore_Learning.TD_Learning_Win();
                        winCounter++;
                        pacBots_W[(rg + gamesCounter) %pacBots.length]++;
                        break;
                    case GameOver:
                        if (periodicallyFullRender || showOnView){
                            view.fullRender();
                            jFrame.setTitle("Simple Pac-man:        Game Over");
                        }
                        if(_Log.LOG_ACTIVE){
                            _Log.i("Game", "Simple Pac-man:        Game Over\n");}

                        UserScore_Learning.TD_Learning_Lose();
                        loseCounter++;
                        pacBots_L[(rg + gamesCounter) %pacBots.length]++;
                        break;
                    case LifeLost:
                        if(periodicallyFullRender || showOnView){
                            view.fullRender();
                            jFrame.setTitle("Simple Pac-man:        Life Lost !");
                        }
                        if(_Log.LOG_ACTIVE) _Log.e("PacMan Game", "Life Lost !");
                        gameState = GameState.Play;

                        if(scenicWaiting)
                            onLose_yetToWait = onLose_Wait;
                        else{ //Otherwise won't restart
                            logic.restart();
                            if (periodicallyFullRender || showOnView){
                                jFrame.setTitle("Simple Pac-man:        "+logic.pac_lives+" lives left");
                                view.fullRender();
                                //view.fullRender();
                            }
                        }
                        continue;                
                }
                if( periodicallyFullRender && ++renderCounter%fullRenderEvery==0 ){
                    renderCounter=0;
                    view.fullRender();
                    jFrame.setTitle("Simple Pac-man:        "+logic.pac_lives+" lives left");
                }
                else if(showOnView)
                    view.render();
            }
            //End of a Game
            //deltaTime = (System.nanoTime() - trueStartTime)/1_000_000_000F;
            if(gamesCounter%log_period==0 )
                _Log.a("Test Percent", f(1F*gamesCounter/testsToRun_PlayerState)+"\n"
                    + "\tWon "+winCounter+" times\n"
                    + "\tLost "+loseCounter+" times\n");
        }
        //End of All Tests
        deltaTime = (System.nanoTime() - testBatteryStartTime)/1_000_000_000F;
        _Log.a("End of Test", "Standard Test Time: "+deltaTime+" s\n"
                + "\tWon "+winCounter+" times\n"
                + "\tLost "+loseCounter+" times\n");
        
        logPacBotsStats(pacBots, pacBots_W, pacBots_L);
        //_Log.a("TD Learning", UserScore_Learning.TD_Learning_read() );
        
        simulateRare_PlayerStates(pacBots, pacBots_W, pacBots_L);
        
        //End
        deltaTime = (System.nanoTime() - testBatteryStartTime)/1_000_000_000F;
        _Log.a("End of Test\n\n\n");
        _Log.a("End of Test", "End of all Tests Time: "+deltaTime+" s\n");
        logPacBotsStats(pacBots, pacBots_W, pacBots_L);
        _Log.a("TD Learning", UserScore_Learning.TD_Learning_read() );
    }
    private void simulateRare_PlayerStates(PacMan_Bot pacBots[], int pacBots_W[], int pacBots_L[]){
        float deltaTime;
        int games=0, loopCounetr=0, 
                total = UserScore_Learning.P*UserScore_Learning.D*UserScore_Learning.E*UserScore_Learning.L,
                log_period=(int)(total*LOG_PERCENT_RATIO);
        
        int rg=Utils.random(0, pacBots.length-1);
        
        if(log_period==0)log_period++;
        
        UserScore_Learning.TD_Learning_setAlphaGamma(50f, 0.95f);
        
        /*for(int p=0; p<UserScore_Learning.P; p++){    
        for(int d=0; d<UserScore_Learning.D; d++){
        for(int e=0; e<UserScore_Learning.E; e++){
        for(int l=UserScore_Learning.L; l>=0; l--){*/
        for(int p=UserScore_Learning.P-1; p>=0; p--){ //Late Phases first.. will avoid useless corrections on early phases
        for(int d=UserScore_Learning.D-1; d>=0; d--){
        for(int e=UserScore_Learning.E-1; e>=0; e--){
        for(int l=0; l<UserScore_Learning.L; l++){
            if(loopCounetr++%log_period==0 )
                _Log.a("End of the game", f(loopCounetr*1f/total)+"\n"
                    + "\tWon "+winCounter+" times\n"
                    + "\tLost "+loseCounter+" times\n");
            while(UserScore_Learning.player_N[p][d][e][l] < MIN_ENCOUNTER_PER_STATE){
                if(_Log.LOG_ACTIVE) _Log.i("Simulating state", "State ("+p+", "+d+", "+e+", "+l+"): #"+UserScore_Learning.player_N[p][d][e][l]);
                UserScore_Learning.TD_Learning_newStart();
                UserScore_Learning.TD_Learning(p, d, e, l);
                
                PacMan_Bot pacBot = pacBots[ (rg + games)%pacBots.length];
                games++;
                logic.setPacMan( pacBot );
                logic.reset(p, d, e, l);
                
                //_Log.a("Playing", ""+pacBot );
                //Def
                int renderCounter=0;
                long startTime = System.nanoTime(),
                        currentTime;
                //float thisGameDuration = 0f;
                
                float onStart_yetToWait=onStart_Wait, onLose_yetToWait=0f;
                GameState gameState=GameState.Play;

                //Init
                if (periodicallyFullRender || showOnView){
                    view.fullRender();
                    view.fullRender();
                    jFrame.setTitle("Simple Pac-man:        "+logic.pac_lives+" lives left");
                }
                
                boolean alreadyForced=false;

                while (running && !gameState.endGame()) {
                    currentTime = System.nanoTime();
                    // deltaTime is in seconds
                    deltaTime = (currentTime-startTime) / 1_000_000_000f;//seconds

                    startTime = currentTime;
                
                    //thisGameDuration+=deltaTime;
                    if( !alreadyForced && logic.fullGameTimer>GAME_FORCE_ROTATION_DURATION + (p*OBSERVATION_PERIOD) ){
                        if(_Log.LOG_ACTIVE) _Log.w("Forcing 180", pacBot+" - "+"("+p+", "+d+", "+e+", "+l+")"+" is lasting too long: "
                                + "simulated "+logic.fullGameTimer+" s");
                        logic.force_180turn();
                        alreadyForced=true;
                    }
                    if( logic.fullGameTimer>GAME_MAX_DURATION + (p*OBSERVATION_PERIOD) ){
                        _Log.a("Repeating", pacBot+" - "+"("+p+", "+d+", "+e+", "+l+")"+" is lasting too long: "
                                + "simulated "+logic.fullGameTimer+" s");
                        games--;
                        break;
                    }
                    if(!showOnView && !periodicallyFullRender && logic.fullGameTimer>ACTIVATE_VIEW+(p*OBSERVATION_PERIOD) ){
                        setView(new View(this, 3));
                        view.fullRender();
                        view.fullRender();
                        showOnView=true;
                    }

                    //If life is lost.. freeze the death screen for the first seconds
                    if(scenicWaiting){
                        if ( onLose_yetToWait>0 ){
                            onLose_yetToWait-=deltaTime;
                            if(onLose_yetToWait <= 0){
                                logic.restart();
                                if (periodicallyFullRender){
                                    view.fullRender();
                                    //view.fullRender();
                                }
                                onStart_yetToWait = onStart_Wait;
                            }
                            else{
                                continue;
                            }
                        }
                        //If (re)starting.. freeze the screen for the first seconds
                        if ( onStart_yetToWait>0 ){
                            onStart_yetToWait-=deltaTime;
                            //continue;//Perde un frame ma è più rapido e pulito
                            if(onStart_yetToWait>0)
                                continue;
                            else{
                                onStart_yetToWait=0;
                                if(periodicallyFullRender || showOnView)
                                    jFrame.setTitle("Simple Pac-man:        "+logic.pac_lives+" lives left");
                            }
                        }
                    }

                    gameState = logic.update( 1f/desiredFPS );
                    switch (gameState){
                        case Play:
                            PlayerState state = logic.observeState_Player();
                            if(state!=null)
                                UserScore_Learning.TD_Learning(state);
                            break;
                        case Win:
                            if (periodicallyFullRender || showOnView){
                                view.fullRender();
                                jFrame.setTitle("Simple Pac-man:        Game Won");
                            }
                            if(_Log.LOG_ACTIVE){
                                _Log.i("Game", "Simple Pac-man:        Game Won\n");}

                            UserScore_Learning.TD_Learning_Win();
                            winCounter++;
                            pacBots_W[(rg + games)%pacBots.length]++;
                            break;
                        case GameOver:
                            if (periodicallyFullRender || showOnView){
                                view.fullRender();
                                jFrame.setTitle("Simple Pac-man:        Game Over");
                            }
                            if(_Log.LOG_ACTIVE){
                                _Log.i("Game", "Simple Pac-man:        Game Over\n");}

                            UserScore_Learning.TD_Learning_Lose();
                            loseCounter++;
                            pacBots_L[(rg + games)%pacBots.length]++;
                            break;
                        case LifeLost:
                            if(periodicallyFullRender || showOnView){
                                view.fullRender();
                                jFrame.setTitle("Simple Pac-man:        Life Lost !");
                            }
                            if(_Log.LOG_ACTIVE) _Log.e("PacMan Game", "Life Lost !");
                            gameState = GameState.Play;

                            if(scenicWaiting)
                                onLose_yetToWait = onLose_Wait;
                            else{ //Otherwise won't restart
                                logic.restart();
                                if (periodicallyFullRender || showOnView){
                                    jFrame.setTitle("Simple Pac-man:        "+logic.pac_lives+" lives left");
                                    view.fullRender();
                                    //view.fullRender();
                                }
                            }
                            continue;                
                    }
                    if( periodicallyFullRender && ++renderCounter%fullRenderEvery==0 ){
                        renderCounter=0;
                        view.fullRender();
                        jFrame.setTitle("Simple Pac-man:        "+logic.pac_lives+" lives left");
                    }
                    else if(showOnView)
                        view.render();
                }
                //End of a Game
                //if(_Log.LOG_ACTIVE) _Log.i("End of the game", "Time: "+thisGameDuration+" s\tOutcome: "+gameState+"\tlives left: "+logic.pac_lives);
            }
        }
        }
        }
        //}
        }
        
        _Log.a("End of Test", "Patch Test: \n"
                + "\tWon "+winCounter+" times\n"
                + "\tLost "+loseCounter+" times\n");
    }
    //Strategies State
    public void simulationRun_BehavioursScores()  {
        long testBatteryStartTime=System.nanoTime();
        float deltaTime;
        int log_period=(int)(testsToRun_perBehav*LOG_PERCENT_RATIO);
        if(log_period==0) log_period++;
        
        PacMan_Bot pacBots[] = getPacBotsArray();
        //int pacBots_W[] = new int[pacBots.length], pacBots_L[] = new int[pacBots.length];
        
        Setup behaviours[] = getAgentsStrategiesArray();
        //Setup behaviours[] = getSingleStrategyArray();
        int behaviours_W[] = new int[behaviours.length], behaviours_L[] = new int[behaviours.length];
        /*** The Game Main Loop ***/
        logic.setAgentObservation(OBSERVATION_PERIOD);
        BehavioursScore_Learning.TD_Learning_init(behaviours.length);
        
        int rg=Utils.random(0, pacBots.length-1);
        for (int beh=0; beh<behaviours.length; beh++){
            logic.setSetup( behaviours[beh] );
            for(int gamesCounter=0; gamesCounter<testsToRun_perBehav; gamesCounter++){
                PacMan_Bot pacBot = pacBots[ (rg + gamesCounter)%pacBots.length];
                logic.reset();
                logic.setPacMan( pacBot );
                //_Log.a("Playing", ""+pacBot );
                //Def
                int renderCounter=0;
                long startTime = System.nanoTime(),
                        currentTime;
                //float thisGameDuration = 0f;
                float onStart_yetToWait=onStart_Wait, onLose_yetToWait=0f;
                GameState gameState=GameState.Play;

                //Init
                if (periodicallyFullRender || showOnView){
                    view.fullRender();
                    view.fullRender();
                    jFrame.setTitle("Simple Pac-man:        "+logic.pac_lives+" lives left");
                }

                BehavioursScore_Learning.TD_Learning_newStart();
                boolean alreadyForced = false;
                
                while (running && !gameState.endGame() ) {
                    currentTime = System.nanoTime();
                    // deltaTime is in seconds
                    deltaTime = (currentTime-startTime) / 1_000_000_000f;//seconds
                    startTime = currentTime;

                    //thisGameDuration+=deltaTime;
                    if( !alreadyForced && logic.fullGameTimer>GAME_FORCE_ROTATION_DURATION ){
                        if(_Log.LOG_ACTIVE) _Log.w("Forcing 180", pacBot+" is lasting too long: "
                                + "simulated "+logic.fullGameTimer+" s");
                        logic.force_180turn();
                        alreadyForced=true;
                    }
                    if( logic.fullGameTimer>GAME_MAX_DURATION ){
                        _Log.a("Repeating", pacBot+" is lasting too long: "
                                + "simulated "+logic.fullGameTimer+" s");
                        gamesCounter--;
                        break;
                    }
                    if(!showOnView && !periodicallyFullRender && logic.fullGameTimer>ACTIVATE_VIEW){
                        setView(new View(this, 3));
                        view.fullRender();
                        view.fullRender();
                        showOnView=true;
                    }

                    //If life is lost.. freeze the death screen for the first seconds
                    if(scenicWaiting){
                        if ( onLose_yetToWait>0 ){
                            onLose_yetToWait-=deltaTime;
                            if(onLose_yetToWait <= 0){
                                logic.restart();
                                if (periodicallyFullRender){
                                    view.fullRender();
                                }
                                onStart_yetToWait = onStart_Wait;
                            }
                            else{
                                continue;
                            }
                        }
                        //If (re)starting.. freeze the screen for the first seconds
                        if ( onStart_yetToWait>0 ){
                            onStart_yetToWait-=deltaTime;
                            if(onStart_yetToWait>0)
                                continue;
                            else{
                                onStart_yetToWait=0;
                                if(periodicallyFullRender || showOnView)
                                    jFrame.setTitle("Simple Pac-man:        "+logic.pac_lives+" lives left");
                            }
                        }
                    }

                    gameState = logic.update( 1f/desiredFPS );
                    switch (gameState){
                        case Play:
                            StrategyState state = logic.observeState_Strategies();
                            if(state!=null){
                                BehavioursScore_Learning.TD_Learning(state, beh);
                                //_Log.a("Dot Dispersion", logic.fullGameTimer+"s: "+board.computeDotDispersion() );
                            }
                            break;
                        case Win:
                            if (periodicallyFullRender || showOnView){
                                view.fullRender();
                                jFrame.setTitle("Simple Pac-man:        Game Won");
                            }
                            if(_Log.LOG_ACTIVE){
                                _Log.i("Game", "Simple Pac-man:        Game Won\n");}
                            //Ghosts Lost
                            BehavioursScore_Learning.TD_Learning_Lose(beh);
                            loseCounter++;
                            //pacBots_W[(rg + gamesCounter) %pacBots.length]++;
                            behaviours_L[ beh ]++;
                            break;
                        case GameOver:
                            if (periodicallyFullRender || showOnView){
                                view.fullRender();
                                jFrame.setTitle("Simple Pac-man:        Game Over");
                            }
                            if(_Log.LOG_ACTIVE){
                                _Log.i("Game", "Simple Pac-man:        Game Over\n");}
                            //Ghosts won
                            BehavioursScore_Learning.TD_Learning_Win(beh);
                            winCounter++;
                            //pacBots_L[(rg + gamesCounter) %pacBots.length]++;
                            behaviours_W[ beh ]++;
                            break;
                        case LifeLost:
                            if(periodicallyFullRender || showOnView){
                                view.fullRender();
                                jFrame.setTitle("Simple Pac-man:        Life Lost !");
                            }
                            if(_Log.LOG_ACTIVE) _Log.e("PacMan Game", "Life Lost !");
                            gameState = GameState.Play;

                            if(scenicWaiting)
                                onLose_yetToWait = onLose_Wait;
                            else{ //Otherwise won't restart
                                logic.restart();
                                if (periodicallyFullRender || showOnView){
                                    jFrame.setTitle("Simple Pac-man:        "+logic.pac_lives+" lives left");
                                    view.fullRender();
                                    //view.fullRender();
                                }
                            }
                            continue;                
                    }
                    if( periodicallyFullRender && ++renderCounter%fullRenderEvery==0 ){
                        renderCounter=0;
                        view.fullRender();
                        jFrame.setTitle("Simple Pac-man:        "+logic.pac_lives+" lives left");
                    }
                    else if(showOnView)
                        view.render();
                }
                //End of a Game
                //deltaTime = (System.nanoTime() - trueStartTime)/1_000_000_000F;
                if(gamesCounter%log_period==0 )
                    _Log.a("Test Percentage "+f((gamesCounter+(beh*testsToRun_perBehav*1f))/(testsToRun_perBehav*behaviours.length*1f)), "End of "+behaviours[beh]+" "+pacBot+"\n"
                            +behaviours[beh]+" "+f(1F*gamesCounter/testsToRun_perBehav)+"\n"
                        + "\tWon "+winCounter+" times\n"
                        + "\tLost "+loseCounter+" times\n");
            }
        }
        //End of All Tests
        deltaTime = (System.nanoTime() - testBatteryStartTime)/1_000_000_000F;
        _Log.a("End of Test", "Standard Test Time: "+deltaTime+" s\n"
                + "\tWon "+winCounter+" times\n"
                + "\tLost "+loseCounter+" times\n");
        
        //logAgentsStrategiesStats(behaviours, behaviours_W, behaviours_L);
        //_Log.a("TD Learning", BehavioursScore_Learning.TD_Learning_read(behaviours.length) );
        
        simulateRare_BehavioursScores(pacBots, behaviours, behaviours_W, behaviours_L);
        
        //End
        deltaTime = (System.nanoTime() - testBatteryStartTime)/1_000_000_000F;
        _Log.a("End of Test\n\n\n");
        _Log.a("End of Test", "End of all Tests Time: "+deltaTime+" s\n");
        logAgentsStrategiesStats(behaviours, behaviours_W, behaviours_L);
        _Log.a("TD Learning", BehavioursScore_Learning.TD_Learning_read(behaviours.length) );
    }
    private void simulateRare_BehavioursScores(PacMan_Bot pacBots[], Setup behaviours[], int behaviours_W[], int behaviours_L[]){
        float deltaTime;
        int games=0, loopCounetr=0, 
                total = behaviours.length * BehavioursScore_Learning.P * BehavioursScore_Learning.C * BehavioursScore_Learning.L,
                log_period=(int)(total*LOG_PERCENT_RATIO);
        
        int rg=Utils.random(0, pacBots.length-1);
        BehavioursScore_Learning.TD_Learning_setAlphaGamma(50f, 1f);
        
        for(int beh=0; beh<behaviours.length; beh++){
            logic.setSetup(behaviours[beh]);
        for(int p=UserScore_Learning.P-1; p>=0; p--){ //Late Phases first.. will avoid useless corrections on early phases
        for(int c=UserScore_Learning.D-1; c>=0; c--){
        for(int l=0; l<UserScore_Learning.L; l++){   
            if(loopCounetr++%log_period==0 )
                _Log.a("End of the game", f(loopCounetr*1f/total)+"\n"
                    + "\tWon "+winCounter+" times\n"
                    + "\tLost "+loseCounter+" times\n");
            while(BehavioursScore_Learning.strategy_N[beh][p][c][l] < MIN_ENCOUNTER_PER_STATE){
                if(_Log.LOG_ACTIVE) _Log.i("Simulating state", "State ("+beh+", "+p+", "+c+", "+l+"): #"+BehavioursScore_Learning.strategy_N[beh][p][c][l]);
                BehavioursScore_Learning.TD_Learning_newStart();
                BehavioursScore_Learning.TD_Learning(beh, p, c, l);
                
                PacMan_Bot pacBot = pacBots[ (rg + games)%pacBots.length];
                games++;
                logic.reset(p, c, l);
                logic.setPacMan( pacBot );
                
                //_Log.a("Playing", ""+pacBot );
                //Def
                int renderCounter=0;
                long startTime = System.nanoTime(),
                        currentTime;
                //float thisGameDuration = 0f;
                
                float onStart_yetToWait=onStart_Wait, onLose_yetToWait=0f;
                GameState gameState=GameState.Play;

                //Init
                if (periodicallyFullRender || showOnView){
                    view.fullRender();
                    view.fullRender();
                    jFrame.setTitle("Simple Pac-man:        "+logic.pac_lives+" lives left");
                }
                boolean alreadyForced = false;
                
                while (running && !gameState.endGame()) {
                    currentTime = System.nanoTime();
                    // deltaTime is in seconds
                    deltaTime = (currentTime-startTime) / 1_000_000_000f;//seconds

                    startTime = currentTime;
                
                    //thisGameDuration+=deltaTime;
                    if( !alreadyForced && logic.fullGameTimer>GAME_FORCE_ROTATION_DURATION + (p*OBSERVATION_PERIOD) ){
                        if(_Log.LOG_ACTIVE) _Log.w("Forcing 180", pacBot+" - "+"("+beh+", "+p+", "+c+", "+l+")"+" is lasting too long: "
                                + "simulated "+logic.fullGameTimer+" s");
                        logic.force_180turn();
                        alreadyForced=true;
                    }
                    if( logic.fullGameTimer>GAME_MAX_DURATION + (p*OBSERVATION_PERIOD) ){
                        _Log.a("Repeating", pacBot+" - "+"("+beh+", "+p+", "+c+", "+l+")"+" is lasting too long: "
                                + "simulated "+logic.fullGameTimer+" s");
                        games--;
                        break;
                    }
                    if(!showOnView && !periodicallyFullRender && logic.fullGameTimer>ACTIVATE_VIEW+(p*OBSERVATION_PERIOD) ){
                        setView(new View(this, 3));
                        view.fullRender();
                        view.fullRender();
                        showOnView=true;
                    }

                    //If life is lost.. freeze the death screen for the first seconds
                    if(scenicWaiting){
                        if ( onLose_yetToWait>0 ){
                            onLose_yetToWait-=deltaTime;
                            if(onLose_yetToWait <= 0){
                                logic.restart();
                                if (periodicallyFullRender){
                                    view.fullRender();
                                    //view.fullRender();
                                }
                                onStart_yetToWait = onStart_Wait;
                            }
                            else{
                                continue;
                            }
                        }
                        //If (re)starting.. freeze the screen for the first seconds
                        if ( onStart_yetToWait>0 ){
                            onStart_yetToWait-=deltaTime;
                            //continue;//Perde un frame ma è più rapido e pulito
                            if(onStart_yetToWait>0)
                                continue;
                            else{
                                onStart_yetToWait=0;
                                if(periodicallyFullRender || showOnView)
                                    jFrame.setTitle("Simple Pac-man:        "+logic.pac_lives+" lives left");
                            }
                        }
                    }

                    gameState = logic.update( 1f/desiredFPS );
                    switch (gameState){
                        case Play:
                            StrategyState state = logic.observeState_Strategies();
                            if(state!=null){
                                BehavioursScore_Learning.TD_Learning(state, beh);
                                //_Log.a("Dot Dispersion", logic.fullGameTimer+"s: "+board.computeDotDispersion() );
                            }
                            break;
                        case Win:
                            if (periodicallyFullRender || showOnView){
                                view.fullRender();
                                jFrame.setTitle("Simple Pac-man:        Game Won");
                            }
                            if(_Log.LOG_ACTIVE){
                                _Log.i("Game", "Simple Pac-man:        Game Won\n");}
                            //Ghosts lost
                            BehavioursScore_Learning.TD_Learning_Lose(beh);
                            loseCounter++;
                            behaviours_L[beh]++;
                            break;
                        case GameOver:
                            if (periodicallyFullRender || showOnView){
                                view.fullRender();
                                jFrame.setTitle("Simple Pac-man:        Game Over");
                            }
                            if(_Log.LOG_ACTIVE){
                                _Log.i("Game", "Simple Pac-man:        Game Over\n");}
                            //Ghosts won
                            BehavioursScore_Learning.TD_Learning_Win(beh);
                            winCounter++;
                            behaviours_W[beh]++;
                            break;
                        case LifeLost:
                            if(periodicallyFullRender || showOnView){
                                view.fullRender();
                                jFrame.setTitle("Simple Pac-man:        Life Lost !");
                            }
                            if(_Log.LOG_ACTIVE) _Log.e("PacMan Game", "Life Lost !");
                            gameState = GameState.Play;

                            if(scenicWaiting)
                                onLose_yetToWait = onLose_Wait;
                            else{ //Otherwise won't restart
                                logic.restart();
                                if (periodicallyFullRender || showOnView){
                                    jFrame.setTitle("Simple Pac-man:        "+logic.pac_lives+" lives left");
                                    view.fullRender();
                                    //view.fullRender();
                                }
                            }
                            continue;                
                    }
                    if( periodicallyFullRender && ++renderCounter%fullRenderEvery==0 ){
                        renderCounter=0;
                        view.fullRender();
                        jFrame.setTitle("Simple Pac-man:        "+logic.pac_lives+" lives left");
                    }
                    else if(showOnView)
                        view.render();
                }
                //End of a Game
                //if(_Log.LOG_ACTIVE) _Log.i("End of the game", "Time: "+thisGameDuration+" s\tOutcome: "+gameState+"\tlives left: "+logic.pac_lives);
            }
        }
        }
        }
        }
        
        _Log.a("End of Test", "Patch Test: \n"
                + "\tWon "+winCounter+" times\n"
                + "\tLost "+loseCounter+" times\n");
    }
    
    public void simulationRun_BehavioursScores_noState() {
        long testBatteryStartTime=System.nanoTime();
        float deltaTime;
        int log_period=(int)(testsToRun_perBehav*LOG_PERCENT_RATIO);
        if(log_period==0) log_period++;
        
        PacMan_Bot pacBots[] = getPacBotsArray();
        //int pacBots_W[] = new int[pacBots.length], pacBots_L[] = new int[pacBots.length];
        
        Setup behaviours[] = getAgentsStrategiesArray();
        //Setup behaviours[] = getSingleStrategyArray();
        int behaviours_W[] = new int[behaviours.length], behaviours_L[] = new int[behaviours.length];
        /*** The Game Main Loop ***/
        int rg=Utils.random(0, pacBots.length-1);
        for (int beh=0; beh<behaviours.length; beh++){
            logic.setSetup( behaviours[beh] );
            for(int gamesCounter=0; gamesCounter<testsToRun_perBehav; gamesCounter++){

                PacMan_Bot pacBot = pacBots[ (rg + gamesCounter)%pacBots.length];
                logic.reset();
                logic.setPacMan( pacBot );
                //_Log.a("Playing", ""+pacBot );
                //Def
                int renderCounter=0;
                long startTime = System.nanoTime(),
                        currentTime;
                //float thisGameDuration = 0f;
                float onStart_yetToWait=onStart_Wait, onLose_yetToWait=0f;
                GameState gameState=GameState.Play;

                //Init
                if (periodicallyFullRender || showOnView){
                    view.fullRender();
                    view.fullRender();
                    jFrame.setTitle("Simple Pac-man:        "+logic.pac_lives+" lives left");
                }

                BehavioursScore_Learning.TD_Learning_newStart();
                boolean alreadyForced = false;
                
                while (running && !gameState.endGame()) {
                    currentTime = System.nanoTime();
                    // deltaTime is in seconds
                    deltaTime = (currentTime-startTime) / 1_000_000_000f;//seconds
                    startTime = currentTime;

                    //thisGameDuration+=deltaTime;
                    if( !alreadyForced && logic.fullGameTimer>GAME_FORCE_ROTATION_DURATION ){
                        if(_Log.LOG_ACTIVE) _Log.w("Forcing 180", pacBot+" is lasting too long: "
                                + "simulated "+logic.fullGameTimer+" s");
                        logic.force_180turn();
                        alreadyForced=true;
                    }
                    if( logic.fullGameTimer>GAME_MAX_DURATION ){
                        _Log.a("Repeating", pacBot+" is lasting too long: "
                                + "simulated "+logic.fullGameTimer+" s");
                        gamesCounter--;
                        break;
                    }
                    if(!showOnView && !periodicallyFullRender && logic.fullGameTimer>ACTIVATE_VIEW){
                        setView(new View(this, 3));
                        view.fullRender();
                        view.fullRender();
                        showOnView=true;
                    }

                    //If life is lost.. freeze the death screen for the first seconds
                    if(scenicWaiting){
                        if ( onLose_yetToWait>0 ){
                            onLose_yetToWait-=deltaTime;
                            if(onLose_yetToWait <= 0){
                                logic.restart();
                                if (periodicallyFullRender){
                                    view.fullRender();
                                }
                                onStart_yetToWait = onStart_Wait;
                            }
                            else{
                                continue;
                            }
                        }
                        //If (re)starting.. freeze the screen for the first seconds
                        if ( onStart_yetToWait>0 ){
                            onStart_yetToWait-=deltaTime;
                            if(onStart_yetToWait>0)
                                continue;
                            else{
                                onStart_yetToWait=0;
                                if(periodicallyFullRender || showOnView)
                                    jFrame.setTitle("Simple Pac-man:        "+logic.pac_lives+" lives left");
                            }
                        }
                    }

                    gameState = logic.update( 1f/desiredFPS );
                    switch (gameState){
                        case Play:
                            break;
                        case Win:
                            if (periodicallyFullRender || showOnView){
                                view.fullRender();
                                jFrame.setTitle("Simple Pac-man:        Game Won");
                            }
                            if(_Log.LOG_ACTIVE){
                                _Log.i("Game", "Simple Pac-man:        Game Won\n");}
                            //Ghosts Lost
                            loseCounter++;
                            //pacBots_W[(rg + gamesCounter) %pacBots.length]++;
                            behaviours_L[ beh ]++;
                            break;
                        case GameOver:
                            if (periodicallyFullRender || showOnView){
                                view.fullRender();
                                jFrame.setTitle("Simple Pac-man:        Game Over");
                            }
                            if(_Log.LOG_ACTIVE){
                                _Log.i("Game", "Simple Pac-man:        Game Over\n");}
                            //Ghosts won
                            winCounter++;
                            //pacBots_L[(rg + gamesCounter) %pacBots.length]++;
                            behaviours_W[ beh ]++;
                            break;
                        case LifeLost:
                            if(periodicallyFullRender || showOnView){
                                view.fullRender();
                                jFrame.setTitle("Simple Pac-man:        Life Lost !");
                            }
                            if(_Log.LOG_ACTIVE) _Log.e("PacMan Game", "Life Lost !");
                            gameState = GameState.Play;

                            if(scenicWaiting)
                                onLose_yetToWait = onLose_Wait;
                            else{ //Otherwise won't restart
                                logic.restart();
                                if (periodicallyFullRender || showOnView){
                                    jFrame.setTitle("Simple Pac-man:        "+logic.pac_lives+" lives left");
                                    view.fullRender();
                                    //view.fullRender();
                                }
                            }
                            continue;                
                    }
                    if( periodicallyFullRender && ++renderCounter%fullRenderEvery==0 ){
                        renderCounter=0;
                        view.fullRender();
                        jFrame.setTitle("Simple Pac-man:        "+logic.pac_lives+" lives left");
                    }
                    else if(showOnView)
                        view.render();
                }
                //End of a Game
                //deltaTime = (System.nanoTime() - trueStartTime)/1_000_000_000F;
                if(gamesCounter%log_period==0 )
                    _Log.a("Test Percentage "+f((gamesCounter+(beh*testsToRun_perBehav*1f))/(testsToRun_perBehav*behaviours.length*1f)), "End of "+behaviours[beh]+" "+pacBot+"\n"
                            +behaviours[beh]+" "+f(1F*gamesCounter/testsToRun_perBehav)+"\n"
                        + "\tWon "+winCounter+" times\n"
                        + "\tLost "+loseCounter+" times\n");
            }
        }
        //End of All Tests
        deltaTime = (System.nanoTime() - testBatteryStartTime)/1_000_000_000F;
        _Log.a("End of Test", "End of all Tests Time: "+deltaTime+" s\n");
        logAgentsStrategiesStats(behaviours, behaviours_W, behaviours_L);
    }
    //Playable
    public void playableRun() {
        long startTime = System.nanoTime(),
                fpsTime = startTime,
                currentTime,
                trueStartTime = startTime;
        int frameCounter = 0, secondsCounter=0, renderCounter=0;
        float avgFps=0, 
                onStart_yetToWait=onStart_Wait, onLose_yetToWait=0f;
        GameState gameState=GameState.Play;
        
        float cumulDeltaTime=0f;
        /*** The Game Main Loop ***/
        //gameLogic.reset(4, 1, 2, 2, 1);
        logic.setPacMan(new PacMan_Player("PacMan", logic, 14, 23, Left) );
        //logic.setPacMan(new PacMan_Bot("PacMan", logic, 14, 23, Left, 2.5f, 2f, new PacBot_WanderDirection_PelletBFS(logic), new PacBot_EscapeDirection_BFS_n(logic, 4)) );
        
        Setup specifics = logic.getSetup();
        //specifics.blinky_S = new ScatterTarget_Blinky_RandomArea(14, -2, 28, 7);
        //specifics.blinky_C = new ChaseTarget_Blinky_RoamAround(5);
        //specifics.pinky_S = new ScatterTarget_RandomArea(6, 8, 13, 14);
        //specifics.inky_S = new ScatterTarget_RandomArea(14, 14, 20, 20);
        //specifics.clyde_S = new ScatterTarget_RandomArea(6, 14, 13, 20);
        logic.setSetup(specifics);
        
        if(periodicallyFullRender || showOnView){
            view.fullRender();
            view.fullRender();
            jFrame.setTitle("Simple Pac-man:        "+logic.pac_lives+" lives left");
        } //Dunno why.. whitout it, sometimes does not draw all
        
        float deltaTime, fpsDeltaTime=0f;
        
        while (running && !gameState.endGame() ) {
            currentTime = System.nanoTime();
            // deltaTime is in seconds
            deltaTime = (currentTime-startTime) / 1_000_000_000f;//seconds
                cumulDeltaTime+=deltaTime;
            
            //if(_Log.LOG_ACTIVE) _Log.d("Delta Time", "deltaTime "+deltaTime+" < "+(1F/desiredFPS)+" ? "+(deltaTime < 1f/desiredFPS));
            if(logFPS){
                fpsDeltaTime = (currentTime-fpsTime) / 1_000_000_000f;
                if(deltaTime < 1f/desiredFPS)
                    continue;
            }
            
            startTime = currentTime;

            //If life is lost.. freeze the death screen for the first seconds
            if(scenicWaiting){
                if ( onLose_yetToWait>0 ){
                    onLose_yetToWait-=deltaTime;
                    if(onLose_yetToWait <= 0){
                        logic.restart();
                        if(periodicallyFullRender || showOnView){
                            view.fullRender();
                            view.fullRender();
                        }
                        onStart_yetToWait = onStart_Wait;
                    }
                    else{
                        continue;
                    }
                }
                //If (re)starting.. freeze the screen for the first seconds
                if ( onStart_yetToWait>0 ){
                    onStart_yetToWait-=deltaTime;
                    //continue;//Perde un frame ma è più rapido e pulito
                    if(onStart_yetToWait>0)
                        continue;
                    else{
                        onStart_yetToWait=0;
                        if(periodicallyFullRender || showOnView)
                            jFrame.setTitle("Simple Pac-man:        "+logic.pac_lives+" lives left");
                    }
                }
            }

            gameState = logic.update( deltaTime );
            if(gameState == GameState.LifeLost){
                if(periodicallyFullRender || showOnView)
                    jFrame.setTitle("Simple Pac-man:        Life Lost !");
                if(_Log.LOG_ACTIVE) _Log.i("PacMan Game", "Life Lost !");
                gameState = GameState.Play;

                if(scenicWaiting)
                    onLose_yetToWait = onLose_Wait;
                else{ //Otherwise won't restart
                    logic.restart();
                    if(periodicallyFullRender || showOnView){
                        view.fullRender();
                        view.fullRender();
                        jFrame.setTitle("Simple Pac-man:        "+logic.pac_lives+" lives left");
                    }
                }
                continue;
            }
            else if(gameState.endGame()){
                if(gameState == Win){
                    if(periodicallyFullRender || showOnView)
                        jFrame.setTitle("Simple Pac-man:        Game Won");
                    _Log.a("Game", "Simple Pac-man:        Game Won\t"+logic.fullGameTimer+" s\n");
                }
                else{//(gameState == GameOver)
                    if(periodicallyFullRender || showOnView)
                        jFrame.setTitle("Simple Pac-man:        Game Over");
                    _Log.a("Game", "Simple Pac-man:        Game Over\t"+logic.fullGameTimer+" s\n");
                }
                break;
            }

            if( periodicallyFullRender && ++renderCounter%fullRenderEvery==0 ){
                renderCounter=0;
                view.fullRender();
                jFrame.setTitle("Simple Pac-man:        "+logic.pac_lives+" lives left");
            }
            else if(showOnView)
                view.render();

            if(logFPS){// Measure FPS
                frameCounter++;
                if (fpsDeltaTime > 1) { // every second
                    avgFps=((avgFps*secondsCounter)+frameCounter)/(secondsCounter+1);
                        secondsCounter++;
                        if(_Log.LOG_ACTIVE) _Log.v("FPS", "curr FPS: "+frameCounter);
                    frameCounter = 0;
                    fpsTime = currentTime;
                }
            }
        }
        
        cumulDeltaTime = (System.nanoTime() - trueStartTime)/1_000_000_000F;
        if(_Log.LOG_ACTIVE) _Log.i("End", "Time: "+cumulDeltaTime+" s\tOutcome: "+gameState);
        //if(_Log.LOG_ACTIVE) _Log.v("FPS", "avg fps: "+avgFps);
    }
    public void playableAdaptiveRun_tabular() {
        long startTime = System.nanoTime(),
                fpsTime = startTime,
                currentTime,
                trueStartTime = startTime;
        int frameCounter = 0, secondsCounter=0, renderCounter=0;
        float avgFps=0, 
                onStart_yetToWait=onStart_Wait, onLose_yetToWait=0f;
        GameState gameState=GameState.Play;
        
        float cumulDeltaTime=0f;
        /*** The Game Main Loop ***/
        logic.setPlayerObservation(OBSERVATION_PERIOD);
        logic.setAgentObservation(OBSERVATION_PERIOD);
        DDA_Utils.loadUserEval();
        Pair<Pair<Object, String>[][], Adapter[]> pair = getFamiliesStrategies();
        DDA_Utils.loadFamilies_tabular(pair.getKey(), pair.getValue());
        int setup[] = new int[FAMILIES];
        logic.setPacMan(new PacMan_Player("PacMan", logic, 14, 23, Left) );
        
        if(periodicallyFullRender || showOnView){
            view.fullRender();
            view.fullRender();
            jFrame.setTitle("T Pac-man:        "+logic.pac_lives+" lives left");
        } //Dunno why.. whitout it, sometimes does not draw all
        
        float deltaTime, fpsDeltaTime=0f;
        
        while (running && !gameState.endGame()) {
            currentTime = System.nanoTime();
            // deltaTime is in seconds
            deltaTime = (currentTime-startTime) / 1_000_000_000f;//seconds
                cumulDeltaTime+=deltaTime;
            
            //if(_Log.LOG_ACTIVE) _Log.d("Delta Time", "deltaTime "+deltaTime+" < "+(1F/desiredFPS)+" ? "+(deltaTime < 1f/desiredFPS));
            if(logFPS){
                fpsDeltaTime = (currentTime-fpsTime) / 1_000_000_000f;
                if(deltaTime < 1f/desiredFPS)
                    continue;
            }
            
            startTime = currentTime;

            //If life is lost.. freeze the death screen for the first seconds
            if(scenicWaiting){
                if ( onLose_yetToWait>0 ){
                    onLose_yetToWait-=deltaTime;
                    if(onLose_yetToWait <= 0){
                        logic.restart();
                        if(periodicallyFullRender || showOnView){
                            view.fullRender();
                            view.fullRender();
                        }
                        onStart_yetToWait = onStart_Wait;
                    }
                    else{
                        continue;
                    }
                }
                //If (re)starting.. freeze the screen for the first seconds
                if ( onStart_yetToWait>0 ){
                    onStart_yetToWait-=deltaTime;
                    //continue;//Perde un frame ma è più rapido e pulito
                    if(onStart_yetToWait>0)
                        continue;
                    else{
                        onStart_yetToWait=0;
                        if(periodicallyFullRender || showOnView)
                            jFrame.setTitle("T Pac-man:        "+logic.pac_lives+" lives left");
                    }
                }
            }

            gameState = logic.update( deltaTime );
            switch (gameState){
                case Play:
                    PlayerState playerState = logic.observeState_Player();
                    StrategyState behState = logic.observeState_Strategies();
                    if(playerState!=null && behState!=null){
                        DDA_Utils.adapt(logic, playerState, behState, setup);
                    }
                    break;
                case Win:
                    if (periodicallyFullRender || showOnView){
                        view.fullRender();
                        jFrame.setTitle("T Pac-man:        Game Won");
                    }
                    _Log.a("Game", "Adaptive Pac-man:        Game Won\t"+logic.fullGameTimer+" s\n\t Hardenings: "+DDA_Utils.makeHarder+"\tSimplifications: "+DDA_Utils.makeEasier);
                    break;
                case GameOver:
                    if (periodicallyFullRender || showOnView){
                        view.fullRender();
                        jFrame.setTitle("T Pac-man:        Game Over");
                    }
                    _Log.a("Game", "Adaptive Pac-man:        Game Over\t"+logic.fullGameTimer+" s\n\t Hardenings: "+DDA_Utils.makeHarder+"\tSimplifications: "+DDA_Utils.makeEasier);
                    break;
                case LifeLost:
                    if(periodicallyFullRender || showOnView){
                        view.fullRender();
                        jFrame.setTitle("T Pac-man:        Life Lost !");
                    }
                    if(_Log.LOG_ACTIVE) _Log.e("PacMan Game", "Life Lost !");
                    gameState = GameState.Play;

                    if(scenicWaiting)
                        onLose_yetToWait = onLose_Wait;
                    else{ // Otherwise won't restart
                        logic.restart();
                        if (periodicallyFullRender || showOnView){
                            jFrame.setTitle("T Pac-man:        "+logic.pac_lives+" lives left");
                            view.fullRender();
                            //view.fullRender();
                        }
                    }
                    continue;                
            }

            if( periodicallyFullRender && ++renderCounter%fullRenderEvery==0 ){
                renderCounter=0;
                view.fullRender();
                jFrame.setTitle("T Pac-man:        "+logic.pac_lives+" lives left");
            }
            else if(showOnView)
                view.render();

            if(logFPS){// Measure FPS
                frameCounter++;
                if (fpsDeltaTime > 1) { // every second
                    avgFps=((avgFps*secondsCounter)+frameCounter)/(secondsCounter+1);
                        secondsCounter++;
                        if(_Log.LOG_ACTIVE) _Log.v("FPS", "curr FPS: "+frameCounter);
                    frameCounter = 0;
                    fpsTime = currentTime;
                }
            }
        }
        cumulDeltaTime = (System.nanoTime() - trueStartTime)/1_000_000_000F;
        if(_Log.LOG_ACTIVE) _Log.i("End", "Time: "+cumulDeltaTime+" s\tOutcome: "+gameState);
        //if(_Log.LOG_ACTIVE) _Log.v("FPS", "avg fps: "+avgFps);
    }
    public void playableAdaptiveRun_SBS() {
        long startTime = System.nanoTime(),
                fpsTime = startTime,
                currentTime,
                trueStartTime = startTime;
        int frameCounter = 0, secondsCounter=0, renderCounter=0;
        float avgFps=0, 
                onStart_yetToWait=onStart_Wait, onLose_yetToWait=0f;
        GameState gameState=GameState.Play;
        
        float cumulDeltaTime=0f;
        /*** The Game Main Loop ***/
        logic.setPlayerObservation(OBSERVATION_PERIOD);
        DDA_Utils.loadUserEval();
        Pair<Pair<Object, String>[][], Adapter[]> pair = getFamiliesStrategies();
        DDA_Utils.loadFamilies_singleValue(pair.getKey(), pair.getValue());
        int setup[] = new int[FAMILIES];
        logic.setPacMan(new PacMan_Player("PacMan", logic, 14, 23, Left) );
        
        if(periodicallyFullRender || showOnView){
            view.fullRender();
            view.fullRender();
            jFrame.setTitle("SBS Pac-man:        "+logic.pac_lives+" lives left");
        } //Dunno why.. whitout it, sometimes does not draw all
        
        float deltaTime, fpsDeltaTime=0f;
        
        while (running && !gameState.endGame()) {
            currentTime = System.nanoTime();
            // deltaTime is in seconds
            deltaTime = (currentTime-startTime) / 1_000_000_000f;//seconds
                cumulDeltaTime+=deltaTime;
            
            //if(_Log.LOG_ACTIVE) _Log.d("Delta Time", "deltaTime "+deltaTime+" < "+(1F/desiredFPS)+" ? "+(deltaTime < 1f/desiredFPS));
            if(logFPS){
                fpsDeltaTime = (currentTime-fpsTime) / 1_000_000_000f;
                if(deltaTime < 1f/desiredFPS)
                    continue;
            }
            
            startTime = currentTime;

            //If life is lost.. freeze the death screen for the first seconds
            if(scenicWaiting){
                if ( onLose_yetToWait>0 ){
                    onLose_yetToWait-=deltaTime;
                    if(onLose_yetToWait <= 0){
                        logic.restart();
                        if(periodicallyFullRender || showOnView){
                            view.fullRender();
                            view.fullRender();
                        }
                        onStart_yetToWait = onStart_Wait;
                    }
                    else{
                        continue;
                    }
                }
                //If (re)starting.. freeze the screen for the first seconds
                if ( onStart_yetToWait>0 ){
                    onStart_yetToWait-=deltaTime;
                    //continue;//Perde un frame ma è più rapido e pulito
                    if(onStart_yetToWait>0)
                        continue;
                    else{
                        onStart_yetToWait=0;
                        if(periodicallyFullRender || showOnView)
                            jFrame.setTitle("SBS Pac-man:        "+logic.pac_lives+" lives left");
                    }
                }
            }

            gameState = logic.update( deltaTime );
            switch (gameState){
                case Play:
                    PlayerState playerState = logic.observeState_Player();
                    if(playerState!=null){
                        DDA_Utils.adapt(logic, playerState, null, setup);
                    }
                    break;
                case Win:
                    if (periodicallyFullRender || showOnView){
                        view.fullRender();
                        jFrame.setTitle("SBS Pac-man:        Game Won");
                    }
                    _Log.a("Game", "NoBehState Adaptive Pac-man:        Game Won\t"+logic.fullGameTimer+" s\n\t Hardenings: "+DDA_Utils.makeHarder+"\tSimplifications: "+DDA_Utils.makeEasier);
                    break;
                case GameOver:
                    if (periodicallyFullRender || showOnView){
                        view.fullRender();
                        jFrame.setTitle("SBS Pac-man:        Game Over");
                    }
                    _Log.a("Game", "NoBehState Adaptive Pac-man:        Game Over\t"+logic.fullGameTimer+" s\n\t Hardenings: "+DDA_Utils.makeHarder+"\tSimplifications: "+DDA_Utils.makeEasier);
                    break;
                case LifeLost:
                    if(periodicallyFullRender || showOnView){
                        view.fullRender();
                        jFrame.setTitle("SBS Pac-man:        Life Lost !");
                    }
                    if(_Log.LOG_ACTIVE) _Log.e("PacMan Game", "Life Lost !");
                    gameState = GameState.Play;

                    if(scenicWaiting)
                        onLose_yetToWait = onLose_Wait;
                    else{ // Otherwise won't restart
                        logic.restart();
                        if (periodicallyFullRender || showOnView){
                            jFrame.setTitle("SBS Pac-man:        "+logic.pac_lives+" lives left");
                            view.fullRender();
                            //view.fullRender();
                        }
                    }
                    continue;                
            }

            if( periodicallyFullRender && ++renderCounter%fullRenderEvery==0 ){
                renderCounter=0;
                view.fullRender();
                jFrame.setTitle("SBS Pac-man:        "+logic.pac_lives+" lives left");
            }
            else if(showOnView)
                view.render();

            if(logFPS){// Measure FPS
                frameCounter++;
                if (fpsDeltaTime > 1) { // every second
                    avgFps=((avgFps*secondsCounter)+frameCounter)/(secondsCounter+1);
                        secondsCounter++;
                        if(_Log.LOG_ACTIVE) _Log.v("FPS", "curr FPS: "+frameCounter);
                    frameCounter = 0;
                    fpsTime = currentTime;
                }
            }
        }
        cumulDeltaTime = (System.nanoTime() - trueStartTime)/1_000_000_000F;
        if(_Log.LOG_ACTIVE) _Log.i("End", "Time: "+cumulDeltaTime+" s\tOutcome: "+gameState);
        //if(_Log.LOG_ACTIVE) _Log.v("FPS", "avg fps: "+avgFps);
    }
    public void nonAdaptiveRun() {
        long startTime = System.nanoTime(),
                fpsTime = startTime,
                currentTime,
                trueStartTime = startTime;
        int frameCounter = 0, secondsCounter=0, renderCounter=0;
        float avgFps=0, 
                onStart_yetToWait=onStart_Wait, onLose_yetToWait=0f;
        GameState gameState=GameState.Play;
        
        float cumulDeltaTime=0f;
        /*** The Game Main Loop ***/
        logic.setPlayerObservation(OBSERVATION_PERIOD);
        DDA_Utils.loadUserEval();
        logic.setPacMan(new PacMan_Player("PacMan", logic, 14, 23, Left) );
        
        if(periodicallyFullRender || showOnView){
            view.fullRender();
            view.fullRender();
            jFrame.setTitle("S Pacman:        "+logic.pac_lives+" lives left");
        } //Dunno why.. whitout it, sometimes does not draw all
        
        float deltaTime, fpsDeltaTime=0f;
        
        while (running && !gameState.endGame()) {
            currentTime = System.nanoTime();
            // deltaTime is in seconds
            deltaTime = (currentTime-startTime) / 1_000_000_000f;//seconds
                cumulDeltaTime+=deltaTime;
            
            //if(_Log.LOG_ACTIVE) _Log.d("Delta Time", "deltaTime "+deltaTime+" < "+(1F/desiredFPS)+" ? "+(deltaTime < 1f/desiredFPS));
            if(logFPS){
                fpsDeltaTime = (currentTime-fpsTime) / 1_000_000_000f;
                if(deltaTime < 1f/desiredFPS)
                    continue;
            }
            
            startTime = currentTime;

            //If life is lost.. freeze the death screen for the first seconds
            if(scenicWaiting){
                if ( onLose_yetToWait>0 ){
                    onLose_yetToWait-=deltaTime;
                    if(onLose_yetToWait <= 0){
                        logic.restart();
                        if(periodicallyFullRender || showOnView){
                            view.fullRender();
                            view.fullRender();
                        }
                        onStart_yetToWait = onStart_Wait;
                    }
                    else{
                        continue;
                    }
                }
                //If (re)starting.. freeze the screen for the first seconds
                if ( onStart_yetToWait>0 ){
                    onStart_yetToWait-=deltaTime;
                    //continue;//Perde un frame ma è più rapido e pulito
                    if(onStart_yetToWait>0)
                        continue;
                    else{
                        onStart_yetToWait=0;
                        if(periodicallyFullRender || showOnView)
                            jFrame.setTitle("S Pacman:        "+logic.pac_lives+" lives left");
                    }
                }
            }

            gameState = logic.update( deltaTime );
            switch (gameState){
                case Play:
                    PlayerState playerState = logic.observeState_Player();
                    if(playerState!=null){
                        _Log.a("Player State", "userState: "+playerState+"\t score: "+DDA_Utils.userScore(playerState));
                    }
                    break;
                case Win:
                    if (periodicallyFullRender || showOnView){
                        view.fullRender();
                        jFrame.setTitle("S Pacman:        Game Won");
                    }
                    _Log.a("Game", "Non Adaptive Pac-man:        Game Won\t"+logic.fullGameTimer+" s\n");
                    break;
                case GameOver:
                    if (periodicallyFullRender || showOnView){
                        view.fullRender();
                        jFrame.setTitle("S Pacman:        Game Over");
                    }
                    _Log.a("Game", "Non Adaptive Pac-man:        Game Over\t"+logic.fullGameTimer+" s\n");
                    break;
                case LifeLost:
                    if(periodicallyFullRender || showOnView){
                        view.fullRender();
                        jFrame.setTitle("S Pacman:        Life Lost !");
                    }
                    if(_Log.LOG_ACTIVE) _Log.e("PacMan Game", "Life Lost !");
                    gameState = GameState.Play;

                    if(scenicWaiting)
                        onLose_yetToWait = onLose_Wait;
                    else{ // Otherwise won't restart
                        logic.restart();
                        if (periodicallyFullRender || showOnView){
                            jFrame.setTitle("S Pacman:        "+logic.pac_lives+" lives left");
                            view.fullRender();
                            //view.fullRender();
                        }
                    }
                    continue;                
            }

            if( periodicallyFullRender && ++renderCounter%fullRenderEvery==0 ){
                renderCounter=0;
                view.fullRender();
                jFrame.setTitle("S Pacman:        "+logic.pac_lives+" lives left");
            }
            else if(showOnView)
                view.render();

            if(logFPS){// Measure FPS
                frameCounter++;
                if (fpsDeltaTime > 1) { // every second
                    avgFps=((avgFps*secondsCounter)+frameCounter)/(secondsCounter+1);
                        secondsCounter++;
                        if(_Log.LOG_ACTIVE) _Log.v("FPS", "curr FPS: "+frameCounter);
                    frameCounter = 0;
                    fpsTime = currentTime;
                }
            }
        }
        cumulDeltaTime = (System.nanoTime() - trueStartTime)/1_000_000_000F;
        if(_Log.LOG_ACTIVE) _Log.i("End", "Time: "+cumulDeltaTime+" s\tOutcome: "+gameState);
        //if(_Log.LOG_ACTIVE) _Log.v("FPS", "avg fps: "+avgFps);
    }
    public void customGame(Test test) {
        long startTime = System.nanoTime(),
                fpsTime = startTime,
                currentTime,
                trueStartTime = startTime;
        int frameCounter = 0, secondsCounter=0, renderCounter=0;
        float avgFps=0, 
                onStart_yetToWait=onStart_Wait, onLose_yetToWait=0f;
        GameState gameState=GameState.Play;
        
        float cumulDeltaTime=0f;
        /*** The Game Main Loop ***/
        logic.setPlayerObservation(OBSERVATION_PERIOD);
        logic.setAgentObservation(OBSERVATION_PERIOD);
        
        int setup[] = new int[FAMILIES];
        switch(test){
            case Placid:
                logic.setSetup(Setup.placid());
                setup = new int[]{1, 0, 1, 1, 0, 2, 3, 3, 3, 3, 2, 3, 1, 3};
                break;
            case iWannaCry:
                logic.setSetup(Setup.iWannaCry());
                setup = new int[]{5, 1, 0, 3, 4, 0, 0, 1, 6, 1, 5, 1, 8, 1};
                break;
            case Unfair:
                setup = new int[]{5, 1, 0, 3, 4, 0, 0, 1, 6, 1, 5, 1, 8, 1};
                logic.setSetup(Setup.unfair());
                break;
        }
        
        {   DDA_Utils.loadUserEval();
            Pair<Pair<Object, String>[][], Adapter[]> pair = getFamiliesStrategies();
            DDA_Utils.loadFamilies_singleValue(pair.getKey(), pair.getValue());
            //DDA_Utils.loadFamilies_tabular(pair.getKey(), pair.getValue());
        }
        logic.setPacMan(new PacMan_Player("PacMan", logic, 14, 23, Left) );
        
        if(periodicallyFullRender || showOnView){
            view.fullRender();
            view.fullRender();
            jFrame.setTitle("S Pacman:        "+logic.pac_lives+" lives left");
        } //Dunno why.. whitout it, sometimes does not draw all
        
        float deltaTime, fpsDeltaTime=0f;
        
        while (running && !gameState.endGame()) {
            currentTime = System.nanoTime();
            // deltaTime is in seconds
            deltaTime = (currentTime-startTime) / 1_000_000_000f;//seconds
                cumulDeltaTime+=deltaTime;
            
            //if(_Log.LOG_ACTIVE) _Log.d("Delta Time", "deltaTime "+deltaTime+" < "+(1F/desiredFPS)+" ? "+(deltaTime < 1f/desiredFPS));
            if(logFPS){
                fpsDeltaTime = (currentTime-fpsTime) / 1_000_000_000f;
                if(deltaTime < 1f/desiredFPS)
                    continue;
            }
            
            startTime = currentTime;

            //If life is lost.. freeze the death screen for the first seconds
            if(scenicWaiting){
                if ( onLose_yetToWait>0 ){
                    onLose_yetToWait-=deltaTime;
                    if(onLose_yetToWait <= 0){
                        logic.restart();
                        if(periodicallyFullRender || showOnView){
                            view.fullRender();
                            view.fullRender();
                        }
                        onStart_yetToWait = onStart_Wait;
                    }
                    else{
                        continue;
                    }
                }
                //If (re)starting.. freeze the screen for the first seconds
                if ( onStart_yetToWait>0 ){
                    onStart_yetToWait-=deltaTime;
                    //continue;//Perde un frame ma è più rapido e pulito
                    if(onStart_yetToWait>0)
                        continue;
                    else{
                        onStart_yetToWait=0;
                        if(periodicallyFullRender || showOnView)
                            jFrame.setTitle("S Pacman:        "+logic.pac_lives+" lives left");
                    }
                }
            }

            gameState = logic.update( deltaTime );
            switch (gameState){
                case Play:
                    PlayerState playerState = logic.observeState_Player();
                    StrategyState behState = logic.observeState_Strategies();
                    if(playerState!=null){
                        _Log.a("Player State", "userState: "+playerState+" behState "+behState);
                        //_Log.a("Scores", "user score: "+DDA_Utils.userScore(playerState)+"\tsetup T score: "+DDA_Utils.setupScore(setup, behState));
                        _Log.a("Scores", "user score: "+DDA_Utils.userScore(playerState)+"\tsetup SBS score: "+DDA_Utils.setupScore(setup, null));
                        _Log.a("Setup", DDA_Utils.setupToString(setup));
                    }
                    break;
                case Win:
                    if (periodicallyFullRender || showOnView){
                        view.fullRender();
                        jFrame.setTitle("S Pacman:        Game Won");
                    }
                    _Log.a("Game", "Non Adaptive Pac-man:        Game Won\t"+logic.fullGameTimer+" s\n");
                    break;
                case GameOver:
                    if (periodicallyFullRender || showOnView){
                        view.fullRender();
                        jFrame.setTitle("S Pacman:        Game Over");
                    }
                    _Log.a("Game", "Non Adaptive Pac-man:        Game Over\t"+logic.fullGameTimer+" s\n");
                    break;
                case LifeLost:
                    if(periodicallyFullRender || showOnView){
                        view.fullRender();
                        jFrame.setTitle("S Pacman:        Life Lost !");
                    }
                    if(_Log.LOG_ACTIVE) _Log.e("PacMan Game", "Life Lost !");
                    gameState = GameState.Play;

                    if(scenicWaiting)
                        onLose_yetToWait = onLose_Wait;
                    else{ // Otherwise won't restart
                        logic.restart();
                        if (periodicallyFullRender || showOnView){
                            jFrame.setTitle("S Pacman:        "+logic.pac_lives+" lives left");
                            view.fullRender();
                            //view.fullRender();
                        }
                    }
                    continue;                
            }

            if( periodicallyFullRender && ++renderCounter%fullRenderEvery==0 ){
                renderCounter=0;
                view.fullRender();
                jFrame.setTitle("S Pacman:        "+logic.pac_lives+" lives left");
            }
            else if(showOnView)
                view.render();

            if(logFPS){// Measure FPS
                frameCounter++;
                if (fpsDeltaTime > 1) { // every second
                    avgFps=((avgFps*secondsCounter)+frameCounter)/(secondsCounter+1);
                        secondsCounter++;
                        if(_Log.LOG_ACTIVE) _Log.v("FPS", "curr FPS: "+frameCounter);
                    frameCounter = 0;
                    fpsTime = currentTime;
                }
            }
        }
        cumulDeltaTime = (System.nanoTime() - trueStartTime)/1_000_000_000F;
        if(_Log.LOG_ACTIVE) _Log.i("End", "Time: "+cumulDeltaTime+" s\tOutcome: "+gameState);
        //if(_Log.LOG_ACTIVE) _Log.v("FPS", "avg fps: "+avgFps);
    }
    
    private PacMan_Bot[] getPacBotsArray(){
        PacBot_WanderDirection_Getter wander[] = new PacBot_WanderDirection_Getter[]
        {   //new PacBot_WanderDirection_RandomTurns(logic), 
            new PacBot_WanderDirection_PelletGatherer(logic, 10), new PacBot_WanderDirection_PelletGatherer(logic, 15), 
            //new PacBot_ScoreBased_W(logic, 5), //new PacBot_ScoreBased_W(logic, 10), 
            new PacBot_WanderDirection_PelletBFS(logic),
            //new PacBot_WanderDirection_PelletBFS(logic), new PacBot_WanderDirection_PelletBFS(logic),
            new PacBot_WanderDirection_PelletBFS(logic), new PacBot_WanderDirection_PelletBFS(logic), new PacBot_WanderDirection_PelletBFS(logic)
        };
        String name_W[] = new String[]{/*"RandomTurns",*/ "PelletGather (10)", "PelletGather (15)", 
                //"ScoreBased (5)",// "ScoreBased (10)", 
                    "Pellet BFS", "Pellet BFS", "Pellet BFS", "Pellet BFS", /*"Pellet BFS", /*"Pellet BFS" */};
        //city e[S]cape
        PacBot_EscapeDirection_Getter escape[] = new PacBot_EscapeDirection_Getter[]
        {   new PacBot_EscapeDirection_Euclidean2_n(logic, 1),
                new PacBot_EscapeDirection_Euclidean2_n(logic, 4),
                    new PacBot_EscapeDirection_Euclidean2_n(logic, 7), 
            new PacBot_EscapeDirection_nStepsAhead_ToroEuclidean2(logic, 1),
                new PacBot_EscapeDirection_nStepsAhead_ToroEuclidean2(logic, 4),
                    new PacBot_EscapeDirection_nStepsAhead_ToroEuclidean2(logic, 7), 
            new PacBot_EscapeDirection_ManhattanProduct_n(logic, 1),
                new PacBot_EscapeDirection_ManhattanProduct_n(logic, 4),
                    new PacBot_EscapeDirection_ManhattanProduct_n(logic, 7), 
            new PacBot_EscapeDirection_BFS_n(logic, 1),
                new PacBot_EscapeDirection_BFS_n(logic, 4),
                    new PacBot_EscapeDirection_BFS_n(logic, 7),
                        new PacBot_EscapeDirection_BFS_n(logic, 10),
            new PacBot_EscapeDirection_BFS_n(logic, 1),
                new PacBot_EscapeDirection_BFS_n(logic, 4),
                    new PacBot_EscapeDirection_BFS_n(logic, 7),
                        new PacBot_EscapeDirection_BFS_n(logic, 10),
            new PacBot_ScoreBased_E(logic, 1),
                new PacBot_ScoreBased_E(logic, 4),
                    new PacBot_ScoreBased_E(logic, 7)
        };
        String name_E[] = new String[]{"Eucl2(1)", "Eucl2(4)", "Eucl2(7)",
                                        "ToroEucl2(1)", "ToroEucl2(4)", "ToroEucl2(7)",
                                        "ManhP(1)", "ManhP(4)", "ManhP(7)",
                                        "BFS(1)", "BFS(4)", "BFS(7)", "BFS(10)",
                                        "BFS(1)", "BFS(4)", "BFS(7)", "BFS(10)",
                                        "ScoreBased(1)", "ScoreBased(4)" , "ScoreBased(7)"};
        
        float alert[] = new float[]{2f, 2.5f, 3f};
        String name_A[] = new String[]{"alert 2", "alert 2.5", "alert 3"};
        
        float alert_tau_offset[] = new float[]{1f, 2f, 3f};
        String name_ATO[] = new String[]{"tau+1", "tau+2", "tau+3"};
        
        PacMan_Bot pacBots[] = new PacMan_Bot[wander.length * escape.length * alert.length * alert_tau_offset.length];
        int p=0;
        for (int a=0; a<alert.length; a++){
            for (int ato=0; ato<alert_tau_offset.length; ato++){
                for (int w=0; w<wander.length; w++){
                    for (int e=0; e<escape.length; e++){
                        String name = "PacBot "+name_W[w]+". "+name_E[e]+". "+name_A[a]+". "+name_ATO[ato];
                        pacBots[p++] = new PacMan_Bot(name, logic, 14, 23, Left, 
                                alert[a],
                                alert_tau_offset[ato],
                                wander[w],
                                escape[e] );
                    }
                }
            }
        }
        return pacBots;
    }
    private void logPacBotsStats(PacMan_Bot pacBots[], int pacBots_W[], int pacBots_L[]) {
        _Log.a("\n\n");
        _Log.a("PacBots Stats", "Win vs Loss");
        float winRatio=(winCounter*1f)/(winCounter+loseCounter), loseRatio=(loseCounter*1f)/(winCounter+loseCounter);
        /*for(int i=0; i<pacBots.length; i++)
            _Log.a("PacBots", pacBots[i]+":\t \t"
                    + "W: "+pacBots_W[i]+" ("+f((pacBots_W[i]*1f)/(pacBots_W[i]+pacBots_L[i]))+") {"+
                        ( ((pacBots_W[i]*1f)/(pacBots_W[i]+pacBots_L[i]))-winRatio >= 0f ? "+" : "")+
                        f( ((pacBots_W[i]*1f)/(pacBots_W[i]+pacBots_L[i])) - winRatio ) +"}\t \t"
                    + "L: "+pacBots_L[i]+" ("+f((pacBots_L[i]*1f)/(pacBots_W[i]+pacBots_L[i]))+") {"+
                        ( ((pacBots_L[i]*1f)/(pacBots_W[i]+pacBots_L[i]))-loseRatio >= 0f ? "+" : "")+
                        f( ((pacBots_L[i]*1f)/(pacBots_W[i]+pacBots_L[i])) - loseRatio ) +"}");
        */
        String name_W[] = new String[]{/*"RandomTurns",*/ "PelletGather (10)", "PelletGather (15)", 
                    //"ScoreBased (5)", //"ScoreBased (10)", 
                    "Pellet BFS",
                    //"Pellet BFS", "Pellet BFS", 
                    "Pellet BFS", "Pellet BFS", "Pellet BFS" };
        String name_E[] = new String[]{"Eucl2(1)", "Eucl2(4)", "Eucl2(7)",
                                        "ToroEucl2(1)", "ToroEucl2(4)", "ToroEucl2(7)",
                                        "ManhP(1)", "ManhP(4)", "ManhP(7)",
                                        "BFS(1)", "BFS(4)", "BFS(7)", "BFS(10)",
                                        "BFS(1)", "BFS(4)", "BFS(7)", "BFS(10)",
                                        "ScoreBased(1)", "ScoreBased(4)" , "ScoreBased(7)"};
        String name_A[] = new String[]{"alert 2", "alert 2.5", "alert 3"};
        String name_ATO[] = new String[]{"tau+1", "tau+2", "tau+3"};
        int wander_W[] = new int[name_W.length], wander_L[]= new int[name_W.length];
        int escape_W[] = new int[name_E.length], escape_L[]= new int[name_E.length];
        int alert_W[] = new int[name_A.length], alert_L[]= new int[name_A.length];
        int ato_W[] = new int[name_ATO.length], ato_L[]= new int[name_ATO.length];
        _Log.a("\n\n");
        _Log.a("PacBots Stats", "per strategy");
        int p=0;
        for (int a=0; a<alert_W.length; a++){
            for (int ato=0; ato<ato_W.length; ato++){
                for (int w=0; w<wander_W.length; w++){
                    for (int e=0; e<escape_W.length; e++){
                        alert_W[a] += pacBots_W[p];
                        ato_W[ato] += pacBots_W[p];
                        wander_W[w] += pacBots_W[p];
                        escape_W[e] += pacBots_W[p];

                        alert_L[a] += pacBots_L[p];
                        ato_L[ato] += pacBots_L[p];
                        wander_L[w] += pacBots_L[p];
                        escape_L[e] += pacBots_L[p];

                        p++;
                    }
                }
            }
        }
        
        for (int a=0; a<alert_W.length; a++)
            _Log.a("Alert Stats", name_A[a]+"\t \t \t"+
                    alert_W[a]+" wins ("+f( (alert_W[a]*1f)/(alert_W[a]+alert_L[a]) )+") {"+
                        ( ((alert_W[a]*1f)/(alert_W[a]+alert_L[a])) - winRatio >= 0f ? "+" : "")+
                        f( ((alert_W[a]*1f)/(alert_W[a]+alert_L[a])) - winRatio ) +"}\t \t"+
                    alert_L[a]+" loss ("+f( (alert_L[a]*1f)/(alert_W[a]+alert_L[a]) )+") {"+
                        ( ((alert_L[a]*1f)/(alert_W[a]+alert_L[a])) - loseRatio >= 0f ? "+" : "")+
                        f( ((alert_L[a]*1f)/(alert_W[a]+alert_L[a])) - loseRatio ) +"}");
        
        for (int ato=0; ato<ato_W.length; ato++)
            _Log.a("ATO Stats", name_ATO[ato]+"\t \t \t"+
                    ato_W[ato]+" wins ("+f( (ato_W[ato]*1f)/(ato_W[ato]+ato_L[ato]) )+") {"+
                        ( ((ato_W[ato]*1f)/(ato_W[ato]+ato_L[ato])) - winRatio >= 0f ? "+" : "")+
                        f( ((ato_W[ato]*1f)/(ato_W[ato]+ato_L[ato])) - winRatio ) +"}\t \t"+
                    ato_L[ato]+" loss ("+f( (ato_L[ato]*1f)/(ato_W[ato]+ato_L[ato]) )+") {"+
                        ( ((ato_L[ato]*1f)/(ato_W[ato]+ato_L[ato])) - loseRatio >= 0f ? "+" : "")+
                        f( ((ato_L[ato]*1f)/(ato_W[ato]+ato_L[ato])) - loseRatio ) +"}");
        
        for (int w=0; w<wander_W.length; w++)
            _Log.a("Wander Stats", name_W[w]+"\t \t \t"
                    +wander_W[w]+" wins ("+f( (wander_W[w]*1f)/(wander_W[w]+wander_L[w]) )+") {"+
                        ( ((wander_W[w]*1f)/(wander_W[w]+wander_L[w])) - winRatio >= 0f ? "+" : "")+
                        f( ((wander_W[w]*1f)/(wander_W[w]+wander_L[w])) - winRatio ) +"}\t"+
                    wander_L[w]+" loss ("+f( (wander_L[w]*1f)/(wander_W[w]+wander_L[w]) )+") {"+
                        ( ((wander_L[w]*1f)/(wander_W[w]+wander_L[w])) - loseRatio >= 0f ? "+" : "")+
                        f( ((wander_L[w]*1f)/(wander_W[w]+wander_L[w])) - loseRatio ) +"}");
        
        for (int e=0; e<escape_W.length; e++)
            _Log.a("Escape Stats", name_E[e]+"\t \t \t"+
                    escape_W[e]+" wins ("+f( (escape_W[e]*1f)/(escape_W[e]+escape_L[e]) )+") {"+
                        ( ((escape_W[e]*1f)/(escape_W[e]+escape_L[e])) - winRatio >= 0f ? "+" : "")+
                        f( ((escape_W[e]*1f)/(escape_W[e]+escape_L[e])) - winRatio ) +"}\t"+
                    escape_L[e]+" loss ("+f( (escape_L[e]*1f)/(escape_W[e]+escape_L[e]) )+") {"+
                        ( ((escape_L[e]*1f)/(escape_W[e]+escape_L[e])) - loseRatio >= 0f ? "+" : "")+
                        f( ((escape_L[e]*1f)/(escape_W[e]+escape_L[e])) - loseRatio ) +"}");
    }
    
    private Setup[] getAgentsStrategiesArray(){
        //Env
        float ghostSpeed[] = new float[]{-0.075f, -0.025f, 0f, 0.025f, 0.05f};
        String ghostSpeed_S[] = new String[]{"Ghost Speed -7.5%", "Ghost Speed -2.5%", "Ghost Speed +0%", "Ghost Speed +2.5%", "Ghost Speed +5%"};
        Ghost_UpTurn_Constraint upTurn[] = new Ghost_UpTurn_Constraint[]{new Ghost_UpTurn_Constraint(false)};
        String upTurn_S[] = new String[]{"allowed upTurns"};
        Ghost_DistanceEvaluator distanceEvals[] = new Ghost_DistanceEvaluator[]{new ManhattanDistanceEvaluator()};
            String distanceEvals_S[] = new String[]{"Manhattan distance eval"};
        float frightTimes[] = new float[]{5.5f, 4.5f, 4f};
            String frightTimes_S[] = new String[]{"Fright Time 5.5s", "Fright Time 4.5s", "Fright Time 4s"};
        
        //Blinky
        int elroy1_dotsLeft[] = new int[]{30, 50, 70, 100};
            String elroy1_dotsLeft_S[] = new String[]{"Elroy1_DL 30", "Elroy1_DL 50", "Elroy1_DL 70", "Elroy1_DL 100"};
        int elroyChases[] = new int[]{2, 3};
            String elroyChases_S[] = new String[]{"Elroy chases on lvl2", "Elroy doesn't chase"};
        ChaseTargetGetter blinkyChase[] = new ChaseTargetGetter[]{ new ChaseTarget_Blinky_RoamAround(2),
            new ChaseTarget_Blinky_RoamAround(4), new ChaseTarget_Blinky_RoamAround(5) };
            String blinkyChase_S[] = new String[]{"Blinky Chase Roam 2", "Blinky Chase Roam 4", "Blinky Chase Roam 5"};
        ScatterTargetGetter blinkyScatter[] = new ScatterTargetGetter[]{ new ScatterTarget_Blinky_RandomArea(14, -2, 27, 8),
            new ScatterTarget_Blinky_RandomArea(14, 8, 20, 14), new ScatterTarget_Blinky_RandomArea(9, 11, 18, 17) };
            String blinkyScatter_S[] = new String[]{"Blinky Scatter Rand(Up Right)", "Blinky Scatter Rand(House NE)", "Blinky Scatter Rand(Around House)"};
        
        //Pinky
        ChaseTargetGetter pinkyChase[] = new ChaseTargetGetter[]{
            new ChaseTarget_Pinky_Std(2), new ChaseTarget_Pinky_Std(6), new ChaseTarget_Pinky_Std(8), 
            new ChaseTarget_Pinky_BugFix(), new ChaseTarget_Pinky_BugFix(6), 
            new ChaseTarget_Pinky_NextIntersection()
        };
        String pinkyChase_S[] = new String[]{"Pinky Chase Std(2)", "Pinky Chase Std(6)", "Pinky Chase Std(8)", 
            "Pinky Chase BugFix", "Pinky Chase BugFix(6)", "Pinky Chase NextIntersection"};
        ScatterTargetGetter pinkyScatter[] = new ScatterTargetGetter[]{ new ScatterTarget_RandomArea(0, -2, 13, 8)
                , new ScatterTarget_RandomArea(6, 8, 13, 14), new ScatterTarget_RandomArea(9, 11, 18, 17) };
        String pinkyScatter_S[] = new String[]{"Pinky Scatter Rand(Up Left)", "Pinky Scatter Rand(House NW)", "Pinky Scatter Rand(Around House)"};
        
        //Inky
        ChaseTargetGetter inkyChase[] = new ChaseTargetGetter[]{
            new ChaseTarget_Inky_Std(5), new ChaseTarget_Inky_Std(6),
            new ChaseTarget_Inky_BugFix(0), new ChaseTarget_Inky_BugFix(6),
            new ChaseTarget_Inky_Clipped(2.5f), new ChaseTarget_Inky_Clipped(4f), new ChaseTarget_Inky_Clipped(6f)
        };
        String inkyChase_S[] = new String[]{"Inky Chase Std(5)", "Inky Chase Std(6)",
             "Inky Chase BugFix(0)", "Inky Chase BugFix(6)",
            "Inky Chase Clipped(2.5)", "Inky Chase Clipped(4)", "Inky Chase Clipped(6)"
        };
        ScatterTargetGetter inkyScatter[] = new ScatterTargetGetter[]{ new ScatterTarget_RandomArea(14, 20, 27, 32)
                , new ScatterTarget_RandomArea(14, 14, 20, 20), new ScatterTarget_RandomArea(9, 11, 18, 17) };
        String inkyScatter_S[] = new String[]{"Inky Scatter Rand(Down Right)", "Inky Scatter Rand(House SE)", "Inky Scatter Rand(Around House)"};
        
        //Clyde
        ChaseTargetGetter clydeChase[] = new ChaseTargetGetter[]{
            new ChaseTarget_Clyde_Std(12),
            new ChaseTarget_Clyde_chaseAhead(5, 3), new ChaseTarget_Clyde_chaseAhead(12, 3),
            new ChaseTarget_Clyde_PacmanClosestDot(8), new ChaseTarget_Clyde_PacmanClosestDot(12),
            new ChaseTarget_Clyde_PacmanClosestDot(8, 3), new ChaseTarget_Clyde_PacmanClosestDot(5, 3),
            new ChaseTarget_Clyde_PacmanClosestEnerg(5), new ChaseTarget_Clyde_PacmanClosestEnerg(12),
        };
        String clydeChase_S[] = new String[]{"Clyde Chase Std(12)", "Clyde Chase Std(5, 3)", "Clyde Chase Std(12, 3)",
            "Clyde Chase ClosestDot", "Clyde Chase ClosestDot(12)",
            "Clyde Chase ClosestDot(8, 3)", "Clyde Chase ClosestDot(5, 3)",
            "Clyde Chase ClosestEnerg(5)", "Clyde Chase ClosestEnerg(12)"};
        ScatterTargetGetter clydeScatter[] = new ScatterTargetGetter[]{ new ScatterTarget_RandomArea(0, 20, 13, 32)
                , new ScatterTarget_RandomArea(6, 14, 13, 20), new ScatterTarget_RandomArea(9, 11, 18, 17) };
        String clydeScatter_S[] = new String[]{"Clyde Scatter Rand(Down Left)", "Clyde Scatter Rand(House SW)", "Clyde Scatter Rand(Around House)"};
        
        
        Setup specifics[] = new Setup[1 +
                ghostSpeed.length + upTurn.length + distanceEvals.length + frightTimes.length + 
                elroy1_dotsLeft.length + elroyChases.length + blinkyChase.length + blinkyScatter.length +
                pinkyChase.length + pinkyScatter.length +
                    inkyChase.length + inkyScatter.length +
                        clydeChase.length + clydeScatter.length
                ];
        int count=0;
        _Log.a(specifics.length+" behs");
        specifics[count++] = Setup.std();
        for (int gS=0; gS<ghostSpeed.length; gS++){
            Setup spec = Setup.std();
            spec.lvl.set_Ghost_Speed(ghostSpeed[gS] );
            spec.name = ghostSpeed_S[gS];
            specifics[count++]=spec;
        }
        for (int uT=0; uT<upTurn.length; uT++){
            Setup spec = Setup.std();
            spec.upTurn_Constraint = upTurn[uT];
            spec.name = upTurn_S[uT];
            specifics[count++]=spec;
        }
        for (int de=0; de<distanceEvals.length; de++){
            Setup spec = Setup.std();
            spec.distance = distanceEvals[de];
            spec.name = distanceEvals_S[de];
            specifics[count++]=spec;
        }
        for (int ft=0; ft<frightTimes.length; ft++){
            Setup spec = Setup.std();
            spec.lvl.set_Fright_Time(frightTimes[ft]);
            spec.name = frightTimes_S[ft];
            specifics[count++]=spec;
        }
        for (int e1dL=0; e1dL<elroy1_dotsLeft.length; e1dL++){
            Setup spec = Setup.std();
            spec.lvl.set_Elroy_1_Activation( elroy1_dotsLeft[e1dL] );
            spec.name = elroy1_dotsLeft_S[e1dL];
            specifics[count++]=spec;
        }
        for (int eC=0; eC<elroyChases.length; eC++){
            Setup spec = Setup.std();
            //spec.elroyChasesOnScatter_Constraint = elroyChases[eC];
            spec.lvl.set_Elroy_AlwaysChase( elroyChases[eC] );
            spec.name = elroyChases_S[eC];
            specifics[count++]=spec;
        }
        for (int bC=0; bC<blinkyChase.length; bC++){
            Setup spec = Setup.std();
            spec.blinky_C = blinkyChase[bC];
            spec.name = blinkyChase_S[bC];
            specifics[count++]=spec;
        }
        for (int bS=0; bS<blinkyScatter.length; bS++){
            Setup spec = Setup.std();
            spec.blinky_S = blinkyScatter[bS];
            spec.name = blinkyScatter_S[bS];
            specifics[count++]=spec;
        }
        for (int pC=0; pC<pinkyChase.length; pC++){
            Setup spec = Setup.std();
            spec.pinky_C = pinkyChase[pC];
            spec.name = pinkyChase_S[pC];
            specifics[count++]=spec;
        }
        for (int pS=0; pS<pinkyScatter.length; pS++){
            Setup spec = Setup.std();
            spec.pinky_S = pinkyScatter[pS];
            spec.name = pinkyScatter_S[pS];
            specifics[count++]=spec;
        }
        for (int iC=0; iC<inkyChase.length; iC++){
            Setup spec = Setup.std();
            spec.inky_C = inkyChase[iC];
            spec.name = inkyChase_S[iC];
            specifics[count++]=spec;
        }
        for (int iS=0; iS<inkyScatter.length; iS++){
            Setup spec = Setup.std();
            spec.inky_S = inkyScatter[iS];
            spec.name = inkyScatter_S[iS];
            specifics[count++]=spec;
        }
        for (int cC=0; cC<clydeChase.length; cC++){
            Setup spec = Setup.std();
            spec.clyde_C = clydeChase[cC];
            spec.name = clydeChase_S[cC];
            specifics[count++]=spec;
        }
        for (int cS=0; cS<clydeScatter.length; cS++){
            Setup spec = Setup.std();
            spec.clyde_S = clydeScatter[cS];
            spec.name = clydeScatter_S[cS];
            specifics[count++]=spec;
        }
        //for (count=0; count<specifics.length; count++) _Log.a("Strategy", specifics[count]+"");
        return specifics;
    }
    private void logAgentsStrategiesStats(Setup strategies[], int strategies_W[], int strategies_L[]) {
        _Log.a("\n\n");
        _Log.a("Behaviours' Stats", "Win vs Loss");
        float winRatio=(strategies_W[0]*1f)/(strategies_W[0]+strategies_L[0]), loseRatio=(strategies_L[0]*1f)/(strategies_W[0]+strategies_L[0]);
        for(int i=0; i<strategies.length; i++)
            _Log.a("Behaviours", strategies[i]+":\t \t"
                    + "W: "+strategies_W[i]+" ("+f((strategies_W[i]*1f)/(strategies_W[i]+strategies_L[i]))+") {"+
                        ( ((strategies_W[i]*1f)/(strategies_W[i]+strategies_L[i]))-winRatio >= 0f ? "+" : "")+
                        f( ((strategies_W[i]*1f)/(strategies_W[i]+strategies_L[i])) - winRatio ) +"}\t \t"
                    + "L: "+strategies_L[i]+" ("+f((strategies_L[i]*1f)/(strategies_W[i]+strategies_L[i]))+") {"+
                        ( ((strategies_L[i]*1f)/(strategies_W[i]+strategies_L[i]))-loseRatio >= 0f ? "+" : "")+
                        f( ((strategies_L[i]*1f)/(strategies_W[i]+strategies_L[i])) - loseRatio ) +"}");
    }
    
    private Setup[] getSingleStrategyArray(){
        float ghostSpeed[] = new float[]{0.025f};
        String ghostSpeed_S[] = new String[]{"Ghost Speed +2.5%"};
        
        Setup specifics[] = new Setup[ 1 + ghostSpeed.length  ];
        int count=0;
        _Log.a(specifics.length+" strategies");
        specifics[count++] = Setup.std();
        for (int gS=0; gS<ghostSpeed.length; gS++){
            Setup spec = Setup.std();
            spec.lvl.set_Ghost_Speed( ghostSpeed[gS] );
            spec.name = ghostSpeed_S[gS];
            specifics[count++]=spec;
        }
        return specifics;
    }
    
    private Pair<Pair<Object, String>[][], Adapter[]> getFamiliesStrategies(){
        //Env
        float ghostSpeed[] = new float[]{-0.05f, -0.075f, -0.025f, 0f, 0.025f, 0.05f};
            String ghostSpeed_S[] = new String[]{"Ghost Speed -5%", "Ghost Speed -7.5%", 
                "Ghost Speed -2.5%", "Ghost Speed +0%", "Ghost Speed +2.5%", "Ghost Speed +5%"};
        Ghost_UpTurn_Constraint upTurn[] = new Ghost_UpTurn_Constraint[]{new Ghost_UpTurn_Constraint(true), new Ghost_UpTurn_Constraint(false)};
            String upTurn_S[] = new String[]{"not allowed upTurns", "allowed upTurns"};
        Ghost_DistanceEvaluator distanceEvals[] = new Ghost_DistanceEvaluator[]{new Euclidean2DistanceEvaluator(), new ManhattanDistanceEvaluator()};
            String distanceEvals_S[] = new String[]{"Euclidean2 distance eval", "Manhattan distance eval"};
        float frightTimes[] = new float[]{5f, 5.5f, 4.5f, 4f};
            String frightTimes_S[] = new String[]{"Fright Time 5s", "Fright Time 5.5s", "Fright Time 4.5s", "Fright Time 4s"};
            
        //Blinky
        int elroy1_dotsLeft[] = new int[]{20, 30, 50, 70, 100};
            String elroy1_dotsLeft_S[] = new String[]{"Elroy1_DL 20", "Elroy1_DL 30", "Elroy1_DL 50", "Elroy1_DL 70", "Elroy1_DL 100"};
        int elroyChases[] = new int[]{1, 2, 3};
            String elroyChases_S[] = new String[]{"Elroy chases on lvl1", "Elroy chases on lvl2", "Elroy doesn't chase"};
        
        ChaseTargetGetter blinkyChase[] = new ChaseTargetGetter[]{ new ChaseTarget_Blinky_Std(), 
                new ChaseTarget_Blinky_RoamAround(2), new ChaseTarget_Blinky_RoamAround(4), new ChaseTarget_Blinky_RoamAround(5) };
            String blinkyChase_S[] = new String[]{"Blinky Chase Std", "Blinky Chase Roam 2", "Blinky Chase Roam 4", "Blinky Chase Roam 5"};
        ScatterTargetGetter blinkyScatter[] = new ScatterTargetGetter[]{ 
                new ScatterTarget_Blinky_Std(25, -3), new ScatterTarget_Blinky_RandomArea(14, -2, 27, 8),
                    new ScatterTarget_Blinky_RandomArea(14, 8, 20, 14), new ScatterTarget_Blinky_RandomArea(9, 11, 18, 17) };
            String blinkyScatter_S[] = new String[]{"Blinky Scatter Std", "Blinky Scatter Rand(Up Right)", "Blinky Scatter Rand(House NE)", "Blinky Scatter Rand(Around House)"};
            
        //Pinky
        ChaseTargetGetter pinkyChase[] = new ChaseTargetGetter[]{
            new ChaseTarget_Pinky_Std(),
            new ChaseTarget_Pinky_Std(2), new ChaseTarget_Pinky_Std(6), new ChaseTarget_Pinky_Std(8), 
            new ChaseTarget_Pinky_BugFix(), new ChaseTarget_Pinky_BugFix(6), 
            new ChaseTarget_Pinky_NextIntersection()
        };
        String pinkyChase_S[] = new String[]{"Pinky Chase Std",
            "Pinky Chase Std(2)", "Pinky Chase Std(6)", "Pinky Chase Std(8)", 
            "Pinky Chase BugFix", "Pinky Chase BugFix(6)",
            "Pinky Chase NextIntersection"};
        ScatterTargetGetter pinkyScatter[] = new ScatterTargetGetter[]{ 
            new ScatterTargetGetter_Const(3, -3), new ScatterTarget_RandomArea(0, -2, 13, 8),
                new ScatterTarget_RandomArea(6, 8, 13, 14), new ScatterTarget_RandomArea(9, 11, 18, 17) };
            String pinkyScatter_S[] = new String[]{"Pinky Scatter Std", "Pinky Scatter Rand(Up Left)", "Pinky Scatter Rand(House NW)", "Pinky Scatter Rand(Around House)"};
 
        //Inky
        ChaseTargetGetter inkyChase[] = new ChaseTargetGetter[]{
            new ChaseTarget_Inky_Std(), new ChaseTarget_Inky_Std(5), new ChaseTarget_Inky_Std(6),
            new ChaseTarget_Inky_BugFix(0), new ChaseTarget_Inky_BugFix(6), 
            new ChaseTarget_Inky_Clipped(2.5f), new ChaseTarget_Inky_Clipped(4f), new ChaseTarget_Inky_Clipped(6f)
        };
            String inkyChase_S[] = new String[]{"Inky Chase Std", "Inky Chase Std(5)", "Inky Chase Std(6)",
                "Inky Chase BugFix(0)", "Inky Chase BugFix(6)",
                "Inky Chase Clipped(2.5)", "Inky Chase Clipped(4)", "Inky Chase Clipped(6)"
            };
        ScatterTargetGetter inkyScatter[] = new ScatterTargetGetter[]{ 
            new ScatterTargetGetter_Const(25, 32), new ScatterTarget_RandomArea(14, 20, 27, 32)
                , new ScatterTarget_RandomArea(14, 14, 20, 20), new ScatterTarget_RandomArea(9, 11, 18, 17) };        
            String inkyScatter_S[] = new String[]{"Inky Scatter Std", "Inky Scatter Rand(Down Right)",
                "Inky Scatter Rand(House SE)", "Inky Scatter Rand(Around House)"};

        //Clyde
        ChaseTargetGetter clydeChase[] = new ChaseTargetGetter[]{
            new ChaseTarget_Clyde_Std(), new ChaseTarget_Clyde_Std(12),
            new ChaseTarget_Clyde_chaseAhead(5, 3), new ChaseTarget_Clyde_chaseAhead(12, 3),
            new ChaseTarget_Clyde_PacmanClosestDot(8), new ChaseTarget_Clyde_PacmanClosestDot(12),
            new ChaseTarget_Clyde_PacmanClosestDot(8, 3), new ChaseTarget_Clyde_PacmanClosestDot(5, 3),
            new ChaseTarget_Clyde_PacmanClosestEnerg(5), new ChaseTarget_Clyde_PacmanClosestEnerg(12),
        };
            String clydeChase_S[] = new String[]{"Clyde Chase Std", "Clyde Chase Std(12)", "Clyde Chase Std(5, 3)", "Clyde Chase Std(12, 3)",
            "Clyde Chase ClosestDot", "Clyde Chase ClosestDot(12)",
            "Clyde Chase ClosestDot(8, 3)", "Clyde Chase ClosestDot(5, 3)",
            "Clyde Chase ClosestEnerg(5)", "Clyde Chase ClosestEnerg(12)"};
        ScatterTargetGetter clydeScatter[] = new ScatterTargetGetter[]{ 
            new ScatterTargetGetter_Const(3, 32), new ScatterTarget_RandomArea(0, 20, 13, 32)
                , new ScatterTarget_RandomArea(6, 14, 13, 20), new ScatterTarget_RandomArea(9, 11, 18, 17) };
            String clydeScatter_S[] = new String[]{"Clyde Scatter Std", "Clyde Scatter Rand(Down Left)",
                "Clyde Scatter Rand(House SW)", "Clyde Scatter Rand(Around House)"};
        
        Pair<Object, String> objs[][] = new Pair[FAMILIES][];
        Adapter adapters[] = new Adapter[FAMILIES];
        int b=0, f=0;
        //
        objs[f] = new Pair[ghostSpeed.length];
        for (int gS=0; gS<ghostSpeed.length; gS++)
            objs[f][b++] = new Pair<>(ghostSpeed[gS], ghostSpeed_S[gS]);
        adapters[f++] = new GhostSpeed_Adapter();
        
        b=0;
        objs[f] = new Pair[upTurn.length];
        for (int uT=0; uT<upTurn.length; uT++)
            objs[f][b++] = new Pair<>(upTurn[uT], upTurn_S[uT]);
        adapters[f++] = new UpTurnConstraint_Adapter();
        
        b=0;
        objs[f] = new Pair[distanceEvals.length];
        for (int de=0; de<distanceEvals.length; de++)
            objs[f][b++] = new Pair<>(distanceEvals[de], distanceEvals_S[de]);
        adapters[f++] = new DistanceEvaluator_Adapter();
        
        b=0;
        objs[f] = new Pair[frightTimes.length];
        for (int de=0; de<frightTimes.length; de++)
            objs[f][b++] = new Pair<>(frightTimes[de], frightTimes_S[de]);
        adapters[f++] = new FrightTime_Adapter();
        
        b=0;
        objs[f] = new Pair[elroy1_dotsLeft.length];
        for (int e1dL=0; e1dL<elroy1_dotsLeft.length; e1dL++)
            objs[f][b++] = new Pair<>(elroy1_dotsLeft[e1dL], elroy1_dotsLeft_S[e1dL]);;
        adapters[f++] = new Elroy1_DotsLeft_Adapter();
        
        b=0;
        objs[f] = new Pair[elroyChases.length];
        for (int eC=0; eC<elroyChases.length; eC++)
            objs[f][b++] = new Pair<>(elroyChases[eC], elroyChases_S[eC]);
        adapters[f++] = new Elroy_Chases_Adapter();
        
        b=0;
        objs[f] = new Pair[blinkyChase.length];
        for (int bC=0; bC<blinkyChase.length; bC++)
            objs[f][b++] = new Pair<>(blinkyChase[bC], blinkyChase_S[bC]);
        adapters[f++] = new Blinky_ChaseTargetGetter_Adapter();
        
        b=0;
        objs[f] = new Pair[blinkyScatter.length];
        for (int bS=0; bS<blinkyScatter.length; bS++)
            objs[f][b++] = new Pair<>(blinkyScatter[bS], blinkyScatter_S[bS]);
        adapters[f++] = new Blinky_ScatterTargetGetter_Adapter();
        
        b=0;
        objs[f] = new Pair[pinkyChase.length];
        for (int pC=0; pC<pinkyChase.length; pC++)
            objs[f][b++] = new Pair<>(pinkyChase[pC], pinkyChase_S[pC]);
        adapters[f++] = new Pinky_ChaseTargetGetter_Adapter();
        
        b=0;
        objs[f] = new Pair[pinkyScatter.length];
        for (int pS=0; pS<pinkyScatter.length; pS++)
            objs[f][b++] = new Pair<>(pinkyScatter[pS], pinkyScatter_S[pS]);
        adapters[f++] = new Pinky_ScatterTargetGetter_Adapter();
        
        b=0;
        objs[f] = new Pair[inkyChase.length];
        for (int iC=0; iC<inkyChase.length; iC++)
            objs[f][b++] = new Pair<>(inkyChase[iC], inkyChase_S[iC]);
        adapters[f++] = new Inky_ChaseTargetGetter_Adapter();
        
        b=0;
        objs[f] = new Pair[inkyScatter.length];
        for (int iS=0; iS<inkyScatter.length; iS++)
            objs[f][b++] = new Pair<>(inkyScatter[iS], inkyScatter_S[iS]);
        adapters[f++] = new Inky_ScatterTargetGetter_Adapter();
        
        b=0;
        objs[f] = new Pair[clydeChase.length];
        for (int cC=0; cC<clydeChase.length; cC++)
            objs[f][b++] = new Pair<>(clydeChase[cC], clydeChase_S[cC]);
        adapters[f++] = new Clyde_ChaseTargetGetter_Adapter();
        
        b=0;
        objs[f] = new Pair[clydeScatter.length];
        for (int cS=0; cS<clydeScatter.length; cS++)
            objs[f][b++] = new Pair<>(clydeScatter[cS], clydeScatter_S[cS]);
        adapters[f++] = new Clyde_ScatterTargetGetter_Adapter();
        
        int count=0;
        for(int i=0; i<objs.length; i++)
            count+=objs[i].length;
        _Log.a(count +" Behs. "+objs.length+" families");
        /*
        for(int i=0; i<objs.length; i++)
            for(int j=0; j<objs[i].length; j++)
                _Log.a("F:"+i+"\tB:"+j+":\t"+objs[i][j].getValue());*/
        return new Pair<>(objs, adapters);
    }
    
    void signKeyListener(PacMan_Player pacman_player) {
        view.addKeyListener(pacman_player);
    }
    
    /**Truncates the float to percentage*/
    static String f(float f){
        return ((int)(f*10000))/100f + " %";
    }
    
    
    
    public static void main (String... args){
        /**
         * 0: Dot
         * 1: Wall
         * 6: Empty
         * 8: Energizer
         * 7: Ghost Door
         * 3: Up Exception EMPTY
         * 9: Up Exception DOT
         */
        int[][] file = new int[][]{
            //                            10                            20                    27
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, 
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, 
            {1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1}, 
            {1, 8, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 8, 1}, 
            {1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1}, 
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, 
            {1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1}, 
            {1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1}, 
            {1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1}, 
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 6, 1, 1, 6, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1}, 
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 6, 1, 1, 6, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1},//10 
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 6, 6, 6, 3, 2, 2, 3, 6, 6, 6, 1, 1, 0, 1, 1, 1, 1, 1, 1}, 
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 6, 1, 1, 1, 7, 7, 1, 1, 1, 6, 1, 1, 0, 1, 1, 1, 1, 1, 1}, 
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 6, 1, 1, 5, 5, 5, 5, 1, 1, 6, 1, 1, 0, 1, 1, 1, 1, 1, 1}, 
            {4, 4, 4, 4, 4, 4, 0, 6, 6, 6, 1, 1, 5, 5, 5, 5, 1, 1, 6, 6, 6, 0, 4, 4, 4, 4, 4, 4}, 
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 6, 1, 1, 5, 5, 5, 5, 1, 1, 6, 1, 1, 0, 1, 1, 1, 1, 1, 1}, 
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 6, 1, 1, 1, 1, 1, 1, 1, 1, 6, 1, 1, 0, 1, 1, 1, 1, 1, 1}, 
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 1, 1, 0, 1, 1, 1, 1, 1, 1}, 
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 6, 1, 1, 1, 1, 1, 1, 1, 1, 6, 1, 1, 0, 1, 1, 1, 1, 1, 1}, 
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 6, 1, 1, 1, 1, 1, 1, 1, 1, 6, 1, 1, 0, 1, 1, 1, 1, 1, 1}, 
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, //r:20
            {1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1}, 
            {1, 8, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 8, 1}, 
            {1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 9, 6, 6, 9, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1}, 
            {1, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 1}, 
            {1, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 1}, 
            {1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1}, 
            {1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1}, 
            {1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1}, 
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, 
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}//30
        };
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                //logCSV();
                
                _Log.a(test+".");
                Board board = new Board(file);
                Game_PacMan game = new Game_PacMan(board);
                if(_Log.LOG_ACTIVE) _Log.d("Init", board.rows+" "+board.cols);
                _Log.a("Init", board.rows+" "+board.cols);
                //_Log.a("Init", "BheavioursScore: 10k, min 500, gamma 1, reward -0.04");
                game.gameLoop();
                
            }            

            private void logCSV() {
                String B[] = new String[]{"Standard", "Ghost Speed -7.5%", "Ghost Speed +0%", "Ghost Speed +5%", 
                    "allowed upTurns", "Manhattan distance eval", "Fright Time 5.5s", "Fright Time 4.5s", "Fright Time 4s", 
                    "Elroy1_DL 30", "Elroy1_DL 50", "Elroy1_DL 70", "Elroy1_DL 100", "Elroy chases on lvl2", "Elroy doesn't chase",
                    "Blinky Chase Roam 2", "Blinky Chase Roam 4", "Blinky Chase Roam 5",
                    "Blinky Scatter Rand Up Right", "Blinky Scatter Rand House NE", "Blinky Scatter Rand Around House",
                    "Pinky Chase Std 2", "Pinky Chase Std 6", "Pinky Chase Std 8", 
                    "Pinky Chase BugFix", "Pinky Chase BugFix 6", "Pinky Chase NextIntersection",
                    "Pinky Scatter Rand Up Left", "Pinky Scatter Rand House NW", "Pinky Scatter Rand Around House",
                    "Inky Chase Std 5", "Inky Chase Std 6",
                     "Inky Chase BugFix 0", "Inky Chase BugFix 6",
                    "Inky Chase Clipped 2.5", "Inky Chase Clipped 4", "Inky Chase Clipped 6",
                    "Inky Scatter Rand Down Right", "Inky Scatter Rand House SE", "Inky Scatter Rand Around House",
                    "Clyde Chase Std 12", "Clyde Chase Std 5, 3", "Clyde Chase Std 12, 3",
                    "Clyde Chase ClosestDot", "Clyde Chase ClosestDot 12",
                    "Clyde Chase ClosestDot 8, 3", "Clyde Chase ClosestDot 5, 3",
                    "Clyde Chase ClosestEnerg 5", "Clyde Chase ClosestEnerg 12",
                    "Clyde Scatter Rand Down Left", "Clyde Scatter Rand House SW", "Clyde Scatter Rand Around House"};
                String P[] = new String[]{"0-15 sec", "15-30 sec", "30-45 sec",
                    "45 sec-1'", "1'-1'15''", "1'15''-1'30''", "1'30''-1'45''", "1'45''+"};
                String C[] = new String[]{"0-75 dots", "75-150 dots", "150-200 dots", "200+ dots"};
                String L[] = new String[]{"0 lives left", "1 life left", "2 lives left"};
                
                for(int b=0; b<B.length; b++){
                for(int p=0; p<P.length; p++){
                for(int c=0; c<C.length; c++){
                for(int l=0; l<L.length; l++){
                    System.out.println(B[b]+";"+P[p]+";"+C[c]+";"+L[l]+";");
                }
                }
                }
                }
            }
        });
    }
}