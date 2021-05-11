package pacman;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import static pacman.Direction.*;

/**
 * @author Falie
 */
public class PacMan_Player extends PacMan implements KeyListener{
    Direction tryDir;

    public PacMan_Player(String name, GameLogic logic, int startX, int startY, Direction startDir) {
        super(name, logic, startX, startY, startDir);
        tryDir=startDir;
        logic.signKeyListener(this);
    }

    @Override
    void updateDirection() {
        if(tryDir!=dir && logic.canGo(this, tryDir))
            dir=tryDir;
    }

    @Override
    public void keyTyped(KeyEvent ke) {
        switch (ke.getKeyCode()){
            case KeyEvent.VK_UP:
                tryDir=Up;
                //System.out.println("Arrow Up");
                break;
            case KeyEvent.VK_LEFT:
                //System.out.println("Arrow Left");
                tryDir=Left;
                break;
            case KeyEvent.VK_DOWN:
                //System.out.println("Arrow down");
                tryDir=Down;
                break;
            case KeyEvent.VK_RIGHT:
                //System.out.println("Arrow Right");
                tryDir=Right;
                break;
        }
    }

    @Override
    public void keyPressed(KeyEvent ke) { }

    @Override
    public void keyReleased(KeyEvent ke) { }

    @Override
    protected void onResetFromWall() {  }
}
