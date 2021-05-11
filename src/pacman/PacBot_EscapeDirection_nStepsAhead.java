package pacman;

/**
 *
 * @author Falie
 */
public abstract class PacBot_EscapeDirection_nStepsAhead extends PacBot_EscapeDirection_Getter{
    private final int stepsAhead;
    
    public PacBot_EscapeDirection_nStepsAhead(GameLogic logic, int stepsAhead) {
        super(logic);
        this.stepsAhead=stepsAhead;
    }
    
    /**This variant, considers all the possible tiles reacheable in n tiles from the current
    * to do this, for each Direction, the score is the avg of all the tiles reacheble in n steps.
    * I could ve used the wavg giving more weight to closer tiles but did not fell to sure about it
    * The max of all Direction is then choosed
    */
    @Override
    public Direction getEscapeDirection(PacMan_Bot pac) {
        int i=0;
        float avg_dists[]= new float[4], curr;
        Ghost ghost;
        
        for(Direction d : Direction.values()){
            avg_dists[i] = exploreDirection(pac.coord_X(), pac.coord_Y(), d);
            i++;
        }
        
        int argmax = Utils.argmax(avg_dists);
        System.out.println(this+" min_Dists "+avg_dists[0]+"\t\t"+avg_dists[1]+"\t\t"+avg_dists[2]+"\t\t"+avg_dists[3]+" \t\t.Argmax "+argmax);
        if(argmax!=-1){
            System.out.println("\tsafest Euc2 dir was "+Direction.values()[ argmax ]);
            return Direction.values()[ argmax ];
        }
        else{
            System.out.println("\tsafest Euc2.. -1: "+pac.dir);
            return pac.dir;
        }
    }
    
    private int count;
    private float cumulativeScore;
    /**Returns the avg of all the reachable tiles. 
     * Uses fields count and cumulative to compute avg*/
    private float exploreDirection(int coordX, int coordY, Direction dir){        
        if(logic.couldPacGo(coordX, coordY, dir)){
            count=0;
            cumulativeScore=0f;
            _exploreDirection(coordX, coordY, dir, stepsAhead-1);
            return cumulativeScore/count;
        }
        else
            return Float.MIN_VALUE;
    }

    private void _exploreDirection(int coordX, int coordY, Direction dir, int stepsLeft) {
        if(logic.couldPacGo(coordX, coordY, dir)){
            float min = Float.MAX_VALUE, curr;
            Ghost ghost;
            
            for(int g=0; g<logic.ghosts.length; g++){
                ghost = logic.ghosts[g];
                curr = this.computeDistance(coordX, coordY, ghost, dir);
                if(curr < min)
                    min = curr;
            }
            
            cumulativeScore+=min;
            count++;
            if(stepsLeft>0){
                for(Direction d : Direction.values())
                    _exploreDirection(coordX + dir.x, coordY + dir.y, d, stepsLeft-1);
            }
        }
    }
}

/*Maximizes the minimal Eculidean^2 distance*/
class PacBot_EscapeDirection_nStepsAhead_Euclidean2 extends PacBot_EscapeDirection_nStepsAhead{

    public PacBot_EscapeDirection_nStepsAhead_Euclidean2(GameLogic logic, int stepsAhead) {
        super(logic, stepsAhead);
    }

    @Override
    public float computeDistance(int coordX, int coordY, Agent agent, Direction d) {
        return Utils.euclidean_dist2(coordX+d.x, coordY+d.y, agent.coord_X(), agent.coord_Y());
    }
}


/**Considers the product of the x and y differences (plus 1 to avoid 0) as distance from ghost.
 * It should improve the probability to take a turn when chased*/
class PacBot_EscapeDirection_nStepsAhead_MaximizeDiagonal extends PacBot_EscapeDirection_nStepsAhead{

    public PacBot_EscapeDirection_nStepsAhead_MaximizeDiagonal(GameLogic logic, int stepsAhead) {
        super(logic, stepsAhead);
    }

    @Override
    public float computeDistance(int coordX, int coordY, Agent agent, Direction d) {
        return ( Utils.difference(coordX+d.x, agent.coord_X()) + 1 ) * 
                            ( Utils.difference(coordY+d.y, agent.coord_Y()) + 1 );
    }
}


/*Miximizes the minimal Eculidean^2 distance*/
class PacBot_EscapeDirection_nStepsAhead_MaxDiag_plus_Euclidean2 extends PacBot_EscapeDirection_nStepsAhead{

    public PacBot_EscapeDirection_nStepsAhead_MaxDiag_plus_Euclidean2(GameLogic logic, int stepsAhead) {
        super(logic, stepsAhead);
    }

    @Override
    public float computeDistance(int coordX, int coordY, Agent agent, Direction d) {
        return ( Utils.difference(coordX+d.x, agent.coord_X()) + 1 ) * 
                            ( Utils.difference(coordY+d.y, agent.coord_Y()) + 1 )+
                Utils.euclidean_dist2(coordX+d.x, coordY+d.y, agent.coord_X(), agent.coord_Y());
    }
}