package pacman;

import java.util.LinkedList;
import java.util.Queue;
import static pacman.Collectible.Dot;
import static pacman.Collectible.Energizer;

/**
 *
 * @author Falie
 */
class ChaseTarget_Clyde_chaseAhead extends ChaseTargetGetter{
    protected final int dist2;
    protected final int offset;

    public ChaseTarget_Clyde_chaseAhead(int dist, int off) {
        super();
        this.dist2=dist*dist;
        this.offset = off;
    }

    public ChaseTarget_Clyde_chaseAhead(int dist) {
        super();
        this.dist2=dist*dist;
        this.offset = 0;
    }
    
    protected final Vector offsetPacTile(GameLogic logic){
        Vector ret = new Vector(
                    logic.pacman_coord_X() + (logic.pacman.dir.x*offset),
                    logic.pacman_coord_Y() + (logic.pacman.dir.y*offset)
            );
        if( ret.x >= logic.board.cols )
            ret.x = logic.board.cols-1;
        else if( ret.x < 0 )
            ret.x = 0;
        if( ret.y >= logic.board.rows )
            ret.y = logic.board.rows-1;
        else if ( ret.y < 0 )
            ret.y = 0;
        return ret;
    }

    @Override
    public Vector getChaseTarget(GameLogic logic, Ghost thisGhost) {
        if( Utils.euclidean_dist2(thisGhost, logic.pacman)>dist2 )
            target.reset( offsetPacTile(logic) );
        else
            target.reset( thisGhost.scatterTile.getScatterTarget( logic, thisGhost) );
        return target;
    }
}

public class ChaseTarget_Clyde_Std extends ChaseTarget_Clyde_chaseAhead{
    
    public ChaseTarget_Clyde_Std() {
        super( 8);
    }

    public ChaseTarget_Clyde_Std(int dist) {
        super( dist);
    }
}

 abstract class ChaseTarget_Clyde_closestItem extends ChaseTarget_Clyde_chaseAhead{

    public ChaseTarget_Clyde_closestItem(int dist, int off) {
        super( dist, off);
    }

    public ChaseTarget_Clyde_closestItem(int dist) {
        super( dist);
    }

    @Override
    public Vector getChaseTarget(GameLogic logic, Ghost thisGhost) {
        if( Utils.euclidean_dist2(thisGhost, logic.pacman)>dist2 )
            target.reset( offsetPacTile(logic) );
        else
            target.reset( getNearestItem(logic, thisGhost) );
        return target;
    }

    /**Returns the tile of the nearest dot.
     * It could either be the nearest to the Ghost, or the nearest to Pacman, or the path to go to the nearest to pacman from ghost
     * depends on implementation
     */
    protected abstract Vector getNearestItem(GameLogic logic, Ghost thisGhost);
}

class ChaseTarget_Clyde_PacmanClosestDot extends ChaseTarget_Clyde_closestItem{

    public ChaseTarget_Clyde_PacmanClosestDot(int dist, int off) {
        super(  dist, off);
    }

    public ChaseTarget_Clyde_PacmanClosestDot(int dist) {
        super(  dist);
    }

    @Override
    protected Vector getNearestItem(GameLogic logic, Ghost thisGhost) {
        Queue<Vector> queue = new LinkedList<>();
        Board board = logic.board;
        Vector curr = this.offsetPacTile(logic);
        boolean visited[][] = new boolean[board.rows][board.cols];
        visited[curr.y][curr.x]=true;
        queue.add(curr);
        //BFS
        int r, D=Direction.values().length;
        while(!queue.isEmpty() ){
            curr = queue.poll();
            //if(_Log.LOG_ACTIVE) _Log.d("BFS","examining "+curr.x+" "+curr.y+"\tfrom "+curr.dir);
            Vector vector;
            r=Utils.random(0, 3);
            //for (Direction dir : Direction.values()){
            for (int i=0; i<D; i++){
                Direction dir = Direction.values()[ (i+r)%D ];
                vector = new Vector( board.xFix(curr.x+dir.x), board.yFix(curr.y+dir.y) );
                if( board.elementIn(vector.x, vector.y)==Dot || board.elementIn(vector.x, vector.y)==Energizer ){
                    queue.clear();
                    return vector;
                }
                if( !visited[ vector.y ][ vector.x ] && logic.couldPacGo(curr.x, curr.y, dir) ){//couldGhostGo
                    //couldGhostGo
                    queue.add( vector );
                    visited[ vector.y ][ vector.x ] = true;
                }
            }
        }
        return new Vector(logic.pacman_coord_X(), logic.pacman_coord_Y());//Extremis
    }
}

