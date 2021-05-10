package pacman;

import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 *
 * @author Falie
 */
public final class Game_PacMan implements Runnable{
    Thread renderThread;
    Board board;
    final GameLogic gameLogic;
    boolean running;
    private final static int desiredFPS=25;
    private final static float onStart_Wait=3f, onLose_Wait=2f;
    
    private View view;
    private JFrame jFrame;

    public Game_PacMan(Board board) {
        this.board = board;
        CustomSpecifics cs_b = new CustomSpecifics();
        
        this.gameLogic = new GameLogic(board, this, cs_b);
        setView(new View(this));
        this.gameLogic.createAgents();
    }

    void setView(View view) {
        this.view=view;
        jFrame = new JFrame("Simple Pac-man");
        jFrame.setBackground(Color.BLUE);
        jFrame.setSize( board.cols * View.TILE_SIZE, (board.rows+1) * View.TILE_SIZE );
        jFrame.setLocationRelativeTo(null);
        jFrame.setResizable(false);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.add(view);
        jFrame.setVisible(true);
    }
    
    private  void gameLoop() {
        resume();
    }
    
    /** Starts the game loop in a separate thread.
     */
    public void resume() {
        running = true;
        renderThread = new Thread(this);
        renderThread.start();         
    }

    /** Stops the game loop and waits for it to finish
     */
    public void pause() {
        running = false;
        while(true) {
            try {
                renderThread.join();
                break;
            } catch (InterruptedException e) {
                // just retry
            }
        }
    }

    @Override /**This is the Game Loop.. everything called in here should have @gameLoop*/
    public void run() {
        long startTime = System.nanoTime(),
                fpsTime = startTime,
                currentTime;
        int frameCounter = 0, secondsCounter=0;
        float avgFps=0, 
                onStart_yetToWait=onStart_Wait, onLose_yetToWait=0f;
        GameState gameState=GameState.Play;

        /*** The Game Main Loop ***/
        view.firstRender();
        view.firstRender(); //Dunno why.. whitout it, sometimes does not draw all
        while (running && !gameState.endGame()) {
            currentTime = System.nanoTime();
            // deltaTime is in seconds
            float deltaTime = (currentTime-startTime) / 1_000_000_000f,//seconds
                  fpsDeltaTime = (currentTime-fpsTime) / 1_000_000_000f;
            
            if(deltaTime < 1f/desiredFPS)
                continue;
            startTime = currentTime;

            //If life is lost.. freeze the death screen for the first seconds
            if ( onLose_yetToWait>0 ){
                onLose_yetToWait-=deltaTime;
                if(onLose_yetToWait <= 0){
                    gameLogic.restart();
                    view.firstRender();
                    view.firstRender();
                    onStart_yetToWait = onStart_Wait;
                }
                else{
                    continue;
                }
            }
            //If (re)starting.. freeze the screen for the first seconds
            if ( onStart_yetToWait>0 ){
                jFrame.setTitle("Simple Pac-man:        Ready Player 1?");
                System.out.println("\nSimple Pac-man:        Ready Player 1?");
                onStart_yetToWait-=deltaTime;
                //continue;//Perde un frame ma è più rapido e pulito
                if(onStart_yetToWait>0)
                    continue;
                else{
                    onStart_yetToWait=0;
                    jFrame.setTitle("Simple Pac-man:        "+gameLogic.pac_lives+" lives left");
                }
            }
                
            gameState = gameLogic.update(deltaTime);
            if(gameState == GameState.LifeLost){
                jFrame.setTitle("Simple Pac-man:        Life Lost !");
                gameState = GameState.Play;
                onLose_yetToWait = onLose_Wait;
                continue;
            }
            else if(gameState.endGame()){
                jFrame.setTitle("Simple Pac-man:        Game Over");
                break;
            }
            //else
            //    jFrame.setTitle("Simple Pac-man:        "+gameLogic.pac_lives+" lives left");
            
            view.render();
            //jFrame.repaint();

            // Measure FPS
            frameCounter++;
            if (fpsDeltaTime > 1) { // every second
            	avgFps=((avgFps*secondsCounter)+frameCounter)/(secondsCounter+1);
	            secondsCounter++;
	            //if(_Log.LOG_ACTIVE){
	                //System.out.println("Current FPS = " + frameCounter+"\n        avgFps: "+avgFps);//}
                frameCounter = 0;
                fpsTime = currentTime;
            }
        }
        
        //System.out.println("AVG FPS: "+avgFps);//}
    }

    void signKeyListener(PacMan_Player pacman_player) {
        view.addKeyListener(pacman_player);
    }
    
    
    
    
    public static void main (String... args){
        /**
         * 0: Dot
         * 1: Wall
         * 6: Empty
         * 8: Energizer
         * 7: Ghost Door
         * 3: Up Exception EMPTY
         * 9: Up Exception DOT
         */
        int[][] file = new int[][]{
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, 
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, 
            {1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1}, 
            {1, 8, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 8, 1}, 
            {1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1}, 
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, 
            {1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1}, 
            {1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1}, 
            {1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1}, 
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 6, 1, 1, 6, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1}, 
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 6, 1, 1, 6, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1}, 
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 6, 6, 6, 3, 2, 2, 3, 6, 6, 6, 1, 1, 0, 1, 1, 1, 1, 1, 1}, 
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 6, 1, 1, 1, 7, 7, 1, 1, 1, 6, 1, 1, 0, 1, 1, 1, 1, 1, 1}, 
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 6, 1, 1, 5, 5, 5, 5, 1, 1, 6, 1, 1, 0, 1, 1, 1, 1, 1, 1}, 
            {4, 4, 4, 4, 4, 4, 0, 6, 6, 6, 1, 1, 5, 5, 5, 5, 1, 1, 6, 6, 6, 0, 4, 4, 4, 4, 4, 4}, 
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 6, 1, 1, 5, 5, 5, 5, 1, 1, 6, 1, 1, 0, 1, 1, 1, 1, 1, 1}, 
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 6, 1, 1, 1, 1, 1, 1, 1, 1, 6, 1, 1, 0, 1, 1, 1, 1, 1, 1}, 
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 1, 1, 0, 1, 1, 1, 1, 1, 1}, 
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 6, 1, 1, 1, 1, 1, 1, 1, 1, 6, 1, 1, 0, 1, 1, 1, 1, 1, 1}, 
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 6, 1, 1, 1, 1, 1, 1, 1, 1, 6, 1, 1, 0, 1, 1, 1, 1, 1, 1}, 
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, 
            {1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1}, 
            {1, 8, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 8, 1}, 
            {1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 9, 0, 0, 9, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1}, 
            {1, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 1}, 
            {1, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 1}, 
            {1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1}, 
            {1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1}, 
            {1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1}, 
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, 
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
        };
        
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                Board board = new Board(file);
                Game_PacMan game = new Game_PacMan(board);
                System.out.println(board.rows+" "+board.cols);
                game.gameLoop();
            }
            
        });
    }
}