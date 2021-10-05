package pacman;

import static pacman.Collectible.*;
import static pacman.Path.*;
import static pacman.Utils.between;

/**
 *
 * @author Falie
 */
public class Board {
    final Tile[][] mazeBackup;
    Tile[][] maze;
    Vector ghostRespawn = new Vector(13,14), 
            ghostOutDoor = new Vector (13, 11);
    static int rows, cols;
    int m_width, m_height;
    int energizers=0, dots=0;
    
    /**Agents move inside the tile and, then, among tiles
     * i.e. if a unit is at the beginning of a tile and moves of 10 units, it is still in the sam tile.
     * Ricorda di aggiornare i valori degli agenti: se mi muovo in una cella verso l'alto, 
     * dovrò trovarmi al centro (orizzontale) della nuova cella
     */
    public static final int TILE_LOGIC_SIZE = 66;//66
    public static final float TURNING_RANGE = 0.45f; //in the middle TURNING_RANGE of a tile it is possible to take a 90° turn. i.e. you should not be able to turn if you are on the first unit of a tile.. too soon
    public static final int TURNING_MIN = (int)((TILE_LOGIC_SIZE/2f)*(1f-TURNING_RANGE)),
                            TURNING_MAX = (int)((TILE_LOGIC_SIZE/2f)*(1f+TURNING_RANGE));
    
    Board(int[][] file){
        rows=file.length;
        cols=file[0].length;
        
        m_width = cols*TILE_LOGIC_SIZE;
        m_height = rows*TILE_LOGIC_SIZE;
        
        maze = new Tile[rows][cols];
        mazeBackup = new Tile[rows][cols];
        for (int y=0; y<rows; y++){
            for (int x=0; x<cols; x++){
                maze[y][x] = new Tile();
                mazeBackup[y][x] = new Tile();
                switch (file[y][x]){
                    case 1: 
                        maze[y][x].contains = mazeBackup[y][x].contains = Wall;
                        break;
                    case 9:
                    case 0:
                        maze[y][x].contains = mazeBackup[y][x].contains = Dot;
                        dots++;
                        break;
                    case 3:
                    case 6:
                        maze[y][x].contains = mazeBackup[y][x].contains = Empty;
                        break;
                    case 8:
                        maze[y][x].contains = mazeBackup[y][x].contains = Energizer;
                        dots++;
                        energizers++;
                        break;
                    case 7:
                        /*The door to the ghost house:
                        *   PacMan can never go trough
                        *   Ghosts only can while exiting or entering
                        */
                        maze[y][x].contains = mazeBackup[y][x].contains = Door;
                        break;
                    case 4:
                        /*The toroidal tunnel:
                        *   PacMan can alaways go trough
                        *   Ghosts slow here
                        */
                        maze[y][x].contains = mazeBackup[y][x].contains = Tunnel;
                        break;
                    case 5:
                        /*The inside of the Ghost house
                        *   If eaten, a ghost here will return alive.. well as much as a ghost can
                        */
                        maze[y][x].contains = mazeBackup[y][x].contains = House;
                        //ghostRespawn.reset(x, y);
                        break;
                    case 2:
                        /*Right outside of the door:
                            Stepping here, for a ghost, changes state from Exiting to Out
                        */
                        maze[y][x].contains = mazeBackup[y][x].contains = OutDoor;
                        //ghostOutDoor.reset(x, y);
                        break;
                }
                maze[y][x].directions = checkDirections(file, y, x);
                if(maze[y][x].directions==Horizontal_Up && (file[y][x]==3 || file[y][x]==9) )
                    maze[y][x].directions=Horizontal_Up_Exc;
                else if( (maze[y][x].directions==Vertical_Sx || maze[y][x].directions==Vertical_Dx)
                        && file[y][x]==7 )
                    maze[y][x].directions=Ghost_Door;
                
                mazeBackup[y][x].directions = maze[y][x].directions;
            }
        }
    }

