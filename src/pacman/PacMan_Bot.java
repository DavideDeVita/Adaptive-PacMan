package pacman;

/**
 *
 * @author Falie
 */
public class PacMan_Bot extends PacMan{
    protected int last_coord_X, last_coord_Y;
    protected boolean forceYes = false;
    private final float escapeTriggerDist2_min, escapeTriggerDist2_max;
    private float escapeTriggerDist2;
    private final PacBot_WanderDirection_Getter wander;
    private final PacBot_EscapeDirection_Getter escape;
    public State lastState;

    public static enum State {Wander, Escape};
            
    public PacMan_Bot(String name, GameLogic logic, int startX, int startY, Direction startDir, float triggerDist_min, float postAlertCaution, PacBot_WanderDirection_Getter wander, PacBot_EscapeDirection_Getter escape) {
        super(name, logic, startX, startY, startDir);
        this.escapeTriggerDist2_min = triggerDist_min*triggerDist_min;
        this.escapeTriggerDist2_max = (triggerDist_min+postAlertCaution)*(triggerDist_min+postAlertCaution);
        this.escapeTriggerDist2 = escapeTriggerDist2_min;
        this.wander=wander;
        this.escape=escape;
    }

    @Override
    public void resetPosition(int coord_x, int coord_y, Direction dir) {
        super.resetPosition(coord_x, coord_y, dir); //To change body of generated methods, choose Tools | Templates.
        forceYes=true;
        wander.reset();
        escape.reset();
    }
    
    @Override
    public void updateDirection() {
        //_Log.a("Wander:", "Update Direction from ("+coord_X()+" "+coord_Y()+" "+dir+"): "+board.pathIn(coord_X(), coord_Y()));
        Ghost nearestGh = selectNearestGhost();
        if ( nearestGh==null || Utils.euclidean_dist2(nearestGh, this) > escapeTriggerDist2){
            this.escapeTriggerDist2 = escapeTriggerDist2_min;
            //_Log.a("Wander:", "Nearest Ghost is "+nearestGh);
            if ( lastState!=State.Wander | positionChanged()){
                Direction lastDir = dir;
                tryDir = wander.getWanderDirection(this);
                //_Log.a("Wander", "retrieved direction ("+coord_X()+" "+coord_Y()+"): "+tryDir);
                if (!board.pathIn(coord_X(), coord_Y()).canGo(tryDir)){
                    //_Log.a("Wander Err", "Error Direction from ("+coord_X()+" "+coord_Y()+" "+tryDir+"): "+board.pathIn(coord_X(), coord_Y())+".\n"
                    //        + "\tReset to "+lastDir);
                    forceYes = true;
                    wander.reset();
                    tryDir = lastDir;
                }
            }
            if(_Log.LOG_ACTIVE) _Log.i("PacBot Update", "PacMan Bot: Wandering "+tryDir);
            this.lastState = State.Wander;
        }
        else{
            this.escapeTriggerDist2 = escapeTriggerDist2_max;
            tryDir = escape.getEscapeDirection(this);
            if(_Log.LOG_ACTIVE) _Log.i("PacBot Update", "PacMan Bot: is Escaping "+tryDir);
            this.lastState = State.Escape;
        }
        super.updateDirection();
    }

    @Override
    public void updatePosition(float deltaSeconds) {
        last_coord_X = coord_X();
        last_coord_Y = coord_Y();
        super.updatePosition(deltaSeconds); 
    }
    
    protected final boolean positionChanged(){
        if( forceYes ){
            forceYes=false;
            return true;
        }
        return last_coord_X!=coord_X() || last_coord_Y!=coord_Y();
    }
    
    protected final boolean _positionChanged(){
        if( forceYes ){
            //forceYes=false;
            return true;
        }
        return last_coord_X!=coord_X() || last_coord_Y!=coord_Y();
    }

    protected void escapeDirection(){
        this.safestDirection_Eucl2();
    }

    protected Ghost selectNearestGhost() {
        return logic.nearestAliveGhost(this);
    }

    /**from all ghosts:
     * Considers the Eculidean Distance^2 as distance from ghost
    */
    protected void safestDirection_Eucl2() {
        int coordX=coord_X(), coordY=coord_Y(), 
                i=0;
        float min_dists[]= new float[4], curr;
        Ghost ghost;
        
        for(Direction d : Direction.values()){
            if(logic.canGo(this, d)){
                min_dists[i]=Float.MAX_VALUE;
                for(int g=0; g<logic.ghosts.length; g++){
                    ghost = logic.ghosts[g];
                    curr = Utils.euclidean_dist2(coordX+d.x, coordY+d.y, ghost.coord_X(), ghost.coord_Y());
                    if(curr<min_dists[i])
                        min_dists[i]=curr;
                }
            }
            else
                min_dists[i]=Float.MIN_VALUE;
            i++;
        }
        int argmax = Utils.argmax(min_dists);
        if(_Log.LOG_ACTIVE) _Log.d("PacBot Update", this+" min_Dists "+min_dists[0]+"\t\t"+min_dists[1]+"\t\t"+min_dists[2]+"\t\t"+min_dists[3]+" \t\t.Argmax "+argmax);
        if(argmax!=-1)
            tryDir = Direction.values()[ argmax ];
        if(_Log.LOG_ACTIVE) _Log.i("PacBot Update", this+" safest dir was "+tryDir);
    }

    /**from all ghosts:
     * Considers the product of the x and y differences (plus 1 to avoid 0) as distance from ghost.
     * It should improve the probability to take a turn when chased
    */
    protected void safestDirection_maximizeDiagonal() {
        int coordX=coord_X(), coordY=coord_Y(), 
                i=0;
        float min_dists[]= new float[4], curr;
        Ghost ghost;
        
        for(Direction d : Direction.values()){
            if(logic.canGo(this, d)){
                min_dists[i]=Float.MAX_VALUE;
                for(int g=0; g<logic.ghosts.length; g++){
                    ghost = logic.ghosts[g];
                    curr = ( Utils.difference(coordX+d.x, ghost.coord_X()) + 1 ) * 
                            ( Utils.difference(coordY+d.y, ghost.coord_Y()) + 1 );
                    if(curr<min_dists[i])
                        min_dists[i]=curr;
                }
            }
            else
                min_dists[i]=Float.MIN_VALUE;
            i++;
        }
        int argmax = Utils.argmax(min_dists);
        if(_Log.LOG_ACTIVE) _Log.d("PacBot Update", this+" min_Dists "+min_dists[0]+" "+min_dists[1]+" "+min_dists[2]+" "+min_dists[3]+" .Argmax "+argmax);
        if(argmax!=-1)
            tryDir = Direction.values()[ argmax ];
        if(_Log.LOG_ACTIVE) _Log.i("PacBot Update", this+" safest dir was "+tryDir);
    }

    @Override
    protected void onResetFromWall() {
        forceYes=true;
    }
}
