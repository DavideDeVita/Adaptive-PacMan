package pacman;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import static pacman.Collectible.Door;
import static pacman.Direction.*;
import static pacman.GhostPersonalState.*;

/**
 *
 * @author Falie
 */
public class GameLogic {
    final Board board;
    
    //Agents
    final static int NUM_GHOSTS = 4,
            BLINKY=0, PINKY=1, INKY=2, CLYDE=3;
    public PacMan pacman;
    public Ghost[] ghosts;
    
    //Game
    public int dotsLeft, energizersLeft;
    int pac_lives;
    private float frightTimer=0f;
    private float timer=0f;
    public float fullGameTimer=0f;
    public TimerState timerState;
    private boolean pacmanCollectedADot = false;
    private final Vector previousPacTile = new Vector(0, 0);
    
    //Features
    final static int SPEED = 660; //units per second        //660
    private KeyListener keyListener=null;
    
    //Custom Specifics
    private Setup setup;
    private LevelSpecifics lvl;
    
    //StateObservation
    private StateObservation_Player playerObservation;
    private StateObservation_Strategies agentsObservation;

    GameLogic(Board board, Game_PacMan pmg, Setup builder) {
        this.board = board;
        
        this.setup = builder;
        this.lvl = builder.lvl;
        
        this.dotsLeft = board.dots;
        this.energizersLeft = board.energizers;
        this.pac_lives = builder.pac_lives;
        this.timerState = TimerState.Scatter_1;
    }
    
    void setPlayerObservation(float period){
        this.playerObservation = new StateObservation_Player(period);
    }
    void setAgentObservation(float period){
        this.agentsObservation = new StateObservation_Strategies(period);
    }

    //Game Setting
    void restart() {
        pacman.resetPosition(14, 23, Left);
        ghosts[BLINKY].resetPosition(14, 11, Left);
        ghosts[PINKY].resetPosition(14, 14, Down);
        ghosts[INKY].resetPosition(13, 14, Up);
        ghosts[CLYDE].resetPosition(15, 14, Up);
        frightAllGhosts(false);
        
        this.timerState = TimerState.Scatter_1;
        frightTimer = 0;
        timer = 0f;
    }

    void reset() {
        pac_lives = setup.pac_lives;
        board.reset();
        dotsLeft = board.dots;
        energizersLeft = board.energizers;
        
        fullGameTimer=0f;
        if(playerObservation!=null)        playerObservation.reset();
        if(agentsObservation!=null)        agentsObservation.reset();
        restart();
    }
    
    void reset(int p, int d, int e, int l) {
        reset();
        p++;
        fullGameTimer = (20.f * p) ;//- 1f;
        
        dotsLeft = board.reset(d, e);
        energizersLeft = board.energizers-e;
        
        timerState = TimerState.stateAt(fullGameTimer, lvl);
        for(int g=0; g<NUM_GHOSTS; g++)
            ghosts[g].delayedStart(fullGameTimer);
        
        pac_lives = l+1;
    }
    void reset(int p, int c, int l) {
        reset();
        p++;
        fullGameTimer = (20.f * p) ;//- 1f;
        
        int e = Utils.random(0, 4);
        dotsLeft = board.reset(c, e);
        energizersLeft = board.energizers-e;
        
        timerState = TimerState.stateAt(fullGameTimer, lvl);
        for(int g=0; g<NUM_GHOSTS; g++)
            ghosts[g].delayedStart(fullGameTimer);
        
        pac_lives = l+1;
    }
    void reset(int p, int c) {
        reset();
        p++;
        fullGameTimer = (20.f * p) ;//- 1f;
        
        int e = Utils.random(0, 4);
        dotsLeft = board.reset(c, e);
        energizersLeft = board.energizers-e;
        
        timerState = TimerState.stateAt(fullGameTimer, lvl);
        for(int g=0; g<NUM_GHOSTS; g++)
            ghosts[g].delayedStart(fullGameTimer);
        
        pac_lives = Utils.random(1, 3);
    }

