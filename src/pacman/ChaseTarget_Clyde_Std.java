package pacman;

import java.util.LinkedList;
import java.util.Queue;
import static pacman.Collectible.Dot;
import static pacman.Collectible.Energizer;

/**
 *
 * @author Falie
 */
public class ChaseTarget_Clyde_Std extends ChaseTargetGetter{
    protected final int dist2;
    
    public ChaseTarget_Clyde_Std(GameLogic logic) {
        super(logic);
        this.dist2=64;
    }

    public ChaseTarget_Clyde_Std(GameLogic logic, int dist) {
        super(logic);
        this.dist2 = dist*dist;
    }

    @Override
    public Vector getChaseTarget(Ghost thisGhost) {
        if( Utils.euclidean_dist2(thisGhost, logic.pacman)>dist2 )
            target.reset(logic.pacman_coord_X(), logic.pacman_coord_Y());
        else
            target.reset( thisGhost.scatterTile.getScatterTarget(thisGhost) );
        return target;
    }
}

 abstract class ChaseTarget_Clyde_closestDot extends ChaseTarget_Clyde_Std{
     
    public ChaseTarget_Clyde_closestDot(GameLogic logic) {
        super(logic);
    }

    public ChaseTarget_Clyde_closestDot(GameLogic logic, int dist) {
        super(logic, dist);
    }

    @Override
    public Vector getChaseTarget(Ghost thisGhost) {
        if( Utils.euclidean_dist2(thisGhost, logic.pacman)>dist2 )
            target.reset(logic.pacman_coord_X(), logic.pacman_coord_Y());
        else
            target.reset( getNearestDot(thisGhost) );
        return target;
    }

    /**Returns the tile of the nearest dot.
     * It could either be the nearest to the Ghost, or the nearest to Pacman, or the path to go to the nearest to pacman from ghost
     * depends on implementation
     */
    protected abstract Vector getNearestDot(Ghost thisGhost);
}

class ChaseTarget_Clyde_PacmanClosestDot extends ChaseTarget_Clyde_closestDot{

    public ChaseTarget_Clyde_PacmanClosestDot(GameLogic logic, int dist) {
        super(logic, dist);
    }

    public ChaseTarget_Clyde_PacmanClosestDot(GameLogic logic) {
        super(logic);
    }

    @Override
    protected Vector getNearestDot(Ghost thisGhost) {
        Queue<Vector> queue = new LinkedList<>();
        Board board = logic.board;
        Vector curr = new Vector(logic.pacman_coord_X(), logic.pacman_coord_Y());
        boolean visited[][] = new boolean[board.rows][board.cols];
        visited[curr.y][curr.x]=true;
        queue.add(curr);
        //BFS
        int r, D=Direction.values().length;
        while(!queue.isEmpty() ){
            curr = queue.poll();
            //System.out.println("BFS: examining "+curr.x+" "+curr.y+"\tfrom "+curr.dir);
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