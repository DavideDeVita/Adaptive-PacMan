package pacman;

import java.util.LinkedList;
import java.util.Queue;
/**
 *
 * @author Falie
 */
public class PacBot_WanderDirection_PelletBFS extends PacBot_WanderDirection_Getter{
    protected LinkedList<Direction> path = new LinkedList<>();
    private Direction DEFAULT = Direction.Up;
    private final static Direction dirs[] = Direction.values();
    
    public PacBot_WanderDirection_PelletBFS(GameLogic logic) {
        super(logic);
    }

    @Override
    public Direction getWanderDirection(PacMan_Bot pac) {
        int coordX = pac.coord_X(),
                coordY = pac.coord_Y();
        if (pac.lastState!=PacMan_Bot.State.Wander)
            path.clear();
        if ( path.isEmpty() )
            BFS_pellet(coordX, coordY);

        return path.removeFirst();
    }

    private void BFS_pellet(final int startX, final int startY) {
        // Class definition
        class BFS_Queue_Node{
            final Vector vec;
            final Direction dir;

            public BFS_Queue_Node(int x, int y, Direction dir) {
                this.vec = new Vector(x, y);
                this.dir = dir;
            }
        }
        //init
        Queue<BFS_Queue_Node> queue = new LinkedList<>();
            //if(_Log.LOG_ACTIVE) _Log.d("PacBot Update", "BFS: start "+coordX+" "+coordY);
        Direction pred[][] = new Direction[board.rows][board.cols];
        pred[startY][startX]=DEFAULT;//Just to stop the backtracking
        //for (Direction dir : dirs){
        int r=Utils.random(0, 3), D = dirs.length;
        int tileX = startX, tileY = startY; //unused
        for (int i=0; i<D; i++){
            Direction dir = dirs[ (i+r)%D ];
            tileX = board.xFix( startX+dir.x );
            tileY = board.yFix( startY+dir.y );
            if( logic.couldPacGo(startX, startY, dir) ){
                queue.add( new BFS_Queue_Node(tileX, tileY, dir) );
                if(_Log.LOG_ACTIVE) _Log.d("PacBot Update", "BFS: first gen "+tileX+" "+tileY+"\tfrom "+dir);
                pred[ tileY ][ tileX ] = dir;
            }
        }
        //BFS
        boolean found=false;
        BFS_Queue_Node curr;
        Vector vec, nextVec = new Vector(0, 0);
        while(!found /*&& !queue.isEmpty()*/ ){ //Should never be empty without finding
            curr = queue.poll();
            vec = curr.vec;
            //if(_Log.LOG_ACTIVE) _Log.d("PacBot Update", "BFS: examining "+curr.x+" "+curr.y+"\tfrom "+curr.dir);
            Collectible element = board.elementIn(vec.x, vec.y);
            if( element == Collectible.Dot || element==Collectible.Energizer ){
                found=true;
                tileX = vec.x;
                tileY = vec.y;
            }
            else{
                r=Utils.random(0, 3);
                //for (Direction dir : dirs){
                for (int i=0; i<D; i++){
                    Direction dir = dirs[ (i+r)%D ];
                    nextVec.reset( board.xFix(vec.x+dir.x), board.yFix(vec.y+dir.y) );
                    if( pred[ nextVec.y ][ nextVec.x ]==null &&
                            logic.couldPacGo(vec.x, vec.y, dir) ){
                        queue.add( new BFS_Queue_Node(nextVec.x, nextVec.y, dir) );
                        pred[ nextVec.y ][ nextVec.x ] = dir;
                    }
                }
            }
        }
        queue.clear();
        
        Direction dir;
        if(_Log.LOG_ACTIVE) _Log.d("PacBot Update", "BFS: backtracking init: we re in "+tileX+" "+tileY+"\tend in "+startX+" "+startY);
        while (tileX!=startX || tileY!=startY){
            dir = pred[ tileY ][ tileX];
            if(_Log.LOG_ACTIVE) _Log.d("PacBot Update", "BFS: backtracking "+tileX+" "+tileY+"\tgot going "+dir);
            path.addFirst( dir );
            tileX = board.xFix(tileX + dir.opposite().x);
            tileY = board.yFix(tileY + dir.opposite().y);
        }
    }

    @Override
    void reset() {
        path.clear();
    }
}
