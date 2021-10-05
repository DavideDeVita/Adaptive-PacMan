package pacman;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import static pacman.Direction.*;

/**
 * @author Falie
 */
public class PacMan_Player extends PacMan implements KeyListener{

    public PacMan_Player(String name, GameLogic logic, int startX, int startY, Direction startDir) {
        super(name, logic, startX, startY, startDir);
        logic.signKeyListener(this);
    }

    @Override
    public void resetPosition(int coord_x, int coord_y, Direction dir) {
        super.resetPosition(coord_x, coord_y, dir);
        tryDir=Left;
    }

    @Override
    public void keyTyped(KeyEvent ke) {
        switch (ke.getKeyCode()){
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                tryDir=Up;
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                tryDir=Left;
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                tryDir=Down;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
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
