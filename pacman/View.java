package pacman;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JPanel;
import static pacman.Board.TILE_LOGIC_SIZE;

/**
 *
 * @author Falie
 */
public class View extends JPanel{
    private final Game_PacMan pmg;
    private final Board b;
    private final GameLogic gl;
    private final int renderAroundWidth;
    
    final static int TILE_SIZE = 22,
            DOT_RADIUS = 7,
            ENERGIZER_RADIUS = 15,
            PACMAN_RADIUS = 32,
            GHOST_RADIUS = 28;
    
    
    public View(Game_PacMan game){
        this(game, 2);
    }
    public View(Game_PacMan game, int renderAroundWidth){
        this.pmg=game;
        this.b = game.board;
        this.gl=game.logic;
        this.renderAroundWidth=renderAroundWidth;
        
        setSize( b.cols*TILE_SIZE, (b.rows+1)*TILE_SIZE );

        addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent ke) { }

            @Override
            public void keyReleased(KeyEvent e) { }

            @Override
            public void keyPressed(KeyEvent ke) {
                gl.notifyKeyTiped(ke);
            }
        });

        setFocusable(true);
        requestFocusInWindow();
    }
    
    public void fullRender(){
        Graphics g = this.getGraphics();
        
        for (int y=0; y<b.rows; y++){
            for (int x=0; x<b.cols; x++){
                drawTile(g, x, y);
            }
        }
        /*Color color;
        for (y=0; y<b.rows; y++){
            for (x=0; x<b.cols; x++){
                switch (b.maze[y][x].directions){
                    case Corridor_V:
                    case Corridor_H:
                    case Corner_NW:
                    case Corner_SW:
                    case Corner_SE:
                    case Corner_NE:
                        color = Color.BLACK;
                        break;
                    case Horizontal_Up_Exc:
                        color = Color.YELLOW;
                        break;
                    case NoWay:
                        color = Color.BLUE;
                        break;
                    case Vertical_Dx:
                    case Vertical_Sx:
                    case Horizontal_Up:
                    case Horizontal_Down:
                    case Cross:
                        color = Color.RED;
                        break;
                    case Ghost_Door:
                        color = Color.WHITE;
                        break;
                    default:
                        throw new AssertionError(maze[y][x].directions.name());
                }
                g.setColor(color);
                g.fillRect(TILE_SIZE*x, TILE_SIZE*y, TILE_SIZE, TILE_SIZE);
            }
        }*/
        
        //Draw Ghosts
        for(int y=0; y<gl.ghosts.length; y++){
            drawGhost(g, gl.ghosts[y]);
        }
        //draw Pacman
        drawPacMan(g, gl.pacman);
    }
    public void render(){
        Graphics g = this.getGraphics();
        
        //Render around agents
        for(int i=0; i<gl.ghosts.length; i++)
            renderAround(g, gl.ghosts[i]);
        renderAround(g, gl.pacman);
        //draw agents
        for(int i=0; i<gl.ghosts.length; i++){
            drawGhost(g, gl.ghosts[i]);
            //drawGhost(g, gl.ghosts[i]);
        }
        drawPacMan(g, gl.pacman);

        //drawColor(g, 13,14, Color.ORANGE);
        //drawColor(g, 13,11, Color.GREEN);
    }
    public void renderAround(Graphics g, Agent agent){
        int coord_X = b.logicToCoordinate_X( agent.x ),
            coord_Y = b.logicToCoordinate_Y( agent.y );
        for(int y=-renderAroundWidth; y<=renderAroundWidth; y++)
            for(int x=-renderAroundWidth; x<=renderAroundWidth; x++)
                drawTile(g, 
                    (coord_X+x + b.cols) % b.cols, 
                    (coord_Y+y + b.rows) % b.rows
                );
    }
    
    public void drawTile(Graphics g, int x, int y){
        switch (b.maze[y][x].contains){
            case Dot:
                drawDot(g, x, y);
                break;
            case Energizer:
                drawEnergizer(g, x, y);
                break;
            case Wall:
                drawWall(g, x, y);
                break;
            case Door:
                drawDoor(g, x, y);
                break;

            case Fruit:
                System.out.println("Fruit graphic unimplemented");
            case Empty:
            default:
                drawEmpty(g, x, y);
        }
    }
    
    public void drawEmpty(Graphics g, int x, int y){
        g.setColor(Color.BLACK);
        g.fillRect(TILE_SIZE*x, TILE_SIZE*y, TILE_SIZE, TILE_SIZE); //it should be (x, y) so first x then y
        
    }
    public void drawDoor(Graphics g, int x, int y){
        g.setColor(Color.BLACK);
        g.fillRect(TILE_SIZE*x, TILE_SIZE*y, TILE_SIZE, TILE_SIZE); //it should be (x, y) so first x then y
        g.setColor(Color.WHITE);
        g.fillRect(TILE_SIZE*x, TILE_SIZE*y + TILE_SIZE/2, TILE_SIZE, TILE_SIZE/5); //it should be (x, y) so first x then y
        
    }
    public void drawWall(Graphics g, int x, int y){
        g.setColor(Color.BLUE);
        g.fillRect(TILE_SIZE*x, TILE_SIZE*y, TILE_SIZE, TILE_SIZE); //it should be (x, y) so first x then y
        
    }
    public void drawDot(Graphics g, int x, int y){
        g.setColor(Color.BLACK);
        g.fillRect(TILE_SIZE*x, TILE_SIZE*y, TILE_SIZE, TILE_SIZE); //it should be (x, y) so first x then y
        
        g.setColor(Color.WHITE);
        g.fillOval(TILE_SIZE*x+(TILE_SIZE/2)-(DOT_RADIUS/2), TILE_SIZE*y+(TILE_SIZE/2)-(DOT_RADIUS/2), DOT_RADIUS, DOT_RADIUS);
    }
    public void drawEnergizer(Graphics g, int x, int y){
        g.setColor(Color.BLACK);
        g.fillRect(TILE_SIZE*x, TILE_SIZE*y, TILE_SIZE, TILE_SIZE); //it should be (x, y) so first x then y
        
        g.setColor(Color.WHITE);
        g.fillOval(TILE_SIZE*x+(TILE_SIZE/2)-(ENERGIZER_RADIUS/2), TILE_SIZE*y+(TILE_SIZE/2)-(ENERGIZER_RADIUS/2), ENERGIZER_RADIUS, ENERGIZER_RADIUS);
    }
    public void drawColor(Graphics g, int x, int y, Color c){
        g.setColor(c);
        g.fillRect(TILE_SIZE*x, TILE_SIZE*y, TILE_SIZE, TILE_SIZE); //it should be (x, y) so first x then y
    }
    
    public void drawPacMan(Graphics g, PacMan pacman){
        int x=logicToScreen_X(pacman.x),
                y=logicToScreen_Y(pacman.y);
        
        g.setColor(Color.YELLOW);
        g.fillOval(x-(PACMAN_RADIUS/2), y-(PACMAN_RADIUS/2), PACMAN_RADIUS, PACMAN_RADIUS);
    }
    public void drawGhost(Graphics g, Ghost ghost){
        int x=logicToScreen_X(ghost.x),
                y=logicToScreen_Y(ghost.y);
        
        g.setColor( ghost.getColor() );
        g.fillOval(x-(GHOST_RADIUS/2), y-(GHOST_RADIUS/2), GHOST_RADIUS, GHOST_RADIUS);
    }
    
    public int logicToScreen_X(int logic_x){
        return b.logicToCoordinate_X(logic_x)*TILE_SIZE + (b.logicToRemainder_X(logic_x)*TILE_SIZE/TILE_LOGIC_SIZE);
    }
    public int logicToScreen_Y(int logic_y){
        return b.logicToCoordinate_Y(logic_y)*TILE_SIZE + (b.logicToRemainder_Y(logic_y)*TILE_SIZE/TILE_LOGIC_SIZE);
    }
}