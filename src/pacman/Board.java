package pacman;

import static pacman.Collectible.*;
import static pacman.Path.*;

/**
 *
 * @author Falie
 */
public class Board {
    Tile[][] maze;
    Vector ghostRespawn = new Vector(14,14), 
            ghostOutDoor = new Vector (11, 14);
    int rows, cols;
    int m_width, m_height;
    
    /**Agents move inside the tile and, then, among tiles
     * i.e. if a unit is at the beginning of a tile and moves of 10 units, it is still in the sam tile.
     * Ricorda di aggiornare i valori degli agenti: se mi muovo in una cella verso l'alto, 
     * dovrò trovarmi al centro (orizzontale) della nuova cella
     */
    public static final int TILE_LOGIC_SIZE = 25;
    
    Board(int[][] file){
        rows=file.length;
        cols=file[0].length;
        
        m_width = cols*TILE_LOGIC_SIZE;
        m_height = rows*TILE_LOGIC_SIZE;
        
        maze = new Tile[rows][cols];
        for (int y=0; y<rows; y++){
            for (int x=0; x<cols; x++){
                maze[y][x] = new Tile();
                switch (file[y][x]){
                    case 1: 
                        maze[y][x].contains=Wall;
                        break;
                    case 9:
                    case 0:
                        maze[y][x].contains=Dot;
                        break;
                    case 3:
                    case 6:
                        maze[y][x].contains=Empty;
                        break;
                    case 8:
                        maze[y][x].contains=Energizer;
                        break;
                    case 7:
                        /*The door to the ghost house:
                        *   PacMan can never go trough
                        *   Ghosts only can while exiting or entering
                        */
                        maze[y][x].contains=Door;
                        break;
                    case 4:
                        /*The toroidal tunnel:
                        *   PacMan can alaways go trough
                        *   Ghosts slow here
                        */
                        maze[y][x].contains=Tunnel;
                        break;
                    case 5:
                        /*The inside of the Ghost house
                        *   If eaten, a ghost here will return alive.. well as much as a ghost can
                        */
                        maze[y][x].contains=House;
                        ghostRespawn.reset(x, y);
                        break;
                    case 2:
                        /*Right outside of the door:
                            Stepping here, for a ghost, changes state from Exiting to Out
                        */
                        maze[y][x].contains=OutDoor;
                        ghostOutDoor.reset(x, y);
                        break;
                }
                maze[y][x].directions = checkDirections(file, y, x);
                if(maze[y][x].directions==Horizontal_Up && (file[y][x]==3 || file[y][x]==9) )
                    maze[y][x].directions=Horizontal_Up_Exc;
                else if( (maze[y][x].directions==Vertical_Sx || maze[y][x].directions==Vertical_Dx)
                        && file[y][x]==7 )
                    maze[y][x].directions=Ghost_Door;
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
}
