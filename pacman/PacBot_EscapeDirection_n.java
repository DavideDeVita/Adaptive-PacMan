package pacman;

/**
 *
 * @author Falie
 */
public abstract class PacBot_EscapeDirection_n extends PacBot_EscapeDirection_GhostDist{
    private final int stepsAhead;
    
    public PacBot_EscapeDirection_n(GameLogic logic, int stepsAhead) {
        super(logic);
        this.stepsAhead=stepsAhead;
    }
    
    /**This variant, considers all the possible tiles reacheable in n tiles from the current
    * to do this, for each Direction, the score is the avg of all the tiles reacheble in n steps.
    * I am using the wavg giving more weight to closer tiles
    * The max of all Direction is then choosed
    */
    @Override
    public Direction getEscapeDirection(PacMan_Bot pac) {
        int i=0;
        float avg_dists[]= new float[4];
        
        for(Direction d : Direction.values()){
            avg_dists[i] = exploreDirection(pac.coord_X(), pac.coord_Y(), d);
            i++;
        }
        
        int argmax = Utils.argmax(avg_dists);
        if(_Log.LOG_ACTIVE) _Log.d("PacBot Update", this+" min_Dists "+avg_dists[0]+"\t\t"+avg_dists[1]+"\t\t"+avg_dists[2]+"\t\t"+avg_dists[3]+" \t\t.Argmax "+argmax);
        if(argmax!=-1){
            if(_Log.LOG_ACTIVE) _Log.d("PacBot Update", "\tsafest dir was "+Direction.values()[ argmax ]);
            return Direction.values()[ argmax ];
        }
        else{
            _Log.a("PacBot Update", "\tsafest.. -1: "+pac.dir);
        _Log.a("PacBot Update", this+" min_Dists "+avg_dists[0]+"\t\t"+avg_dists[1]+"\t\t"+avg_dists[2]+"\t\t"+avg_dists[3]+" \t\t.Argmax "+argmax);
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
            if(_Log.LOG_ACTIVE) _Log.i("Explore Direction", this+" "+coordX+" "+coordY+" "+dir+" "+(stepsAhead-1)+" steps left = "+cumulativeScore+"/"+count);
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
                if(!ghost.isNotAThreat()){
                    curr = this.computeDistance(coordX, coordY, ghost, dir);
                    if(_Log.LOG_ACTIVE) _Log.i("_Explore", ghost+" is a Threat. Dists "+curr+" (min was "+min+")");
                    if(curr < min)
                        min = curr;
                }
            }
            
            cumulativeScore+=(min*(1+stepsLeft));
            count+=stepsLeft+1;
            if(stepsLeft>0){
                for(Direction d : Direction.values())
                    if( d != dir.opposite())
                        _exploreDirection( board.xFix(coordX+dir.x), board.yFix(coordY+dir.y), d, stepsLeft-1);
            }
        }
    }
}

abstract class PacBot_EscapeDirection_nStepsAhead_BACKUP extends PacBot_EscapeDirection_GhostDist{
    private final int stepsAhead;
    
    public PacBot_EscapeDirection_nStepsAhead_BACKUP(GameLogic logic, int stepsAhead) {
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
        float avg_dists[]= new float[4];
        
        for(Direction d : Direction.values()){
            avg_dists[i] = exploreDirection(pac.coord_X(), pac.coord_Y(), d);
            i++;
        }
        
        int argmax = Utils.argmax(avg_dists);
        if(_Log.LOG_ACTIVE) _Log.d("PacBot Update", this+" min_Dists "+avg_dists[0]+"\t\t"+avg_dists[1]+"\t\t"+avg_dists[2]+"\t\t"+avg_dists[3]+" \t\t.Argmax "+argmax);
        if(argmax!=-1){
            if(_Log.LOG_ACTIVE) _Log.d("PacBot Update", "\tsafest Euc2 dir was "+Direction.values()[ argmax ]);
            return Direction.values()[ argmax ];
        }
        else{
            if(_Log.LOG_ACTIVE) _Log.d("PacBot Update", "\tsafest Euc2.. -1: "+pac.dir);
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
                if(ghost.isNotAThreat())
                    continue;
                curr = this.computeDistance(coordX, coordY, ghost, dir);
                if(curr < min)
                    min = curr;
            }
            
            cumulativeScore+=min;
            count++;
            if(stepsLeft>0){
                for(Direction d : Direction.values())
                    if( d != dir.opposite())
                        _exploreDirection( board.xFix(coordX+dir.x), board.yFix(coordY+dir.y), d, stepsLeft-1);
            }
        }
    }
}





