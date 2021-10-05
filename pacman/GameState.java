package pacman;

/**
 *
 * @author Falie
 */
public enum GameState {
    Play(false),
    Win(true),
    GameOver(true),
    LifeLost(true);
    
    private final boolean end;
    
    GameState(boolean end){
        this.end=end;
    }
    
    public boolean endGame(){ return end; }
}