    void createAgents() {
        ghosts = new Ghost[NUM_GHOSTS];
            ghosts[BLINKY] = new Ghost("Blinky", this, 14, 11, lvl.ACTIVATE_BLINKY, Left, Color.RED);
            ghosts[BLINKY].setTileGetters(setup.blinky_S, setup.blinky_C);
            ghosts[BLINKY].setCruiseElroy( true );
            
            ghosts[PINKY] = new Ghost("Pinky", this, 14, 14, lvl.ACTIVATE_PINKY, Down, Color.PINK);
            ghosts[PINKY].setTileGetters(setup.pinky_S, setup.pinky_C);
            
            ghosts[INKY] = new Ghost("Inky", this, 13, 14, lvl.ACTIVATE_INKY, Up, Color.CYAN);
            ghosts[INKY].setTileGetters(setup.inky_S, setup.inky_C);
            
            ghosts[CLYDE] = new Ghost("Clyde", this, 15, 14, lvl.ACTIVATE_CLYDE, Up, new Color(255,126,0)); //, Color.ORANGE
            ghosts[CLYDE].setTileGetters(setup.clyde_S, setup.clyde_C);
            
        pacman = new PacMan_Bot("PacMan", this, 14, 24, Left, 4f, 2f, 
            new PacBot_WanderDirection_PelletBFS(this),
                new PacBot_EscapeDirection_BFS_n(this, 4));
        //pacman = new PacMan_Player("PacMan", this, 14, 23, Left);
    }

    void setPacMan (PacMan pac){
        this.pacman = pac;
    }
    //Game Updates
    /**Returns <code>false</code> if Game is Over
     */
    GameState update(float deltaTime) {   
        fullGameTimer+=deltaTime;
        
        if(playerObservation!=null)        playerObservation.log(this, deltaTime);
        if(agentsObservation!=null)        agentsObservation.log(this, deltaTime);
        
        updateState(deltaTime);
        
        GameState gs;
        //Update Ghosts
        for(int g=0; g<NUM_GHOSTS; g++){
            ghosts[g].update(deltaTime);
            
            gs = consequences(ghosts[g]);
            if(gs.endGame()) return gs;
        }
        //Update PacMan
        pacman.update(deltaTime);
        gs=consequences(pacman);
        //if(gameOver()) return false;
        return gs;
    }
    
    public void updateState(float deltaTime){
        //Update State
        if( !frightened() ) {
            if (!timerState.isLast() ){
                timer+=deltaTime;
                if ( timer >= timerState.timerStateDuration(lvl) ){
                    timer=0f;
                    force_180turn();
                    timerState=timerState.next();
                }
            }//Pointless to update otherwise
        }
        else{
            frightTimer -= deltaTime;
            if(frightTimer<=0){
                frightTimer=0f;
                frightAllGhosts(false);
            }
        }
    }

    public StateObservation_Player.PlayerState observeState_Player() {
        StateObservation_Player.PlayerState state = null;
        if ( playerObservation.shouldObserve() ){
                if(_Log.LOG_ACTIVE) _Log.w("State Observation", "fullGameTimer: "+fullGameTimer
                        + "\n"+playerObservation.toString());
            //..
            state = playerObservation.getState();
            playerObservation.reset();
        }
        return state;
    }
    public StateObservation_Strategies.StrategyState observeState_Strategies() {
        StateObservation_Strategies.StrategyState state = null;
        if ( agentsObservation.shouldObserve() ){
                if(_Log.LOG_ACTIVE) _Log.w("State Observation", "fullGameTimer: "+fullGameTimer
                        + "\n"+agentsObservation.toString());
            //..
            state = agentsObservation.getState();
            agentsObservation.reset();
        }
        return state;
    }
    
    public boolean shouldObserve(){
        return playerObservation.shouldObserve();
    }

    //Functionalities
    public void force_180turn(){
        if(setup.force180Turn_Constraint.constraintActive())
            for(int i=0; i<NUM_GHOSTS; i++)
                if( ghosts[i].state==GhostPersonalState.Out )
                    ghosts[i].turn180();
    }

