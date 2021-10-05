package pacman;

/**
 *
 * @author Falie
 */
public enum GhostPersonalState {
    Housed(false), Exiting(true), Eaten(true), Out(false);
    
    private final boolean canGoPastDoor;

    private GhostPersonalState(boolean canGoPastDoor) {
        this.canGoPastDoor = canGoPastDoor;
    }
    
    boolean canGoPastDoor() {
        return canGoPastDoor;
    }
}
