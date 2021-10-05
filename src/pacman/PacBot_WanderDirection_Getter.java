package pacman;

/**
 *
 * @author Falie
 */
public abstract class PacBot_WanderDirection_Getter {
    protected final GameLogic logic;
    protected final Board board;

    public PacBot_WanderDirection_Getter(GameLogic logic) {
        this.logic = logic;
        this.board = logic.board;
    }
    
    public abstract Direction getWanderDirection(PacMan_Bot pac);

    void reset() { }
}

class PacBot_WanderDirection_RandomTurns extends PacBot_WanderDirection_Getter{

    public PacBot_WanderDirection_RandomTurns(GameLogic logic) {
        super(logic);
    }

    @Override
    public Direction getWanderDirection(PacMan_Bot pac) {
        int coordX = pac.coord_X(),
                coordY = pac.coord_Y();
        Path path = board.pathIn(coordX, coordY);
        if( path.ways()>2 ){
            int i=0;
            boolean canGo[] = new boolean[Direction.values().length];
            for(Direction d : Direction.values()){
                //Don't go behind.. it's pointless
                if(d!=pac.dir.opposite() && logic.canGo(pac, d)){
                    //options++;
                    canGo[i]=true;
                }
                else
                    canGo[i]=false;
                i++;
            }

            return Direction.values()[ Utils.argRandom(canGo) ];
        }
        else
            return pac.cornerTurn(path);
    }
}

/**Counts the number of pellets reachable taking n steps in each direction
 * chooses with probability growing on number of pellets
*/
class PacBot_WanderDirection_PelletGatherer extends PacBot_WanderDirection_Getter{
    private final int stepsAhead;
    private int stepsAhead_curr; //increases 1 every time is a draw
    
    public PacBot_WanderDirection_PelletGatherer(GameLogic logic, int stepsAhead) {
        super(logic);
        this.stepsAhead=stepsAhead;
        this.stepsAhead_curr=stepsAhead;
    }

    @Override
    public Direction getWanderDirection(PacMan_Bot pac) {
        int coordX = pac.coord_X(),
                coordY = pac.coord_Y();
        Path path = board.pathIn(coordX, coordY);
        if( path.ways()>2 ){
            int i=0;
            int dotPerDirection[] = new int[Direction.values().length],
                    totalCountDots=0, min=Integer.MAX_VALUE;
            boolean availableDirections[] = new boolean[Direction.values().length];
            for(Direction dir : Direction.values()){
                if(logic.canGo(pac, dir)){
                    availableDirections[i]=true;
                    dotPerDirection[i] = countDots(coordX, coordY, dir, stepsAhead_curr-1);
                    if(dotPerDirection[i]<min)
                        min=dotPerDirection[i];
                    totalCountDots += dotPerDirection[i];
                }
                else{
                    dotPerDirection[i]=0;
                    availableDirections[i]=false;
                }
                i++;
            }
            if(totalCountDots==0)
                stepsAhead_curr++;
            else
                stepsAhead_curr=stepsAhead;

            int rescale = (min==0) ? 1 : min;
            //unavailable Directions must not be considered by softmax
            int available = Utils.count(availableDirections);
            if(available==0)
                _Log.a("PacBot Pellet Gatherer. 0 available directions in "+coordX+" "+coordY);
            int dotPerDirection_trimmed[] = new int[ Utils.count(availableDirections) ];
            int trim_i=0;
            for (i=0; i<dotPerDirection.length; i++){
                if(availableDirections[i]){
                    if(Direction.values()[i]!=pac.dir.opposite())
                        dotPerDirection_trimmed[trim_i++]=dotPerDirection[i]/rescale;//to avoid overflow
                    else
                        dotPerDirection_trimmed[trim_i++]=dotPerDirection[i]/(2*rescale);
                }
            }

            try{
                trim_i = Utils.argRandomFromSoftmax(dotPerDirection_trimmed);
            }
            catch (IllegalStateException ise){
                String exc = "";
                for (int j=0; j<dotPerDirection_trimmed.length; j++)
                    exc+=dotPerDirection_trimmed[j]+" ";
                _Log.a("Wander Pellet Gather", ise.getMessage()+exc);
                return pac.dir;
            }
                
            for (i=0; i<dotPerDirection.length; i++){
                if(!availableDirections[i])
                    continue;
                trim_i--;
                if(trim_i<0){
                    if(_Log.LOG_ACTIVE) _Log.d("PacBot Update", "Returning dir "+i);
                    return Direction.values()[i];
                }
            }
        }
        else
            return pac.cornerTurn(path);
        
        return pac.dir; //Never
    }

    private int countDots(int coordX, int coordY, Direction dir, int stepsLeft) {
        int subCount=0, 
                newX=board.xFix(coordX+dir.x), 
                newY=board.yFix(coordY+dir.y);
        //if(_Log.LOG_ACTIVE) _Log.d("PacBot Update", "subCountDots init: "+newX+" "+newY+" "+dir+" "+stepsLeft+"\t: "+board.elementIn(newX, newY));
        if(logic.couldPacGo(coordX, coordY, dir)){
            if (board.elementIn(newX, newY) == Collectible.Dot ||
                    board.elementIn(newX, newY) == Collectible.Energizer )
                subCount++;
            
            if(stepsLeft>0){
                for(Direction d : Direction.values())
                    if( d!= dir.opposite())
                        subCount+=countDots(newX, newY, d, stepsLeft-1);
            }
        }
        //if(_Log.LOG_ACTIVE) _Log.d("PacBot Update", "subCountDots: "+newX+" "+newY+" "+dir+" "+stepsLeft+" = "+subCount);
        return subCount;
    }
}