package pacman;

/**
 * @author Falie
 */
class ScatterTarget_Blinky_Std extends ScatterTargetGetter_Const{
    /**Elroy Cruise*/
    
    public ScatterTarget_Blinky_Std(GameLogic logic, int x, int y) {
        super(logic, x, y);
    }
    public ScatterTarget_Blinky_Std(GameLogic logic, Vector target) {
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
