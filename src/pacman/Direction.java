package pacman;

/**
 *
 * @author Falie
 */
public enum Direction {
    Up(0, -1), Left(-1, 0), Down(0, 1), Right(1, 0);
    
    final int x, y;
    private Direction opposite=null;

    Direction(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    Direction opposite(){
        if(opposite==null){
            switch(this){
                case Up:
                    opposite=Down;
                    break;
                case Left:
                    opposite=Right;
                    break;
                case Down:
                    opposite=Up;
                    break;
                case Right:
                    opposite=Left;
                    break;
            }
        }
        return opposite;
    }

    public boolean isPerpendicularTo(Direction dir) {
        return this!=dir && this.opposite()!=dir;
    }
}