    private Path checkDirections(int[][] file, int i, int j) {
        if(file[i][j]==1)
            return NoWay;
        else{
            boolean up, left, down, right;
            up = file[ (i+rows-1)%rows ][ j ] != 1; //Up is no Wall
            down = file[ (i+rows+1)%rows ][ j ] != 1; //Down is no Wall
            left = file[ i ][ (j+cols-1)%cols ] != 1; //Left is no Wall
            right = file[ i ][ (j+cols+1)%cols ] != 1; //Right is no Wall
            return Path.getFromOpenings(up, left, down, right);
        }
    }

    public int countDots() {
        int count=0;
        for(int x=0; x<cols; x++){
            for(int y=0; y<rows; y++){
                if( elementIn(x, y)==Dot || elementIn(x, y)==Energizer )
                    count++;
            }
        }
        return count;
    }

    public void eatPellet(int x, int y) {
        switch (elementIn(x, y)){ //Useless.. but let's be safe
            case Energizer:
            case Dot:
                maze[y][x].contains=Empty;
                break;
        }
    }

    public Vector getGhostHouse() {
        return this.ghostRespawn;
    }
    public Vector getOutDoor() {
        return this.ghostOutDoor;
    }
    
    /*Rememeber that first dimension is height and second is width*/
    public Collectible elementIn(int x, int y){ 
        if(x<0) x+=cols;
        else if(x>=cols) x-=cols;
        
        if(y<0) y+=rows;
        else if(y>=rows) y-=rows;
        
        return maze[y][x].contains; 
    }
    public Collectible elementIn(Vector tile){ 
        if(tile.x<0) tile.x+=cols;
        else if(tile.x>=cols) tile.x-=cols;
        
        if(tile.y<0) tile.y+=rows;
        else if(tile.y>=rows) tile.y-=rows;
        
        return maze[tile.y][tile.x].contains; 
    }
    public Path pathIn(int x, int y) { 
        if(x<0) x+=cols;
        else if(x>=cols) x-=cols;
        
        if(y<0) y+=rows;
        else if(y>=rows) y-=rows;
        
        return maze[y][x].directions; 
    }
    
    public int logicToCoordinate_X(int logic_x){
        return logic_x/TILE_LOGIC_SIZE;
    }
    public int logicToCoordinate_Y(int logic_y){
        return logic_y/TILE_LOGIC_SIZE;
    }
    public int logicToRemainder_X(int logic_x){
        return logic_x%TILE_LOGIC_SIZE;
    }
    public int logicToRemainder_Y(int logic_y){
        return logic_y%TILE_LOGIC_SIZE;
    }

    /**Half tiles methods return the x,y of the exact middle of the tile the logical position is in*/
    int halfTile_X(int logic_X) {
        return logicToCoordinate_X( logic_X )*TILE_LOGIC_SIZE + (TILE_LOGIC_SIZE/2);
    }
    int halfTile_Y(int logic_Y) {
        return logicToCoordinate_Y( logic_Y )*TILE_LOGIC_SIZE + (TILE_LOGIC_SIZE/2);
    }
    
    int coord_to_logicalHalfTile_X(int coord_x) {
        return coord_x*TILE_LOGIC_SIZE + (TILE_LOGIC_SIZE/2);
    }
    int coord_to_logicalHalfTile_Y(int coord_Y) {
        return coord_Y*TILE_LOGIC_SIZE + (TILE_LOGIC_SIZE/2);
    }
    int coord_to_logicalTile_X(int coord_x) {
        return coord_x*TILE_LOGIC_SIZE ;
    }
    int coord_to_logicalTile_Y(int coord_Y) {
        return coord_Y*TILE_LOGIC_SIZE ;
    }
    
    int xFix(int x){ return (x+cols)%cols; }
    int yFix(int y){ return (y+rows)%rows; }
    
    int logic_xFix(int x){ return (x+m_width)%m_width; }
    int logic_yFix(int y){ return (y+m_height)%m_height; }
    
