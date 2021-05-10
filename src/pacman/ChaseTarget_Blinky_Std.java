package pacman;

/**
 *
 * @author Falie
 */
public 

class ChaseTarget_Blinky_Std extends ChaseTargetGetter{
    public ChaseTarget_Blinky_Std(GameLogic logic) {
        super(logic);
    }

    @Override
    public Vector getChaseTarget(Ghost thisGhost) {
        target.reset(logic.pacman_coord_X(), logic.pacman_coord_Y());
        return target;
    }
}