    public GameState consequences(Ghost ghost) {
        if (ghost.coord_X() == pacman_coord_X() &&
                ghost.coord_Y() == pacman_coord_Y() ){
            if( ghost.isFrightened )
                eatGhost(ghost);
            else if(ghost.state!=Eaten)
                return eatPacMan();
        }
        if ( ghost.state==Exiting &&
                board.elementIn(ghost.coord_X(), ghost.coord_Y())==Collectible.OutDoor && ghost.canTurn90(ghost.dir)){
            ghost.state=Out;
            ghost.hasToChooseDir=true;
            //ghost.dir = Left; //This is to optimize the decision process; I could otherwise put a clause in the update dir process
        }
        else if ( ghost.state==Eaten &&
                board.elementIn(ghost.coord_X(), ghost.coord_Y())==Collectible.House ){
            ghost.state=Exiting;
            ghost.isFrightened = false;
        }
        return GameState.Play;
    }
    private GameState consequences(PacMan pacman) {
        Ghost ghost;
        for(int g=0; g<ghosts.length; g++){
            ghost = ghosts[g];
            if (ghost.coord_X() == pacman.coord_X() &&
                ghost.coord_Y() == pacman.coord_Y() ){
                if( ghost.isFrightened )
                    eatGhost(ghost);
                else if(ghost.state!=Eaten)
                    return eatPacMan();
            }
        }
        Vector pacTile = pacman.tile();
        switch (board.elementIn(pacTile)){
            case Energizer:
                frightTimer = lvl.get_Fright_Time()*1f;
                frightAllGhosts( true );
                force_180turn();
                energizersLeft--;
                //No Break; !!
            case Dot:
                dotsLeft--;
                board.eatPellet(pacman.coord_X(), pacman.coord_Y());
                pacmanCollectedADot=true;
                if(dotsLeft==0) return GameState.Win;
                break;
            case Fruit:
                break;
            default:
                if(!previousPacTile.equals(pacTile)){
                    //_Log.a("Pac changed position: prev "+previousPacTile+"\t curr "+pacTile+"\t \t pacCollected? "+pacmanCollectedADot);
                    pacmanCollectedADot=false;
                }
        }
        previousPacTile.reset(pacTile);
        return GameState.Play;
    }

    private void eatGhost(Ghost ghost) {
        if( ghost.state!=Eaten ){
            ghost.turn180();
            ghost.state = Eaten;
        }
    }

    private GameState eatPacMan() {
        this.pac_lives--;
        return pac_lives>0 ? GameState.LifeLost : GameState.GameOver;
    }
    
    //State of the game
    boolean frightened() {
        return frightTimer>0f;
    }

    private void frightAllGhosts( boolean setTo ) {
        frightColor = Color.BLUE;
        blinksLeft = 2*fright_blinks_number;
        for (int g=0; g<ghosts.length; g++)
            ghosts[g].isFrightened=setTo;
    }
    
    int getCruiseElroyLevel() {
        if ( dotsLeft <= lvl.get_Elroy_1_Activation() ){
            if ( dotsLeft <= lvl.get_Elroy_2_Activation() )
                return 2;
            return 1;
        }
        return 0;
    }

    boolean cruiseElroyChase() {
        return getCruiseElroyLevel() >= lvl.get_Elroy_AlwaysChase() ;
    }
    
    boolean eloryChasesOnScatter(){
        return setup.elroyChasesOnScatter_Constraint.constraintActive();
    }
    
    //Agents utilities
    public int pacman_coord_X(){
        return pacman.coord_X();
    }
    public int pacman_coord_Y(){
        return pacman.coord_Y();
    }

    public Ghost InkyReferenceGhost() {
        return ghosts[ setup.inkyReferenceGhost_Getter.getInkyReferenceGhost(this) ];
    }
    
    //Blinking
    private final int fright_blinks_number = 4;
    private int blinksLeft = 2*fright_blinks_number;
    private Color frightColor=Color.BLUE;
    private final float start_blinking_ratio = 0.25f; //starts blinking on the last 25% of fright duration
    
