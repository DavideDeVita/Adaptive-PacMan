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
    private final Game_PacMan pmg;
    
    //Agents
    final static int NUM_GHOSTS = 4,
            BLINKY=0, PINKY=1, INKY=2, CLYDE=3;
    public PacMan pacman;
    public Ghost[] ghosts;
    
    //Game
    private int dotsLeft;
    int pac_lives;
    private float frightTimer=0;
    private float timer=0f;
    public TimerState timerState;
    
    //Features
    final static int SPEED = 200; //units per second
    private KeyListener keyListener=null;
    
    //Custom Specific
    LevelSpecifics levelSpecifics;
    Ghost_UpTurn_Constraint g_ut_c;
    Ghost_180Turn_Constraint g_180t_c;
    Ghost_Force180Turn_Constraint g_force180t_c;
    InkyReferenceGhost_Getter Irg_g;

    GameLogic(Board board, Game_PacMan pmg, CustomSpecifics builder) {
        this.board = board;
        this.pmg=pmg;
        
        this.levelSpecifics = builder.getLevelSpecific();
        this.g_ut_c = builder.getUpTurn_Constraint();
        this.g_180t_c = builder.getTurn180_Constraint();
        this.g_force180t_c = builder.getForce180Turn_Constraint();
        this.Irg_g = builder.getInkyReferenceGhost_Getter();
        
        this.dotsLeft=board.countDots();
        this.pac_lives = builder.getPacLives();
        this.timerState = TimerState.Scatter_1;
    }

    void restart() {
        pacman.resetPosition(14, 23, Left);
        ghosts[BLINKY].resetPosition(14, 11, Left);
        ghosts[PINKY].resetPosition(14, 14, Down);
        ghosts[INKY].resetPosition(13, 14, Up);
        ghosts[CLYDE].resetPosition(15, 14, Up);
        
        this.timerState = TimerState.Scatter_1;
        frightTimer = 0;
        timer = 0f;
    }

    void createAgents() {
        ghosts = new Ghost[NUM_GHOSTS];
            ghosts[BLINKY] = new Ghost("Blinky", this, 14, 11, levelSpecifics.get_Activate_Blinky(), Left, Color.RED);
            ghosts[BLINKY].setScatterTile( CustomSpecifics.std_Blinky_Scatter(this) );
            ghosts[BLINKY].setChaseTile( CustomSpecifics.std_Blinky_Chase(this) );
            ghosts[BLINKY].setCruiseElroy( true );
            
            ghosts[PINKY] = new Ghost("Pinky", this, 14, 14, levelSpecifics.get_Activate_Pinky(), Down, Color.PINK);
            ghosts[PINKY].setScatterTile( CustomSpecifics.std_Pinky_Scatter(this) );
            ghosts[PINKY].setChaseTile( CustomSpecifics.std_Pinky_Chase(this) );
            
            ghosts[INKY] = new Ghost("Inky", this, 13, 14, levelSpecifics.get_Activate_Inky(), Up, Color.CYAN);
            ghosts[INKY].setScatterTile( CustomSpecifics.std_Inky_Scatter(this) );
            ghosts[INKY].setChaseTile( CustomSpecifics.std_Inky_Chase(this) );
            
            ghosts[CLYDE] = new Ghost("Clyde", this, 15, 14, levelSpecifics.get_Activate_Clyde(), Up, Color.ORANGE);
            ghosts[CLYDE].setScatterTile( CustomSpecifics.std_Clyde_Scatter(this) );
            ghosts[CLYDE].setChaseTile( CustomSpecifics.std_Clyde_Chase(this) );
        pacman = new PacMan_Bot("PacMan", this, 14, 23, Left, 6, 
                new PacBot_WanderDirection_RandomTurns(this), new PacBot_EscapeDirection_nStepsAhead_MaxDiag_plus_Euclidean2(this, 3));
        //pacman = new PacMan_Player("PacMan", this, 14, 23, Left);
    }

    /**Returns <code>false</code> if Game is Over
     */
    GameState update(float deltaTime) {   
        updateState(deltaTime);
        GameState gs=GameState.Play;
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
                if ( timer >= timerState.timerStateDuration(levelSpecifics) ){
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

    public void force_180turn(){
        if(g_force180t_c.constraintActive())
            for(int i=0; i<NUM_GHOSTS; i++)
                if( ghosts[i].state!=GhostPersonalState.Eaten )
                    ghosts[i].turn180();
    }

    public GameState consequences(Ghost ghost) {
        if (ghost.coord_X() == pacman_coord_X() &&
                ghost.coord_Y() == pacman_coord_Y() ){
            if( ghost.isFrightened )
                eatGhost(ghost);
            else
                return eatPacMan();
        }
        if ( ghost.state==Exiting &&
                board.elementIn(ghost.coord_X(), ghost.coord_Y())==Collectible.OutDoor ){
            ghost.state=Out;
            ghost.dir = Left; //This is to optimize the decision process; I could ptherwise put a clause in the update dir process
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
                else
                    return eatPacMan();
            }
        }
        switch (board.elementIn(pacman.coord_X(), pacman.coord_Y())){
            case Energizer:
                frightTimer = levelSpecifics.get_Fright_Time()*1f;
                frightAllGhosts( true );
                force_180turn();
            case Dot:
                dotsLeft--;
                board.eatPellet(pacman.coord_X(), pacman.coord_Y());
                if(dotsLeft==0) return GameState.Win;
                break;
            case Fruit:
                break;
        }
        return GameState.Play;
    }

    private void eatGhost(Ghost ghost) {
        ghost.turn180();
        ghost.state = Eaten;
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
        for (int g=0; g<ghosts.length; g++)
            ghosts[g].isFrightened=setTo;
    }
    
    int getCruiseElroyLevel() {
        if ( dotsLeft <= levelSpecifics.get_Elroy_1_Activation() ){
            if ( dotsLeft <= levelSpecifics.get_Elroy_2_Activation() )
                return 2;
            return 1;
        }
        return 0;
    }

    boolean cruiseElroyChase() {
        return getCruiseElroyLevel() >= levelSpecifics.get_Elroy_AlwaysChase() ;
    }
    
    //Agents utilities
    public int pacman_coord_X(){
        return pacman.coord_X();
    }
    public int pacman_coord_Y(){
        return pacman.coord_Y();
    }

    public Ghost InkyReferenceGhost() {
        return ghosts[ Irg_g.getInkyReferenceGhost(this) ];
    }
    
    public Color frightenedColor() {
        return Color.BLUE;
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
                //System.out.println("Ghost Speed: GHOST_TUNNEL_SPEED"+getSpeed(LevelSpecifics.GHOST_TUNNEL_SPEED));
                return getSpeed(LevelSpecifics.GHOST_TUNNEL_SPEED);
            default:
                if( ghost.isFrightened )
                    return getSpeed(LevelSpecifics.FRIGHT_GHOST_SPEED);
                else if (ghost.isCruiseElroy) {
                    switch (getCruiseElroyLevel()){
                        case 1:     return getSpeed(LevelSpecifics.ELROY_1_SPEED);
                        case 2:     return getSpeed(LevelSpecifics.ELROY_2_SPEED);
                        default:    return getSpeed(LevelSpecifics.GHOST_SPEED);
                    }
                }
                else
                    return getSpeed(LevelSpecifics.GHOST_SPEED);
        }
    }
    float agentSpeed(PacMan pacman) {
        int x=pacman_coord_X(), y=pacman_coord_Y();
        if( frightened() ){
            switch(board.elementIn(x, y)){
                case Dot:
                case Energizer:
                    return getSpeed(LevelSpecifics.FRIGHT_PACMAN_DOTS_SPEED);
                default:
                    return getSpeed(LevelSpecifics.FRIGHT_PACMAN_SPEED);
            }
        }
        else{
            switch(board.elementIn(x, y)){
                case Dot:
                case Energizer:
                    //System.out.println("PacMan Speed: PACMAN_DOTS_SPEED"+getSpeed(LevelSpecifics.PACMAN_DOTS_SPEED));
                    return getSpeed(LevelSpecifics.PACMAN_DOTS_SPEED);
                default:
                    //System.out.println("PacMan Speed: PACMAN_SPEED"+getSpeed(LevelSpecifics.PACMAN_SPEED));
                    return getSpeed(LevelSpecifics.PACMAN_SPEED);
            }
        }
    }
    
    int getSpeed(int specificIndex){
        return (int)( SPEED * levelSpecifics.get(specificIndex) );
    }
    
    public boolean canGo(Agent agent, Direction dir){
        if(agent instanceof Ghost)
            return canGo( (Ghost)agent, dir);
        else
            return canGo( (PacMan)agent, dir);
    }
    public boolean canGo(Ghost ghost, Direction dir){
        Path path = board.pathIn( ghost.coord_X(), ghost.coord_Y() );
        System.out.println("\tcan "+ghost+" go "+dir+"\tdir: "+ghost.dir+"\tlastDir: "+ghost.lastDir);
        if( ghost.state!=GhostPersonalState.Exiting && 
                (ghost.dir.opposite()==dir || ghost.lastDir.opposite()==dir)
                && g_180t_c.constraintActive())
            return false;
        
        if( path==Path.Horizontal_Up_Exc && !ghost.isFrightened && dir==Up && g_ut_c.constraintActive() )
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
            if( ghost.isFrightened || ghost.state==GhostPersonalState.Housed
                    || ghost.state==GhostPersonalState.Eaten )
                continue;
            
            curr = Utils.euclidean_dist2( agent, ghosts[g] );
            if ( curr<min ){
                min=curr;
                nearest = ghosts[g];
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
}