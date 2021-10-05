package pacman;

import static java.lang.Integer.max;
import static java.lang.Integer.min;
import static pacman.Collectible.Door;
import static pacman.Collectible.Wall;

/**
 *
 * @author Falie
 */
public abstract class PacMan extends Agent{
    protected Direction tryDir;

    public PacMan(String name, GameLogic logic, int startX, int startY, Direction startDir) {
        super(name, logic, startX, startY, startDir);
        tryDir=startDir;
    }

    @Override
    void updateDirection() {
        //_Log.a("PacBot", "tryD "+tryDir+". dir "+dir+".");
        if(tryDir!=dir && logic.canGo(this, tryDir) ){
            if(!tryDir.isPerpendicularTo(dir) || canTurn90(tryDir) )
                dir=tryDir;
        }
    }

    @Override
    public void updatePosition(float deltaSeconds){
        int undirectedMovement = (int)( (deltaSeconds+deltaSecondsCarry) * logic.agentSpeed(this));
        //_Log.a("Pac Move", undirectedMovement+" steps");
        if(undirectedMovement<2){
            deltaSecondsCarry+=deltaSeconds;
            return;
        }
        deltaSecondsCarry=0;
        
        int nextX = board.capMovement_X(this, x + dir.x * undirectedMovement ),
                nextY = board.capMovement_Y(this, y + dir.y * undirectedMovement);
        int coord_x = coord_X(), 
                coord_y = coord_Y();
        int halfTile_X = board.coord_to_logicalHalfTile_X(coord_x),//.halfTile_X(nextX),
                halfTile_Y = board.coord_to_logicalHalfTile_Y(coord_y);//.halfTile_Y(nextY);
        
        switch(dir){
            case Up:
                if( board.elementIn(coord_x, coord_y-1)==Wall )
                    nextY = max(nextY, halfTile_Y);
                nextX=halfTile_X;
                break;
            case Left:
                if( board.elementIn( coord_x-1, coord_y)==Wall )
                    nextX = max(nextX, halfTile_X);
                nextY=halfTile_Y;
                break;
            case Down:
                if( board.elementIn(coord_x, coord_y+1)==Wall || board.elementIn(coord_x, coord_y+1)==Door )
                    nextY = min(nextY, halfTile_Y);
                nextX=halfTile_X;
                break;
            case Right:
                if( board.elementIn(coord_x+1, coord_y)==Wall )
                    nextX = min(nextX, halfTile_X);
                nextY=halfTile_Y;
                break;
        }
        this.x=nextX;
        this.y=nextY;
    }
}
