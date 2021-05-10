package pacman;

/**
 *
 * @author Falie
 */
public class ChaseTarget_Pinky_Std extends ChaseTargetGetter{
    protected final int offset;
    
    public ChaseTarget_Pinky_Std(GameLogic logic) {
        super(logic);
        offset=4;
    }

    @Override
    public Vector getChaseTarget(Ghost thisGhost) {
        target.reset(
                logic.pacman_coord_X() + logic.pacman.dir.x*offset,
                logic.pacman_coord_Y() + + logic.pacman.dir.y*offset
        );
        //arcade 8 bit buf
        if(logic.pacman.dir == Direction.Up)
            target.x-=offset;
        return target;
    }
}
