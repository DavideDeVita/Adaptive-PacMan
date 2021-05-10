package pacman;

/**
 *
 * @author Falie
 */
public class PacMan_Bot extends PacMan{
    protected int last_coord_X, last_coord_Y;
    private final float escapeTriggerDist2;
    private final PacBot_WanderDirection_Getter wander;
    private final PacBot_EscapeDirection_Getter escape;

    public PacMan_Bot(String name, GameLogic logic, int startX, int startY, Direction startDir, float triggerDist, PacBot_WanderDirection_Getter wander, PacBot_EscapeDirection_Getter escape) {
        super(name, logic, startX, startY, startDir);
        this.escapeTriggerDist2 = triggerDist*triggerDist;
        this.wander=wander;
        this.escape=escape;
    }

    @Override
    public void updatePosition(float deltaSeconds) {
        last_coord_X = coord_X();
        last_coord_Y = coord_Y();
        super.updatePosition(deltaSeconds); 
    }
    
    protected final boolean positionChanged(){
        return last_coord_X!=coord_X() || last_coord_Y!=coord_Y();
    }
    
    @Override
    void updateDirection() {
        if ( Utils.euclidean_dist2(selectNearestGhost(), this) > escapeTriggerDist2){
            if ( positionChanged())
                dir = wander.getWanderDirection(this);
        }
        else{
            System.out.println(this+" is escaping ");
            dir = escape.getEscapeDirection(this);
        }
    }

    protected void escapeDirection(){
        this.safestDirection_maximizeDiagonal_plus_Eucl();
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
        System.out.println(this+" min_Dists "+min_dists[0]+"\t\t"+min_dists[1]+"\t\t"+min_dists[2]+"\t\t"+min_dists[3]+" \t\t.Argmax "+argmax);
        if(argmax!=-1)
            dir = Direction.values()[ argmax ];
        System.out.println(this+" safest dir was "+dir);
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
        System.out.println(this+" min_Dists "+min_dists[0]+" "+min_dists[1]+" "+min_dists[2]+" "+min_dists[3]+" .Argmax "+argmax);
        if(argmax!=-1)
            dir = Direction.values()[ argmax ];
        System.out.println(this+" safest dir was "+dir);
    }
    
    /**from all ghosts:
     * Considers the product of the x and y differences (plus 1 to avoid 0) as distance from ghost.
     * It should improve the probability to take a turn when chased
    */
    protected void safestDirection_maximizeDiagonal_plus_Eucl() {
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
                            ( Utils.difference(coordY+d.y, ghost.coord_Y()) + 1 ) +
                            (int)Utils.euclidean_dist(coordX+d.x, coordY+d.y, ghost.coord_X(), ghost.coord_Y());
                    if(curr<min_dists[i])
                        min_dists[i]=curr;
                }
            }
            else
                min_dists[i]=Float.MIN_VALUE;
            i++;
        }
        int argmax = Utils.argmax(min_dists);
        System.out.println(this+" min_Dists "+min_dists[0]+" "+min_dists[1]+" "+min_dists[2]+" "+min_dists[3]+" .Argmax "+argmax);
        if(argmax!=-1)
            dir = Direction.values()[ argmax ];
        System.out.println(this+" safest dir was "+dir);
    }
}
