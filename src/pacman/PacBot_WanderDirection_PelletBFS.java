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

    private void BFS_pellet(int coordX, int coordY) {
        // Class definition
        class BFS_Queue_Node{
            final int x, y;
            final Direction dir;

            public BFS_Queue_Node(int x, int y, Direction dir) {
                this.x = x;
                this.y = y;
                this.dir = dir;
            }
        }
        //init
        Queue<BFS_Queue_Node> queue = new LinkedList<>();
            //System.out.println("BFS: start "+coordX+" "+coordY);
        Direction pred[][] = new Direction[board.rows][board.cols];
        pred[coordY][coordX]=DEFAULT;//Just to stop the backtracking
        //for (Direction dir : Direction.values()){
        int r=Utils.random(0, 3), D = Direction.values().length;
        for (int i=0; i<D; i++){
            Direction dir = Direction.values()[ (i+r)%D ];
            if( logic.couldPacGo(coordX, coordY, dir) ){
                queue.add( new BFS_Queue_Node(coordX+dir.x, coordY+dir.y, dir) );
            System.out.println("BFS: first gen "+(coordX+dir.x)+" "+(coordY+dir.y)+"\tfrom "+dir);
                pred[ board.yFix(coordY+dir.y) ][ board.xFix(coordX+dir.x) ] = dir;
            }
        }
        //BFS
        boolean found=false;
        int tileX = coordX, tileY = coordY; //unused
        while(!found /*&& !queue.isEmpty()*/ ){ //Should never be empty without finding
            BFS_Queue_Node curr = queue.poll();
            //System.out.println("BFS: examining "+curr.x+" "+curr.y+"\tfrom "+curr.dir);
            Collectible element = board.elementIn(curr.x, curr.y);
            if( element == Collectible.Dot || element==Collectible.Energizer ){
                found=true;
                tileX = curr.x;
                tileY = curr.y;
            }
            else{
                r=Utils.random(0, 3);
                //for (Direction dir : Direction.values()){
                for (int i=0; i<D; i++){
                    Direction dir = Direction.values()[ (i+r)%D ];
                        if( pred[ board.yFix(curr.y+dir.y) ][ board.xFix(curr.x+dir.x) ]==null &&
                            logic.couldPacGo(curr.x, curr.y, dir) ){
                        queue.add( new BFS_Queue_Node(curr.x+dir.x, curr.y+dir.y, dir) );
                        pred[board.yFix(curr.y+dir.y) ][ board.xFix(curr.x+dir.x)] = dir;
                    }
                }
            }
        }
        queue.clear();
        
        Direction dir;
        //System.out.println("BFS: backtracking init: we re in "+tileX+" "+tileY+"\tend in "+coordX+" "+coordY);
        while (tileX!=coordX || tileY!=coordY){
            dir = pred[ board.yFix(tileY) ][ board.xFix(tileX) ];
            //System.out.println("BFS: backtracking "+tileX+" "+tileY+"\tfrom "+dir);
            path.addFirst( dir );
            tileX += dir.opposite().x;
            tileY += dir.opposite().y;
        }
    }
}
