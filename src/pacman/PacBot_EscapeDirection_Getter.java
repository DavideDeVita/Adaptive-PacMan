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
    
    public Direction getEscapeDirection(PacMan_Bot pac) {
        int i=0;
        float min_dists[]= new float[4], curr;
        Ghost ghost;
        
        for(Direction d : Direction.values()){
            min_dists[i] = compute_minGhostDistance(pac.coord_X(), pac.coord_Y(), d);
            i++;
        }
        
        int argmax = Utils.argmax(min_dists);
        System.out.println(this+" min_Dists "+min_dists[0]+"\t\t"+min_dists[1]+"\t\t"+min_dists[2]+"\t\t"+min_dists[3]+" \t\t.Argmax "+argmax);
        if(argmax!=-1){
            System.out.println("\tsafest Euc2 dir was "+Direction.values()[ argmax ]);
            return Direction.values()[ argmax ];
        }
        else{
            System.out.println("\tsafest Euc2.. -1: "+pac.dir);
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
                curr = this.computeDistance(coordX, coordY, ghost, d);
                if(curr < ret)
                    ret = curr;
            }
        }
        else
            ret = Float.MIN_VALUE;
        return ret;
    }
    
    /**Computes the distance (according to some metric) from the coordinates x,y moving in direction dir, to agent*/
    protected float computeDistance(PacMan pac, Agent agent, Direction d){
        return this.computeDistance(pac.coord_X(), pac.coord_Y(), agent, d);
    }
    protected abstract float computeDistance(int coordX, int coordY, Agent agent, Direction d);
}