    public Color frightenedColor() {
        float startBlinking = lvl.get_Fright_Time()*start_blinking_ratio;
        if(frightTimer <= startBlinking ){
            if(frightTimer <= startBlinking * (blinksLeft/(2f*fright_blinks_number)) ){
                frightColor = (frightColor==Color.BLUE) ? Color.WHITE : Color.BLUE; 
                blinksLeft--;
            }
            //return frightColor;
        }
        return frightColor;
    }
    public Color eatenColor() {
        return Color.WHITE;
    }

    float agentSpeed(Ghost ghost) {
        int x=ghost.coord_X(), y=ghost.coord_Y();
        
        if( ghost.state == Eaten )
            return SPEED * 1.5f;
        
        switch(board.elementIn(x, y)){
            case House:
            case Door:
            case Tunnel:
                //if(_Log.LOG_ACTIVE) _Log.v("Ghost Speed", "GHOST_TUNNEL_COEFF"+getSpeed(LevelSpecifics.GHOST_TUNNEL_COEFF));
                //TunnelSpeed = GhostSpeed * GHOST_TUNNEL_COEFF
                if(_Log.LOG_ACTIVE) _Log.d(ghost+" Speed", lvl.get_Ghost_Tunnel_Speed()+" tunnel");
                return SPEED * lvl.get_Ghost_Tunnel_Speed();
            default:
                if( ghost.isFrightened ){
                    if(_Log.LOG_ACTIVE) _Log.d(ghost+" Speed", lvl.get_Ghost_Fright_Speed()+" fright");
                    return SPEED * lvl.get_Ghost_Fright_Speed();
                }
                else if (ghost.isCruiseElroy) {
                    switch (getCruiseElroyLevel()){
                        case 1:     return SPEED * lvl.get_Elroy_1_Speed();
                        case 2:     return SPEED * lvl.get_Elroy_2_Speed();
                        default:    return SPEED * lvl.get_Ghost_Speed();
                    }
                }
                else{
                    if(_Log.LOG_ACTIVE) _Log.d(ghost+" Speed", lvl.get_Ghost_Speed()+" //"+board.elementIn(x, y));
                    return SPEED * lvl.get_Ghost_Speed();
                }
        }
    }
    float agentSpeed(PacMan pacman) {
        int x=pacman_coord_X(), y=pacman_coord_Y();
        if( frightened() ){
            if(pacmanCollectedADot){
                    //_Log.a("PacSpeed", "Dot Fright in "+pacman.tile());
                    return SPEED * lvl.get_PacMan_Fright_Dot_Speed();
            }
            else{
               // _Log.a("PacSpeed", "Empty Firght in "+pacman.tile());
                return SPEED * lvl.get_PacMan_Fright_Speed();
            }
        }
        else{
            if(pacmanCollectedADot){
                    if(_Log.LOG_ACTIVE) _Log.d("PacSpeed", lvl.get_PacMan_Dot_Speed()+" dot speed");
                    return SPEED * lvl.get_PacMan_Dot_Speed();
            }
            else{
                //_Log.a("PacSpeed", "Empty in "+pacman.tile());
                //_Lov.v("PacMan Speed","PACMAN_SPEED"+getSpeed(LevelSpecifics.PACMAN_SPEED));
                if(_Log.LOG_ACTIVE) _Log.d("Pacman Speed", lvl.get_PacMan_Speed()+"");
                return SPEED * lvl.get_PacMan_Speed();
            }
        }
    }
    