    /** Data la prossima coordinata logica
     * restituisce una coordinata logica CORRETTA (evitando che ci si muova di più di una cella in un frame)
     */
    int capMovement_X(Agent agent, int next_X){
        if( Utils.difference(agent.x, next_X)>TILE_LOGIC_SIZE ){
            if(_Log.LOG_ACTIVE) _Log.w("Movement Cap LOGIC", agent+": "+agent.x+"\t nextX was "+next_X+"\n"
                    + "\tset to "+(agent.x + TILE_LOGIC_SIZE*agent.dir.x) );
            if(_Log.LOG_ACTIVE) _Log.w("Movement Cap COORD", agent+": "+agent.coord_X()+"\t nextX was "+logicToCoordinate_X(next_X)+"\n"
                    + "\tset to "+logicToCoordinate_X(agent.x + TILE_LOGIC_SIZE*agent.dir.x) );
            next_X = agent.x + TILE_LOGIC_SIZE*agent.dir.x;
        }
        return logic_xFix(next_X);
    }
    int capMovement_Y(Agent agent, int next_Y){
        if( Utils.difference(agent.y, next_Y)>TILE_LOGIC_SIZE ){
            if(_Log.LOG_ACTIVE){
                _Log.w("Movement Cap LOGIC", agent+": "+agent.y+"\t nextX was "+next_Y+"\n"
                    + "\tset to "+(agent.x + TILE_LOGIC_SIZE*agent.dir.x) );
                _Log.w("Movement Cap COORD", agent+": "+agent.coord_Y()+"\t nextX was "+logicToCoordinate_Y(next_Y)+"\n"
                    + "\tset to "+logicToCoordinate_Y(agent.y + TILE_LOGIC_SIZE*agent.dir.y) );
            }
            next_Y = agent.y + TILE_LOGIC_SIZE*agent.dir.y;
        }
        return logic_yFix(next_Y);
    }

    public boolean canTurn90(int x, int y, Direction dir) {
        switch (dir){
            case Up:
            case Down:
                x=logicToRemainder_X(x);
                return between(x, TURNING_MIN, TURNING_MAX);
            case Left:
            case Right:
                y=logicToRemainder_Y(y);
                return between(y, TURNING_MIN, TURNING_MAX);
        }
        return true;
    }

    public void reset() {
        for (int r=0; r<rows; r++){
            for (int c=0; c<cols; c++)
                maze[r][c].contains = mazeBackup[r][c].contains;
        }
    }

    public int reset(int d, int e) {//d = dotsCollected; e=energLeft
        reset();
        int minErasedD = getMinD(d),
                maxErasedD = getMaxD(d),
                eraseE = e;
        float chance_eraseD = (minErasedD+maxErasedD)/(2f*dots),
                chance_eraseE = 1f*eraseE/energizers;
        int countD=dots, countE=energizers;
        /*if(_Log.LOG_ACTIVE) _Log.d("Board reset", "eraseD: "+minErasedD+" - "+maxErasedD+"\n"
                + "eraseE: "+eraseE+"\n"
                + "chance_D: "+chance_eraseD+"\n"
                + "chance_E: "+chance_eraseE+"\n"
                + "count_E: "+countE+"\n"
                + "countD: "+countD);*/
        int offR = Utils.random( (rows-1)/3, (rows-1)/2 ),//Gives more chance to begin erasure from the bottom
                offC = Utils.random(0, cols-1);
        
        float enhancer=1f; //If last was no Dot 1, else: if removed =2.5 : else =0.25
        
        while (countE>energizers-e || countD>dots-minErasedD ){
            for (int for_r=1; for_r<=rows; for_r++){
                int r = rows-for_r;
                enhancer=1f;
                for (int for_c=0; for_c<cols; for_c++){
                    int c = (for_c+offC)%cols;
                    Collectible tile = maze[r][c].contains,
                            backup = mazeBackup[r][c].contains;
                    //!Utils.between(countD, dots-maxErasedD, dots-minErasedD)
                    if( backup==Dot && tile==Dot && countD>dots-maxErasedD ){
                        if (Utils.chance(chance_eraseD*enhancer) ){
                            maze[r][c].contains = Empty;
                            countD--;
                            enhancer=2.5f;
                        }
                        else
                            enhancer=0.25f;
                    }
                    else{
                        enhancer=1f;
                        if ( backup==Energizer && tile==Energizer && countE>energizers-e ){
                            if (Utils.chance(chance_eraseE) ){
                                maze[r][c].contains = Empty;
                                countD--;
                                countE--;
                            }
                        }
                    }
                }
            }
        }
        return countD;
    }

