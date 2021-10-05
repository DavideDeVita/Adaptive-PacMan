package pacman;

/**
 *
 * @author Falie
 */
public class ChaseTarget_Blinky_Std extends ChaseTargetGetter{
    public ChaseTarget_Blinky_Std() {
        super();
    }

    @Override
    public Vector getChaseTarget(GameLogic logic, Ghost thisGhost) {
        target.reset(logic.pacman_coord_X(), logic.pacman_coord_Y());
        return target;
    }
}

class ChaseTarget_Blinky_RoamAround extends ChaseTargetGetter{
    private final int rx, ry;
    public ChaseTarget_Blinky_RoamAround(int rx, int ry) {
        super();
        this.rx=rx;
        this.ry=ry;
    }
    public ChaseTarget_Blinky_RoamAround(int r) {
        super();
        this.rx=r;
        this.ry=r;
    }

    @Override
    public Vector getChaseTarget(GameLogic logic, Ghost thisGhost) {
        target.x = logic.pacman_coord_X() + Utils.random(-rx, rx);
        target.y = logic.pacman_coord_Y() + Utils.random(-ry, ry);
        //_Log.a("Blinky Chase roam", "Pacman is in "+logic.pacman.tile()+".. chasing "+target);
        return target;
    }
}