    public boolean canGo(Agent agent, Direction dir){
        if(agent instanceof Ghost)
            return canGo( (Ghost)agent, dir);
        else
            return canGo( (PacMan)agent, dir);
    }
    public boolean canGo(Ghost ghost, Direction dir){
        Path path = board.pathIn( ghost.coord_X(), ghost.coord_Y() );
        if(_Log.LOG_ACTIVE) _Log.i("Ghost can go?", "\tcan "+ghost+" go "+dir+"\tdir: "+ghost.dir+"\tlastDir: "+ghost.lastDir);
        if( ghost.state!=GhostPersonalState.Exiting && 
                (ghost.dir.opposite()==dir || ghost.lastDir.opposite()==dir)
                && setup.turn180_Constraint.constraintActive())
            return false;
        
        if( path==Path.Horizontal_Up_Exc && !ghost.isFrightened && dir==Up && setup.upTurn_Constraint.constraintActive() )
            return false;
        
        /*if(dir==Down && board.elementIn( ghost.coord_X(), ghost.coord_Y()+1)==Door )
            return ghost.state.canGoPastDoor();*/
        
        //any other case
        boolean isDoor = board.elementIn( ghost.coord_X()+dir.x, ghost.coord_Y()+dir.y)==Door;
        return path.canGo(dir) && 
                ( !isDoor || ghost.state.canGoPastDoor() ); //Or trough Door
    }
    public boolean canGo(PacMan pacman, Direction dir){
        Path path = board.pathIn( pacman.coord_X(), pacman.coord_Y() );
        boolean isDoor = board.elementIn( pacman.coord_X()+dir.x, pacman.coord_Y()+dir.y)==Door;
        return path.canGo(dir) && !isDoor;
    }
    boolean couldPacGo(int coordX, int coordY, Direction dir) {
        Path path = board.pathIn( coordX, coordY );
        boolean isDoor = board.elementIn(coordX+dir.x, coordY+dir.y)==Door;
        return path.canGo(dir) && !isDoor;
    }
    public boolean couldGhostGo(Ghost ghost, int coordX, int coordY, Direction dir){
        Path path = board.pathIn( coordX, coordY );
        return path.canGo(dir);
    }
    
    public float distance(int x1, int y1, int x2, int y2){
        return setup.distance.eval(x1, y1, x2, y2);
    }

    /**Returns he nearest ghost to agent*/
    Ghost nearestGhost(Agent agent) {
        float min=Float.MAX_VALUE, curr;
        Ghost nearest=null;
        for(int g=0; g<NUM_GHOSTS; g++){
            curr = Utils.euclidean_dist2( agent, ghosts[g] );
            if ( curr<min ){
                min=curr;
                nearest = ghosts[g];
            }
        }
        return nearest;
    }
    Ghost nearestAliveGhost(Agent agent) {
        float min=Float.MAX_VALUE, curr;
        Ghost nearest=null, ghost;
        for(int g=0; g<NUM_GHOSTS; g++){
            ghost = ghosts[g];
            if( ghost.isNotAThreat() )
                continue;
            curr = Utils.euclidean_dist2( agent, ghost );
            //_Log.a("Nearest Alive", ghost+"("+ghost.coord_X()+" "+ghost.coord_Y()+") from Pac("+agent.coord_X()+" "+agent.coord_Y()+")="+curr);
            if ( curr<min ){
                min=curr;
                nearest = ghost;
            }
        }
        return nearest;
    }

    void signKeyListener(PacMan_Player pacman_player) {
        keyListener=pacman_player;
    }
    void notifyKeyTiped(KeyEvent ke) {
        if(keyListener!=null)
            keyListener.keyTyped(ke);
    }
    
    //Simulation
    public void setSetup(Setup builder){
        this.setup = builder;
        this.lvl = builder.lvl;
        ghosts[BLINKY].setTileGetters(setup.blinky_S, setup.blinky_C);
        ghosts[PINKY].setTileGetters(setup.pinky_S, setup.pinky_C);
        ghosts[INKY].setTileGetters(setup.inky_S, setup.inky_C);
        ghosts[CLYDE].setTileGetters(setup.clyde_S, setup.clyde_C);
    }
    public Setup getSetup(){
        return setup;
    }
    
    public void setGhost(int gId, Ghost ghost){
        ghosts[gId] = ghost;
    }
    
    public boolean firstLife(){
        return pac_lives == setup.pac_lives;
    }
}