    public int reset_BACKUP(int d, int e) {
        reset();
        int minErasedD = (d<=2) ? d*75 : 200,
                maxErasedD = (d<=1) ? (d+1)*75 : 
                                (d==2) ? 200 : 240,
                eraseE = e;
        float chance_eraseD = (minErasedD+maxErasedD)/(2f*dots),
                chance_eraseE = 1f*eraseE/energizers;
        int countD=dots, countE=energizers;
        /*_Log.d("Board reset", "eraseD: "+minErasedD+" - "+maxErasedD+"\n"
                + "eraseE: "+eraseE+"\n"
                + "chance_D: "+chance_eraseD+"\n"
                + "chance_E: "+chance_eraseE+"\n"
                + "count_E: "+countE+"\n"
                + "countD: "+countD);*/
        int offR = Utils.random( (rows-1)/3, (rows-1)/2 ),//Gives more chance to begin erasure from the bottom
                offC = Utils.random(0, cols-1);
        
        float enhancer=1f; //If last was no Dot 1, else: if removed =2 : else =0.5
        
        while (countE>energizers-e || countD>dots-minErasedD ){
            for (int for_r=0; for_r<rows; for_r++){
                int r = (for_r+offR)%rows;
                enhancer=1f;
                for (int for_c=0; for_c<cols; for_c++){
                    int c = (for_c+offC)%cols;
                    Collectible tile = maze[r][c].contains,
                            backup = mazeBackup[r][c].contains;
                    //!Utils.between(countD, dots-maxErasedD, dots-minErasedD)
                    if( backup==Dot && tile==Dot && countD>dots-minErasedD ){
                        if (Utils.chance(chance_eraseD*enhancer) ){
                            maze[r][c].contains = Empty;
                            countD--;
                            enhancer=2.5f;
                        }
                        else
                            enhancer=0f;
                    }
                    else{
                        enhancer=1f;
                        if ( backup==Energizer && tile==Energizer && countE>energizers-e ){
                            if (Utils.chance(chance_eraseE) ){
                                maze[r][c].contains = Empty;
                                countD--;
                                countE--;
                            }
                        }
                    }
                }
            }
        }
        return countD;
    }

    public float computeDotDispersion() {
        float dispersion=0f;
        int dotsCount=0;
        for(int r1=0; r1<rows; r1++){
        for(int c1=0; c1<cols; c1++){
            if(maze[r1][c1].contains==Dot || maze[r1][c1].contains==Energizer){
                dotsCount++;
                for(int r2=0; r2<rows; r2++){
                for(int c2=0; c2<cols; c2++){
                    if(maze[r2][c2].contains==Dot || maze[r2][c2].contains==Energizer){
                        dispersion += Utils.euclidean_dist(c1, r1, c2, r2);
                    }
                }
                }
            }
        }
        }
        return dispersion/(dotsCount*dotsCount);
    }

    private final static int dots_interval[][] = new int[][]{   {0, 75, 150, 175, 200, 225}, 
                                                                {74, 149, 174, 199, 224, 239}
                                                            };
    
    private int getMinD(int d) {
        if(d<0 || d>=dots_interval[0].length)
            throw new IllegalArgumentException("Board reset. d value is not settable: "+d);
        return dots_interval[0][d];
    }

    private int getMaxD(int d) {
        if(d<0 || d>=dots_interval[0].length)
            throw new IllegalArgumentException("Board reset. d value is not settable: "+d);
        return dots_interval[1][d];
    }
}
