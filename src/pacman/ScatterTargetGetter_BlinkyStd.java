package pacman;

/**
 * @author Falie
 */
class ScatterTargetGetter_BlinkyStd extends ScatterTargetGetter_Const{
    /**Elroy Cruise*/
    
    public ScatterTargetGetter_BlinkyStd(GameLogic logic, int x, int y) {
        super(logic, x, y);
    }
    public ScatterTargetGetter_BlinkyStd(GameLogic logic, Vector target) {
        super(logic, target);
    }
    
    @Override
    public Vector getScatterTarget(Ghost thisGhost) {
        if(logic.cruiseElroyChase())
            return thisGhost.chaseTile.getChaseTarget(thisGhost);
        else
            return target;
    }
}
