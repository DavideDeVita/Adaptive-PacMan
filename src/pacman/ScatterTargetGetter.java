package pacman;

/**
 *
 * @author Falie
 */
public abstract class ScatterTargetGetter {
    protected final Vector target;
    protected final GameLogic logic;

    public ScatterTargetGetter(GameLogic logic) {
        this.target = new Vector(0,0);
        this.logic = logic;
    }

    public abstract Vector getScatterTarget(Ghost thisGhost);
}

class ScatterTargetGetter_Const extends ScatterTargetGetter{
    public ScatterTargetGetter_Const(GameLogic logic, int x, int y) {
        super(logic);
        this.target.reset( x, y );
    }
    public ScatterTargetGetter_Const(GameLogic logic, Vector target) {
        super(logic);
        this.target.reset( target );
    }
    
    @Override
    public Vector getScatterTarget(Ghost thisGhost) {
        return target;
    }
    
}

class ScatterTargetGetter_RandomArea extends ScatterTargetGetter{
    int xMin, yMin, xMax, yMax;

    public ScatterTargetGetter_RandomArea(GameLogic logic, int xMin, int yMin, int xMax, int yMax) {
        super(logic);
        this.xMin = xMin;
        this.yMin = yMin;
        this.xMax = xMax;
        this.yMax = yMax;
    }

    /*Generates a square centered in (x, y)*/
    public ScatterTargetGetter_RandomArea(GameLogic logic, int x, int y, int half_width) {
        super(logic);
        this.xMin = x-half_width;
        this.yMin = y-half_width;
        this.xMax = x+half_width;
        this.yMax = y+half_width;
    }
    
    @Override
    public Vector getScatterTarget(Ghost thisGhost) {
        target.x = Utils.random(xMin, xMax);
        target.y = Utils.random(yMin, yMax);
        return target;
    }
}