/*Maximizes the minimal Eculidean^2 distance*/
class PacBot_EscapeDirection_Euclidean2_n extends PacBot_EscapeDirection_n{

    public PacBot_EscapeDirection_Euclidean2_n(GameLogic logic, int stepsAhead) {
        super(logic, stepsAhead);
    }

    @Override
    protected float computeDistance(int coordX, int coordY, Ghost ghost, Direction d) {
        return Utils.euclidean_dist2(coordX+d.x, coordY+d.y, ghost.coord_X(), ghost.coord_Y());
    }
}

/*Maximizes the minimal Eculidean^2 distance*/
class PacBot_EscapeDirection_nStepsAhead_ToroEuclidean2 extends PacBot_EscapeDirection_n{

    public PacBot_EscapeDirection_nStepsAhead_ToroEuclidean2(GameLogic logic, int stepsAhead) {
        super(logic, stepsAhead);
    }

    @Override
    protected float computeDistance(int coordX, int coordY, Ghost ghost, Direction d) {
        return Utils.euclidean_dist2(board.xFix(coordX+d.x), board.yFix(coordY+d.y), ghost.coord_X(), ghost.coord_Y());
    }
}


/**Considers the product of the x and y differences (plus 1 to avoid 0) as distance from ghost.
 * It should improve the probability to take a turn when chased*/
class PacBot_EscapeDirection_ManhattanProduct_n extends PacBot_EscapeDirection_n{

    public PacBot_EscapeDirection_ManhattanProduct_n(GameLogic logic, int stepsAhead) {
        super(logic, stepsAhead);
    }

    @Override
    protected float computeDistance(int coordX, int coordY, Ghost ghost, Direction d) {
        return ( Utils.difference(coordX+d.x, ghost.coord_X()) + 1 ) * 
                            ( Utils.difference(coordY+d.y, ghost.coord_Y()) + 1 );
    }
}

class PacBot_EscapeDirectionManhattanProduct_plus_Euclidean2_n extends PacBot_EscapeDirection_n{

    public PacBot_EscapeDirectionManhattanProduct_plus_Euclidean2_n(GameLogic logic, int stepsAhead) {
        super(logic, stepsAhead);
    }

    @Override
    protected float computeDistance(int coordX, int coordY, Ghost ghost, Direction d) {
        return ( Utils.difference(coordX+d.x, ghost.coord_X()) + 1 ) * 
                            ( Utils.difference(coordY+d.y, ghost.coord_Y()) + 1 )+
                Utils.euclidean_dist2(coordX+d.x, coordY+d.y, ghost.coord_X(), ghost.coord_Y());
    }
}

class PacBot_EscapeDirectionManhattanProduct_plus_ToroEuclidean2_n extends PacBot_EscapeDirection_n{

    public PacBot_EscapeDirectionManhattanProduct_plus_ToroEuclidean2_n(GameLogic logic, int stepsAhead) {
        super(logic, stepsAhead);
    }

    @Override
    protected float computeDistance(int coordX, int coordY, Ghost ghost, Direction d) {
        return ( Utils.difference(coordX+d.x, ghost.coord_X()) + 1 ) * 
                            ( Utils.difference(coordY+d.y, ghost.coord_Y()) + 1 )
                + Utils.euclidean_dist2(board.xFix(coordX+d.x), board.yFix(coordY+d.y), ghost.coord_X(), ghost.coord_Y());
    }
}