package pacman;

/**
 *
 * @author Falie
 */
public abstract class PacMan_Bot extends PacMan{
    protected int last_coord_X, last_coord_Y;

    public PacMan_Bot(String name, GameLogic logic, int startX, int startY, Direction startDir) {
        super(name, logic, startX, startY, startDir);
    }

    @Override
    public void updatePosition(float deltaSeconds) {
        last_coord_X = coord_X();
        last_coord_Y = coord_Y();
        super.updatePosition(deltaSeconds); 
    }
    
    protected final boolean positionChanged(){
        return last_coord_X!=coord_X() || last_coord_Y!=coord_Y();
    }
}
