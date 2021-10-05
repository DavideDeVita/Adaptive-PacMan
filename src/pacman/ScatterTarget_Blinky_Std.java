package pacman;

import static pacman.State.Chase;

/**
 * @author Falie
 */
class ScatterTarget_Blinky_Std extends ScatterTargetGetter_Const{
    /**Elroy Cruise*/
    
    public ScatterTarget_Blinky_Std(int x, int y) {
        super( x, y);
    }
    public ScatterTarget_Blinky_Std(Vector target) {
        super( target);
    }
    
    @Override
    public Vector getScatterTarget(GameLogic logic, Ghost thisGhost) {
        if(logic.eloryChasesOnScatter() && logic.cruiseElroyChase())
            return elroyDoesntScatter(logic, thisGhost);
        else
            return target;
    }
    
    protected Vector elroyDoesntScatter(GameLogic logic, Ghost thisGhost){
        thisGhost.lastTargetGetter=Chase;
        return thisGhost.chaseTile.getChaseTarget(logic, thisGhost);
    }
}

class ScatterTarget_Blinky_RandomArea extends ScatterTarget_RandomArea{
    private final Vector ifNotInQuadrant;
    private boolean requireRecompute=true;
    
    public ScatterTarget_Blinky_RandomArea(int xMin, int yMin, int xMax, int yMax) {
        super(xMin, yMin, xMax, yMax);
        ifNotInQuadrant = new Vector(xMax, yMax);
    }

    /*Generates a square centered in (x, y)*/
    public ScatterTarget_Blinky_RandomArea(int x, int y, int half_width) {
        super( x, y, half_width);
        ifNotInQuadrant = new Vector(xMax, yMax);
    }
    
    @Override
    public Vector getScatterTarget(GameLogic logic, Ghost thisGhost) {
        if(logic.eloryChasesOnScatter() && logic.cruiseElroyChase()){
            requireRecompute=true;
            return elroyDoesntScatter(logic, thisGhost);
        }
        else{
            //target.x = Utils.random(xMin, xMax);
            //target.y = Utils.random(yMin, yMax);
            //return target;
            if( Utils.between(thisGhost.coord_X(), xMin, xMax) &&
                    Utils.between(thisGhost.coord_Y(), yMin, yMax) ){
                if(requireRecompute)
                    thisGhost.lastTargetGetter=null;
                requireRecompute=false;
                return super.getScatterTarget(logic, thisGhost);
            }
            else{
                //_Log.a("Blinky Rand", "Not in Quadrant");
                requireRecompute=true;
                return ifNotInQuadrant;
            }
        }
    }
    
    protected Vector elroyDoesntScatter(GameLogic logic, Ghost thisGhost){
        thisGhost.lastTargetGetter=Chase;
        return thisGhost.chaseTile.getChaseTarget(logic, thisGhost);
    }
}