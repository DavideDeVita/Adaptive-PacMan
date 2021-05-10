package pacman;

import static pacman.Collectible.Wall;
import static pacman.Direction.*;

/**
 *
 * @author Falie
 */
public abstract class Agent {
    private final String name;
    Board board;
    GameLogic logic;
    public int x, y; //These are logical dimensions. NOT COORDINATES
    public int lastCoord_X, lastCoord_Y;
    Direction dir;
    protected float deltaSecondsCarry=0f; //Should no movement occur, to avoid stall
    protected boolean walled;

    public Agent(String name, GameLogic logic, int start_coord_X, int start_coord_Y, Direction dir) {
        this.name=name;
        this.logic = logic;
        this.board = logic.board;
        this.x = board.coord_to_logicalTile_X(start_coord_X);
        this.y = board.coord_to_logicalHalfTile_Y(start_coord_Y);
        this.lastCoord_X = start_coord_X;
        this.lastCoord_Y = start_coord_Y;
        this.dir=dir;
    }

    @Override
    public String toString() {
        return name;
    }
    
    public void update(float deltaSeconds){
        updateDirection();
        updatePosition(deltaSeconds);
        checkPositionIsNotAWall();
    }
    
    public void resetPosition(int coord_x, int coord_y, Direction dir){
        //I trust that is not a wall
        this.x = board.coord_to_logicalTile_X(coord_x);
        this.y = board.coord_to_logicalHalfTile_Y(coord_y);
        this.lastCoord_X = coord_x;
        this.lastCoord_Y = coord_y;
        this.dir=dir;
    }
    
    public int coord_X(){ return board.logicToCoordinate_X(x); }
    public int coord_Y(){ return board.logicToCoordinate_Y(y); }

    abstract void updateDirection();

    abstract void updatePosition(float deltaSeconds);
    
    protected Direction cornerTurn(Path corner){
        switch (corner){
                case Corner_NW:
                    if(dir==Down)
                        return Left;
                    else
                        return Up;
                case Corner_SW:
                    if(dir==Up)
                        return Left;
                    else
                        return Down;
                case Corner_SE:
                    if(dir==Up)
                        return Right;
                    else
                        return Down;
                case Corner_NE:
                    if(dir==Down)
                        return Right;
                    else
                        return Up;
                case Corridor_V:
                case Corridor_H:
                default:
                    return dir;
            }
    }
    
    protected Direction maxEuclidDistDirection(Agent from){
        int coordX=coord_X(), coordY=coord_Y();
        float max=Float.MIN_VALUE, curr;
        Direction ret=dir;
        for(Direction d : Direction.values()){
            if(logic.canGo(this, d)){
                curr = Utils.euclidean_dist2(this.coord_X()+d.x, this.coord_Y()+d.y, from.coord_X(), from.coord_Y());
                if(curr>max){
                    max=curr;
                    ret=d;
                }
            }
        }
        return ret;
    }
    
    /**Aligns the horizontal next logical position (nextX) to allow position between tiles
    *    as in classic PacMan.
    * Aesthetic only.. unused
    */
    protected int horizontalAlignment(int coord_X, int coord_Y, int nextX, int halfTile_X, Direction dir){
        boolean tooMuchOnRight = nextX>halfTile_X,
                tooMuchOnLeft = nextX<halfTile_X,
                wallOnTheRight = board.elementIn(coord_X+1, coord_Y)==Wall,
                wallOnTheLeft = board.elementIn(coord_X-1, coord_Y)==Wall,
                nextTileHasWallOnTheRight = board.elementIn(coord_X+1, coord_Y+dir.y)==Wall,
                nextTileHasWallOnTheLeft = board.elementIn(coord_X-1, coord_Y+dir.y)==Wall;
        switch(dir){
            case Up:
            case Down:
                if( ( tooMuchOnRight && wallOnTheRight && nextTileHasWallOnTheRight ) ||
                        ( tooMuchOnLeft && wallOnTheLeft && nextTileHasWallOnTheLeft ) )
                    return halfTile_X;
            default:
                return nextX;
        }
    }

    private void checkPositionIsNotAWall(){
        if ( this.board.elementIn( coord_X(), coord_Y() )==Wall ){
            System.out.println(this+" went in wall in "+coord_X()+" "+coord_Y());
            System.out.println(this+" last coord were "+lastCoord_X+" "+lastCoord_Y+"\ndirection was "+dir);
            x = this.board.coord_to_logicalHalfTile_X(lastCoord_X);
            y = this.board.coord_to_logicalHalfTile_Y(lastCoord_Y);
            System.out.println(this+"set to logic "+x+" "+y+"");
            System.out.println(this+" set to "+coord_X()+" "+coord_Y()+"\n");
        }
        else{
            lastCoord_X = coord_X();
            lastCoord_Y = coord_Y();
            System.out.println(this+" updated Last Coord "+lastCoord_X+" "+lastCoord_Y+"\n");
        }
    }
}
