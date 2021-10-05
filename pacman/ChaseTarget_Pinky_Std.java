package pacman;

import static pacman.Direction.*;

/**
 *
 * @author Falie
 */
public class ChaseTarget_Pinky_Std extends ChaseTargetGetter{
    protected final int offset;
    
    public ChaseTarget_Pinky_Std() {
        super();
        offset=4;
    }
    
    public ChaseTarget_Pinky_Std(int offset) {
        super();
        this.offset=offset;
    }

    @Override
    public Vector getChaseTarget(GameLogic logic, Ghost thisGhost) {
        target.reset(
                logic.pacman_coord_X() + logic.pacman.dir.x*offset,
                logic.pacman_coord_Y() + + logic.pacman.dir.y*offset
        );
        //arcade 8 bit buf
        if(logic.pacman.dir == Direction.Up)
            target.x-=offset;
        return target;
    }
}

 class ChaseTarget_Pinky_BugFix extends ChaseTarget_Pinky_Std{

    public ChaseTarget_Pinky_BugFix() {
        super();
    }

    public ChaseTarget_Pinky_BugFix(int offset) {
        super( offset);
    }
    
    @Override
    public Vector getChaseTarget(GameLogic logic, Ghost thisGhost) {
        target.reset(
                logic.pacman_coord_X() + logic.pacman.dir.x*offset,
                logic.pacman_coord_Y() + + logic.pacman.dir.y*offset
        );
        return target;
    }
}

 class ChaseTarget_Pinky_NextIntersection extends ChaseTarget_Pinky_Std{

    public ChaseTarget_Pinky_NextIntersection() {
        super();
    }
    
    @Override
    public Vector getChaseTarget(GameLogic logic, Ghost thisGhost) {
        target.reset( getNextIntersection(logic, thisGhost) );
        return target;
    }

    private Vector getNextIntersection(GameLogic logic, Ghost thisGhost) {
        Vector vec = new Vector(
                logic.pacman.coord_X(),
                logic.pacman.coord_Y());
        Direction dir = logic.pacman.dir;
        Path path = logic.board.pathIn(vec.x, vec.y);
        while (path.ways()==2){
            switch (path){
                case Corner_NW:
                    if(dir==Down)
                        dir=Left;
                    else
                        dir=Up;
                    break;
                case Corner_SW:
                    if(dir==Up)
                        dir=Left;
                    else
                        dir=Down;
                    break;
                case Corner_SE:
                    if(dir==Up)
                        dir=Right;
                    else
                        dir=Down;
                    break;
                case Corner_NE:
                    if(dir==Down)
                        dir=Right;
                    else
                        dir=Up;
                    break;
                case Corridor_V:
                case Corridor_H:
                    break;
            }
            vec.add(dir.x, dir.y);
            path = logic.board.pathIn(vec.x, vec.y);
        }
        //Already on nextIntersection
        if(thisGhost.tile().equals(vec)){
            Direction best=null;
            double min=Double.MAX_VALUE, currDist;
            if(_Log.LOG_ACTIVE) _Log.d("Pinky chase", "Already on next Intersection");
            for(Direction d : Direction.values()){
                if(logic.canGo(thisGhost, d)){
                    currDist = Utils.euclidean_dist2(logic.pacman.x, logic.pacman.y, vec.x+d.x, vec.y+d.y);
                    if(currDist<min){
                        min=currDist;
                        best=d;
                    }
                }
            }
            vec.reset(vec.x+best.x, vec.y+best.y);
        }
        
        return vec;
    }
}