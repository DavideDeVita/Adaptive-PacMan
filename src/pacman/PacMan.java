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

    public PacMan(String name, GameLogic logic, int startX, int startY, Direction startDir) {
        super(name, logic, startX, startY, startDir);
    }

    @Override
    public void updatePosition(float deltaSeconds){
        int undirectedMovement = (int)( (deltaSeconds+deltaSecondsCarry) * logic.agentSpeed(this));
        if(undirectedMovement==0){
            deltaSecondsCarry+=deltaSeconds;
            return;
        }
        deltaSecondsCarry=0;
        
        int nextX = (x + dir.x * undirectedMovement + board.m_width)%board.m_width,
                nextY = (y + dir.y * undirectedMovement + board.m_height)%board.m_height;
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
