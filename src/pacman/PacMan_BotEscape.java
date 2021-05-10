package pacman;

/**
 *
 * @author Falie
 */
public abstract class PacMan_BotEscape extends PacMan_Bot{
    private final float escapeTriggerDist2;

    public PacMan_BotEscape(String name, GameLogic logic, int startX, int startY, Direction startDir, float triggerDist) {
        super(name, logic, startX, startY, startDir);
        this.escapeTriggerDist2 = triggerDist*triggerDist;
    }

    @Override
    void updateDirection() {
        Ghost nearest = selectNearestGhost();
        if ( Utils.euclidean_dist2(nearest, this) > escapeTriggerDist2 )
            _updateDirection();
        else{
            System.out.println(this+" is escaping from "+nearest);
            escapeDirection(nearest);
        }
    }

    protected abstract void _updateDirection();

    protected void escapeDirection(Ghost nearest){
        this.safestDirection();
    }

    protected Ghost selectNearestGhost() {
        return logic.nearestAliveGhost(this);
    }

    //rom all ghosts
    protected void safestDirection() {
        int coordX=coord_X(), coordY=coord_Y(), 
                i=0;
        float min_dists[]= new float[4], curr;
        Ghost ghost;
        
        for(Direction d : Direction.values()){
            if(logic.canGo(this, d)){
                min_dists[i]=Float.MAX_VALUE;
                for(int g=0; g<logic.ghosts.length; g++){
                    ghost = logic.ghosts[g];
                    curr = Utils.euclidean_dist2(this.coord_X()+d.x, this.coord_Y()+d.y, ghost.coord_X(), ghost.coord_Y());
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
