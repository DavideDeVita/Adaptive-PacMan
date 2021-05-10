package pacman;

/**
 *
 * @author Falie
 */
public class PacMan_RandomTurns extends PacMan_Bot{

    public PacMan_RandomTurns(String name, GameLogic logic, int startX, int startY, Direction startDir) {
        super(name, logic, startX, startY, startDir);
    }

    @Override
    void updateDirection() {
        int coordX=coord_X(), coordY=coord_Y();
        Path path=board.pathIn(coordX, coordY);
        if( this.positionChanged() ){
            if( path.ways()>2 ){
                int options=0, i=0;
                boolean canGo[] = new boolean[Direction.values().length];
                for(Direction d : Direction.values()){
                    if(logic.canGo(this, d)){
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
                dir = Direction.values()[i-1];
            }
            else
                dir=cornerTurn(path);
        }
    }
}

 class PacMan_RandomTurns_Escape extends PacMan_BotEscape{

    public PacMan_RandomTurns_Escape(String name, GameLogic logic, int startX, int startY, Direction startDir, float triggerDist) {
        super(name, logic, startX, startY, startDir, triggerDist);
    }

    @Override
    protected void _updateDirection()  {
        int coordX=coord_X(), coordY=coord_Y();
        Path path=board.pathIn(coordX, coordY);
        if( this.positionChanged() ){
            if( path.ways()>2 ){
                int options=0, i=0;
                boolean canGo[] = new boolean[Direction.values().length];
                for(Direction d : Direction.values()){
                    if(logic.canGo(this, d)){
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
                dir = Direction.values()[i-1];
            }
            else
                dir=cornerTurn(path);
        }
    }
}