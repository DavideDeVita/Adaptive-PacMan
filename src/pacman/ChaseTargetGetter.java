package pacman;

/**
 *
 * @author Falie
 */
public abstract class ChaseTargetGetter {
    protected final Vector target;

    public ChaseTargetGetter() {
        this.target = new Vector(0,0);
    }

    public abstract Vector getChaseTarget(GameLogic logic, Ghost thisGhost);
}