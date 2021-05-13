package pacman;

import java.util.LinkedList;
import java.util.Queue;
import static pacman.GhostPersonalState.*;

/**
 *
 * @author Falie
 */
public abstract class PacBot_EscapeDirection_Getter {
    protected final GameLogic logic;
    protected final Board board;

    public PacBot_EscapeDirection_Getter(GameLogic logic) {
        this.logic = logic;
        this.board = logic.board;
    }
    
    public Direction getEscapeDirection(PacMan_Bot pac) {
        int i=0;
        float min_dists[]= new float[4], curr;
        Ghost ghost;
        
        for(Direction d : Direction.values()){
            min_dists[i] = compute_minGhostDistance(pac.coord_X(), pac.coord_Y(), d);
            i++;
        }
        System.out.println("minDists: "+min_dists[0]+" "+min_dists[1]+" "
                +min_dists[2]+" "+min_dists[3]);
        int argmax = Utils.argmax(min_dists);
        if(argmax!=-1){
            return Direction.values()[ argmax ];
        }
        else{
            return pac.dir;
        }
    }
    
    protected float compute_minGhostDistance(int coordX, int coordY, Direction d){
        float ret, curr;
        Ghost ghost;
        
        if(logic.couldPacGo(coordX, coordY, d)){
            ret=Float.MAX_VALUE;
            for(int g=0; g<logic.ghosts.length; g++){
                ghost = logic.ghosts[g];
                if ( ghost.isAThreat() )
                    continue;//Ignore non dangerous ghosts
                curr = this.computeDistance(coordX, coordY, ghost, d);
                if(curr < ret)
                    ret = curr;
            }
        }
        else
            ret = -1;
        return ret;
    }
    
    /**Computes the distance (according to some metric) from the coordinates x,y moving in direction dir, to agent*/
    protected float computeDistance(PacMan pac, Agent agent, Direction d){
        return this.computeDistance(pac.coord_X(), pac.coord_Y(), agent, d);
    }
    protected abstract float computeDistance(int coordX, int coordY, Agent agent, Direction d);
}

class PacBot_EscapeDirection_BFS extends PacBot_EscapeDirection_Getter{

    public PacBot_EscapeDirection_BFS(GameLogic logic) {
        super(logic);
    }

    @Override
    public float computeDistance(int coordX, int coordY, Agent agent, Direction d) {
        Queue<Vector> queue = new LinkedList<>();
        Vector curr = new Vector(agent.coord_X(), agent.coord_Y()),
                dest = new Vector(coordX+d.x, coordY+d.y);
        Integer dist[][] = new Integer[board.rows][board.cols];
        dist[curr.y][curr.x]=0;
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
                if( dist[ vector.y ][ vector.x ]==null && logic.couldPacGo(curr.x, curr.y, dir) ){//couldGhostGo
                    if( vector.equals(dest) ){ //Ends before
                        return dist[curr.y][curr.x] + 1;
                    }
                    else{
                    //couldGhostGo
                        queue.add( vector );
                        dist[ vector.y ][ vector.x ] = dist[ curr.y ][ curr.x ] + 1;
                    }
                }
            }
        }
        System.out.println("Returning default -1 from BFS escape dir: "+d);
        return -1;//Extremis
    }
}