class ChaseTarget_Clyde_PacmanClosestEnerg extends ChaseTarget_Clyde_closestItem{

    public ChaseTarget_Clyde_PacmanClosestEnerg(int dist, int off) {
        super(  dist, off);
    }

    public ChaseTarget_Clyde_PacmanClosestEnerg(int dist) {
        super(  dist);
    }

    @Override
    protected Vector getNearestItem(GameLogic logic, Ghost thisGhost) {
        if(logic.energizersLeft==0)
            return getNearestDot(logic, thisGhost);
        
        Queue<Vector> queue = new LinkedList<>();
        Board board = logic.board;
        Vector curr = this.offsetPacTile(logic);
        boolean visited[][] = new boolean[board.rows][board.cols];
        visited[curr.y][curr.x]=true;
        queue.add(curr);
        //BFS
        int r, D=Direction.values().length;
        while(!queue.isEmpty() ){
            curr = queue.poll();
            //if(_Log.LOG_ACTIVE) _Log.d("BFS","examining "+curr.x+" "+curr.y+"\tfrom "+curr.dir);
            Vector vector;
            r=Utils.random(0, 3);
            //for (Direction dir : Direction.values()){
            for (int i=0; i<D; i++){
                Direction dir = Direction.values()[ (i+r)%D ];
                vector = new Vector( board.xFix(curr.x+dir.x), board.yFix(curr.y+dir.y) );
                if( board.elementIn(vector.x, vector.y)==Energizer ){
                    queue.clear();
                    return vector;
                }
                if( !visited[ vector.y ][ vector.x ] && logic.couldPacGo(curr.x, curr.y, dir) ){//couldGhostGo
                    //couldGhostGo
                    queue.add( vector );
                    visited[ vector.y ][ vector.x ] = true;
                }
            }
        }
        return new Vector(logic.pacman_coord_X(), logic.pacman_coord_Y());//Extremis
    }
    
    protected Vector getNearestDot(GameLogic logic, Ghost thisGhost) {
        Queue<Vector> queue = new LinkedList<>();
        Board board = logic.board;
        Vector curr = this.offsetPacTile(logic );
        boolean visited[][] = new boolean[board.rows][board.cols];
        visited[curr.y][curr.x]=true;
        queue.add(curr);
        //BFS
        int r, D=Direction.values().length;
        while(!queue.isEmpty() ){
            curr = queue.poll();
            //if(_Log.LOG_ACTIVE) _Log.d("BFS","examining "+curr.x+" "+curr.y+"\tfrom "+curr.dir);
            Vector vector;
            r=Utils.random(0, 3);
            //for (Direction dir : Direction.values()){
            for (int i=0; i<D; i++){
                Direction dir = Direction.values()[ (i+r)%D ];
                vector = new Vector( board.xFix(curr.x+dir.x), board.yFix(curr.y+dir.y) );
                if( board.elementIn(vector.x, vector.y)==Dot || board.elementIn(vector.x, vector.y)==Energizer ){
                    queue.clear();
                    return vector;
                }
                if( !visited[ vector.y ][ vector.x ] && logic.couldPacGo(curr.x, curr.y, dir) ){//couldGhostGo
                    //couldGhostGo
                    queue.add( vector );
                    visited[ vector.y ][ vector.x ] = true;
                }
            }
        }
        return new Vector(logic.pacman_coord_X(), logic.pacman_coord_Y());//Extremis
    }
}