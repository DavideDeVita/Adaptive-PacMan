package pacman;

/**
 *
 * @author Falie
 */
public class ChaseTarget_Clyde_Std extends ChaseTargetGetter{
    public ChaseTarget_Clyde_Std(GameLogic logic) {
        super(logic);
    }

    @Override
    public Vector getChaseTarget(Ghost thisGhost) {
        if( Utils.euclidean_dist2(thisGhost, logic.pacman)>64 )
            target.reset(logic.pacman_coord_X(), logic.pacman_coord_Y());
        else
            target.reset( thisGhost.scatterTile.getScatterTarget(thisGhost) );
        return target;
    }
}
