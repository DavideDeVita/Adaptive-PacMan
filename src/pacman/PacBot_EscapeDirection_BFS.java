package pacman;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 *
 * @author Falie
 */
public class PacBot_EscapeDirection_BFS extends PacBot_EscapeDirection_GhostDist{

    public PacBot_EscapeDirection_BFS(GameLogic logic) {
        super(logic);
    }

    /*From agent's position to coordX Y +dir*/
    @Override
    protected float computeDistance(int coordX, int coordY, Ghost ghost, Direction d) {
        Queue<Vector> queue = new LinkedList<>();
        Vector curr = new Vector(ghost.coord_X(), ghost.coord_Y()),
                dest = new Vector( board.xFix(coordX+d.x), board.yFix(coordY+d.y) );
        if(_Log.LOG_ACTIVE) _Log.i("PacBot Update", "BFS: "+ghost+" in "+curr.x+" "+curr.y+".\tdest is "+coordX+" "+coordY+" going "+d);
        if(curr.equals(dest)){
            if(_Log.LOG_ACTIVE) _Log.d("PacBot Update", "BFS: same start and destination on "+d);
            return 0;
        }
        Integer dist[][] = new Integer[board.rows][board.cols];
        dist[curr.y][curr.x]=0;
        queue.add(curr);
        //BFS
        int r, D=Direction.values().length;
        //if(_Log.LOG_ACTIVE) _Log.d("PacBot Update", "Start BFS: from "+curr.x+" "+curr.y+"\t to "+dest.x+" "+dest.y);
        while(!queue.isEmpty() ){
            curr = queue.poll();
            //if(_Log.LOG_ACTIVE) _Log.d("PacBot Update", "BFS: examining\t\t"+curr.x+" "+curr.y+"\t dist "+dist[curr.y][curr.x]);
            Vector vector;
            r=Utils.random(0, 3);
            //for (Direction dir : Direction.values()){
            for (int i=0; i<D; i++){
                Direction dir = Direction.values()[ (i+r)%D ];
                vector = new Vector( board.xFix(curr.x+dir.x), board.yFix(curr.y+dir.y) );
                if( dist[ vector.y ][ vector.x ]==null && logic.couldPacGo(curr.x, curr.y, dir) ){//couldGhostGo
                    //if(_Log.LOG_ACTIVE)_Log.d("\tBFS: adj "+vector.x+" "+vector.y+"\t"+dir+" from "+curr.x+" "+curr.y);
                    if( vector.equals(dest) ){ //Ends before
                        if(_Log.LOG_ACTIVE) _Log.i("PacBot Update", "BFS Distance "+ghost+" "+d+" = "+(dist[curr.y][curr.x]+1));
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
        if(_Log.LOG_ACTIVE) _Log.w("PacBot Update", "BFS Distance "+ghost+"("+ghost.state+") "+d+" = default -1\n"
                + "\tagent was in: "+ghost.coord_X()+" "+ghost.coord_Y()+".\t Pac was in "+coordX+" "+coordY);
        return -1;//Extremis
    }
}

class PacBot_EscapeDirection_BFS_n extends PacBot_EscapeDirection_n{
    private Ghost lastG = null;
    private Map<Ghost, Integer[][]> ghosts = new HashMap<>();
    private Map<Ghost, Vector> lastSrc = new HashMap<>();
    private Map<Ghost, Queue<Vector>> queues = new HashMap<>();
    
    public PacBot_EscapeDirection_BFS_n(GameLogic logic, int stepsAhead) {
        super(logic, stepsAhead);
    }

    @Override
    protected float computeDistance(int coordX, int coordY, Ghost ghost, Direction d) {
        Integer dist[][] = null;
        Queue<Vector> queue=null;
        Vector curr = new Vector(ghost.coord_X(), ghost.coord_Y()),
                dest = new Vector( board.xFix(coordX+d.x), board.yFix(coordY+d.y) ),
                    lastGSrc = lastSrc.get(ghost);
        if(_Log.LOG_ACTIVE) _Log.i("PacBot Update", "BFS: "+ghost+" in "+curr.x+" "+curr.y+".\tdest is "+coordX+" "+coordY+" going "+d);
        
        //Dealing with the Map
        if( !ghost.equals(lastG) ){
            dist = ghosts.get(ghost);
            queue = queues.get(ghost);
            //_Log.a(ghost+"", "Getting dist from map: "+(dist==null ? "null" : "[][]"));
            //if(dist == null)
            //    dist = new Integer[board.rows][board.cols];
        }
        
        if(curr.equals(lastGSrc) && dist!=null && dist[dest.y][dest.x]!=null){
            //_Log.a("Easy BFS", ghost+"("+curr.x+" "+curr.y+") already computed: "+dist[dest.y][dest.x]);
            return dist[dest.y][dest.x];
        }
        //else
        //If the current ghost position is different from the last known -> Must reset his Dist[][] and restart BFS
        if( !curr.equals(lastGSrc) || dist==null){
            //_Log.a("Easy BFS", ghost+" is resetting his map: last was "+lastGSrc+"\tnow in "+curr);
            dist = new Integer[board.rows][board.cols];
            if(queue!=null) queue.clear();
            else queue = new LinkedList<>();
        
            dist[curr.y][curr.x]=0;
            queue.add(curr);
            
            if(lastGSrc!=null) lastGSrc.reset(curr);
            else lastGSrc = new Vector(curr.x, curr.y);
            lastG = ghost;
            lastSrc.put(ghost, lastGSrc);
            ghosts.put(ghost, dist);
            queues.put(ghost, queue);
            
            if(curr.equals(dest)){
                if(_Log.LOG_ACTIVE) _Log.d("PacBot Update", "BFS: same start and destination on "+d);
                return 0;
            }
        }
        //BFS
        int r, D=Direction.values().length;
            r=Utils.random(0, 3);
        boolean found=false;
        //if(_Log.LOG_ACTIVE) _Log.d("PacBot Update", "Start BFS: from "+curr.x+" "+curr.y+"\t to "+dest.x+" "+dest.y);
        while(!found && !queue.isEmpty() ){
            curr = queue.poll();
            if(_Log.LOG_ACTIVE) _Log.d("PacBot Update", "BFS: examining\t\t"+curr.x+" "+curr.y+"\t dist "+dist[curr.y][curr.x]);
            Vector vector;
            //for (Direction dir : Direction.values()){
            for (int i=0; i<D; i++){
                Direction dir = Direction.values()[ (i+r)%D ];
                vector = new Vector( board.xFix(curr.x+dir.x), board.yFix(curr.y+dir.y) );
                //_Log.a(""+ghost, "dist is null ? "+(dist==null));
                //_Log.a(""+ghost, "dist[y] is null ? "+(dist[vector.y]==null));
                if( dist[ vector.y ][ vector.x ]==null && logic.couldPacGo(curr.x, curr.y, dir) ){//couldGhostGo
                    //if(_Log.LOG_ACTIVE) _Log.d("\tBFS: adj "+vector.x+" "+vector.y+"\t"+dir+" from "+curr.x+" "+curr.y);
                    queue.add( vector );
                    dist[ vector.y ][ vector.x ] = dist[ curr.y ][ curr.x ] + 1;
                    
                    if( vector.equals(dest) ){ //Ends later
                        if(_Log.LOG_ACTIVE) _Log.i("PacBot Update", "BFS Distance "+ghost+" "+d+" = "+(dist[curr.y][curr.x]+1));
                        found=true;
                    }
                }
            }
        }
        
        if(found){
            ghosts.put(ghost, dist);
            queues.put(ghost, queue);
            return dist[dest.y][dest.x];
        }
        _Log.a("PacBot Update", "BFS Distance "+ghost+"("+ghost.state+") "+d+" = default 0\n"
                + "\t"+ghost+" was in: x "+ghost.coord_X()+"  y "+ghost.coord_Y()+".\t Pac was in x "+logic.pacman_coord_X()+"  y "+logic.pacman_coord_Y()
                + "\t distance of ghost from tile x "+coordX+"  y "+coordY);
        String maze="";
        for(int i=0; i<board.rows; i++){
            for(int j=0; j<board.cols; j++)
                maze += "(y:"+i+" x:"+j+")["+board.pathIn(j, i)+"]"+dist[i][j]+"   ";
            maze+="\n";
        }
        _Log.a("BFS Default", maze);
        return 0;//Extremis
    }
}