package pacman;

import static pacman.State.Scatter;

/**
 *
 * @author Falie
 */
public abstract class ScatterTargetGetter {
    protected final Vector target;

    public ScatterTargetGetter() {
        this.target = new Vector(0,0);
    }

    public abstract Vector getScatterTarget(GameLogic logic, Ghost thisGhost);
}

class ScatterTargetGetter_Const extends ScatterTargetGetter{
    public ScatterTargetGetter_Const(int x, int y) {
        super();
        this.target.reset( x, y );
    }
    public ScatterTargetGetter_Const(Vector target) {
        super();
        this.target.reset( target );
    }
    
    @Override
    public Vector getScatterTarget(GameLogic logic, Ghost thisGhost) {
        return target;
    }
    
}

class ScatterTarget_RandomArea extends ScatterTargetGetter{
    protected final int xMin, yMin, xMax, yMax;

    public ScatterTarget_RandomArea(int xMin, int yMin, int xMax, int yMax) {
        super();
        this.xMin = xMin;
        this.yMin = yMin;
        this.xMax = xMax;
        this.yMax = yMax;
    }

    /*Generates a square centered in (x, y)*/
    public ScatterTarget_RandomArea(int x, int y, int half_width) {
        super();
        this.xMin = x-half_width;
        this.yMin = y-half_width;
        this.xMax = x+half_width;
        this.yMax = y+half_width;
    }
    
    @Override
    public Vector getScatterTarget(GameLogic logic, Ghost thisGhost) {
        if(thisGhost.lastTargetGetter!=Scatter){
            target.x = Utils.random(xMin, xMax);
            target.y = Utils.random(yMin, yMax);
            //_Log.a(thisGhost+" Rand Scatter", "x:["+xMin+":"+xMax+"]\ty:["+yMin+":"+yMax+"]: "+target);
        }
        return target;
    }
}