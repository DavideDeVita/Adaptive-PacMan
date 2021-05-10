package pacman;

/**
 *
 * @author Falie
 */
abstract class InkyReferenceGhost_Getter {
    public abstract int getInkyReferenceGhost(GameLogic gameLogic);
}

class InkyReferenceGhost_Getter_Std extends InkyReferenceGhost_Getter{

    @Override
    public int getInkyReferenceGhost(GameLogic gameLogic) {
        return GameLogic.BLINKY;
    }
    
}