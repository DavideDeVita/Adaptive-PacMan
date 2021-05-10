package pacman;

/**
 *
 * @author Falie
 */
public abstract class ChaseTargetGetter {
    protected final Vector target;
    protected final GameLogic logic;

    public ChaseTargetGetter(GameLogic logic) {
        this.target = new Vector(0,0);
        this.logic = logic;
    }

    public abstract Vector getChaseTarget(Ghost thisGhost);
}