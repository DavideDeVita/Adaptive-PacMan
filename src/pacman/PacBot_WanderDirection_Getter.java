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
        if( pac.positionChanged() ){
            if( path.ways()>2 ){
                int options=0, i=0;
                boolean canGo[] = new boolean[Direction.values().length];
                for(Direction d : Direction.values()){
                    //Don't go behind.. it's pointless
                    if(d!=pac.dir.opposite() && logic.canGo(pac, d)){
                        options++;
                        canGo[i]=true;
                    }
                    else
                        canGo[i]=false;
                    i++;
                }
                int r=Utils.random(0, options-1);
                i=0;
                while(r>=0){
                    if(canGo[i])
                        r--;
                    i++;
                }
                return Direction.values()[i-1];
            }
            else
                return pac.cornerTurn(path);
        }
        return pac.dir;//Extremis case
    }
}