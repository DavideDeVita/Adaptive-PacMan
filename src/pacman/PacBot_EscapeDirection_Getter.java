package pacman;

/**
 *
 * @author Falie
 */
public abstract class PacBot_EscapeDirection_Getter {
    protected final GameLogic logic;
    protected final Board board;

    public PacBot_EscapeDirection_Getter(GameLogic logic) {
        this.logic = logic;
        this.board = logic.board;
    }
    
    public abstract Direction getEscapeDirection(PacMan_Bot pac);

    void reset() { }
}

 abstract class PacBot_EscapeDirection_GhostDist extends PacBot_EscapeDirection_Getter{
    public PacBot_EscapeDirection_GhostDist(GameLogic logic) {
        super(logic);
    }
    
    @Override
    public Direction getEscapeDirection(PacMan_Bot pac) {
        int i=0;
        float min_dists[]= new float[4], curr;
        Ghost ghost;
        
        for(Direction d : Direction.values()){
            min_dists[i] = compute_minGhostDistance(pac.coord_X(), pac.coord_Y(), d);
            i++;
        }
        if(_Log.LOG_ACTIVE) _Log.d("PacBot Update", "minDists: "+min_dists[0]+" "+min_dists[1]+" "
                +min_dists[2]+" "+min_dists[3]);
        int argmax = Utils.argmax(min_dists);
        if(argmax!=-1){
            return Direction.values()[ argmax ];
        }
        else{
            return pac.dir;
        }
    }
    
    protected float compute_minGhostDistance(int coordX, int coordY, Direction d){
        float ret, curr;
        Ghost ghost;
        
        if(logic.couldPacGo(coordX, coordY, d)){
            ret=Float.MAX_VALUE;
            for(int g=0; g<logic.ghosts.length; g++){
                ghost = logic.ghosts[g];
                if ( ghost.isNotAThreat() )
                    continue;//Ignore non dangerous ghosts
                curr = this.computeDistance(coordX, coordY, ghost, d);
                if(curr < ret)
                    ret = curr;
            }
        }
        else
            ret = -1;
        return ret;
    }
    
    /**Computes the distance (according to some metric) from the coordinates x,y moving in direction dir, to agent*/
    protected float computeDistance(PacMan pac, Ghost ghost, Direction d){
        return this.computeDistance(pac.coord_X(), pac.coord_Y(), ghost, d);
    }
    protected abstract float computeDistance(int coordX, int coordY, Ghost ghost, Direction d);

    void reset() { }
}