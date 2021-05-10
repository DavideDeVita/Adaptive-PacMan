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
            if(logic.canGo(pac, d)){
                min_dists[i]=Float.MAX_VALUE;
                for(int g=0; g<logic.ghosts.length; g++){
                    ghost = logic.ghosts[g];
                    curr = this.computeDistance(pac, ghost, d);
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
        if(argmax!=-1){
            System.out.println("\tsafest Euc2 dir was "+Direction.values()[ argmax ]);
            return Direction.values()[ argmax ];
        }
        else{
            System.out.println("\tsafest Euc2.. -1: "+pac.dir);
            return pac.dir;
        }
    }
    
    public abstract float computeDistance(PacMan pac, Agent agent, Direction d);
}

/*Maximizes the minimal Eculidean^2 distance*/
class PacBot_EscapeDirection_Euclidean2 extends PacBot_EscapeDirection_Getter{

    public PacBot_EscapeDirection_Euclidean2(GameLogic logic) {
        super(logic);
    }

    @Override
    public float computeDistance(PacMan pac, Agent agent, Direction d) {
        return Utils.euclidean_dist2(pac.coord_X()+d.x, pac.coord_Y()+d.y, agent.coord_X(), agent.coord_Y());
    }
}

/**Considers the product of the x and y differences (plus 1 to avoid 0) as distance from ghost.
 * It should improve the probability to take a turn when chased*/
class PacBot_EscapeDirection_MaximizeDiagonal extends PacBot_EscapeDirection_Getter{

    public PacBot_EscapeDirection_MaximizeDiagonal(GameLogic logic) {
        super(logic);
    }

    @Override
    public float computeDistance(PacMan pac, Agent agent, Direction d) {
        return ( Utils.difference(pac.coord_X()+d.x, agent.coord_X()) + 1 ) * 
                            ( Utils.difference(pac.coord_Y()+d.y, agent.coord_Y()) + 1 );
    }
}

/*Miximizes the minimal Eculidean^2 distance*/
class PacBot_EscapeDirection_MaxDiag_plus_Euclidean2 extends PacBot_EscapeDirection_Getter{

    public PacBot_EscapeDirection_MaxDiag_plus_Euclidean2(GameLogic logic) {
        super(logic);
    }

    @Override
    public float computeDistance(PacMan pac, Agent agent, Direction d) {
        return ( Utils.difference(pac.coord_X()+d.x, agent.coord_X()) + 1 ) * 
                            ( Utils.difference(pac.coord_Y()+d.y, agent.coord_Y()) + 1 )+
                Utils.euclidean_dist2(pac.coord_X()+d.x, pac.coord_Y()+d.y, agent.coord_X(), agent.coord_Y());
    }
}