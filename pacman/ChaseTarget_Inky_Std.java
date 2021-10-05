package pacman;

/**
 *
 * @author Falie
 */
public class ChaseTarget_Inky_Std extends ChaseTargetGetter{
    protected final int offset;
    
    public ChaseTarget_Inky_Std() {
        super();
        offset=2;
    }
    
    public ChaseTarget_Inky_Std(int offset) {
        super();
        this.offset=offset;
    }

    @Override
    public Vector getChaseTarget(GameLogic logic, Ghost thisGhost) {
        Ghost refer = logic.InkyReferenceGhost();
        PacMan p = logic.pacman;
        int offPac_x =  p.coord_X() + p.dir.x*offset;
        int offPac_y =  p.coord_Y() + p.dir.y*offset;
        
        //arcade 8 bit buf
        if( p.dir == Direction.Up ){ offPac_x-=offset; }
        
        int x_difference = offPac_x - refer.coord_X();
        int y_difference = offPac_y - refer.coord_Y();
        
        target.reset(
                x_difference + offPac_x,
                y_difference + offPac_y
        );
        return target;
    }
}

 class ChaseTarget_Inky_BugFix extends ChaseTarget_Inky_Std{
    
    public ChaseTarget_Inky_BugFix() {
        super();
    }
    
    public ChaseTarget_Inky_BugFix(int offset) {
        super( offset);
    }

    @Override
    public Vector getChaseTarget(GameLogic logic, Ghost thisGhost) {
        Ghost refer = logic.InkyReferenceGhost();
        PacMan p = logic.pacman;
        int offPac_x =  p.coord_X() + p.dir.x*offset;
        int offPac_y =  p.coord_Y() + p.dir.y*offset;
        
        int x_difference = offPac_x - refer.coord_X();
        int y_difference = offPac_y - refer.coord_Y();
        
        target.reset(
                x_difference + offPac_x,
                y_difference + offPac_y
        );
        return target;
    }
}

 class ChaseTarget_Inky_Clipped extends ChaseTarget_Inky_Std{
    private final float clip2;
     
    public ChaseTarget_Inky_Clipped(float maxLength) {
        super();
        this.clip2 = maxLength*maxLength;
    }
    
    public ChaseTarget_Inky_Clipped(int offset, float maxLength) {
        super( offset);
        this.clip2 = maxLength*maxLength;
    }

    @Override
    public Vector getChaseTarget(GameLogic logic, Ghost thisGhost) {
        Ghost refer = logic.InkyReferenceGhost();
        PacMan p = logic.pacman;
        int offPac_x =  p.coord_X() + p.dir.x*offset;
        int offPac_y =  p.coord_Y() + p.dir.y*offset;
        
        int x_difference = offPac_x - refer.coord_X();
        int y_difference = offPac_y - refer.coord_Y();
        
        float distance = Utils.euclidean_dist2(offPac_x, offPac_y, refer.coord_X(), refer.coord_Y() );
        float clipper = clip2/distance;
        
        if(clipper<1f){
            target.reset(
                    offPac_x + (int)(x_difference*clipper),
                    offPac_y + (int)(y_difference*clipper)
            );
        }
        else
            target.reset(
                    offPac_x + x_difference,
                    offPac_y + y_difference
            );
        return target;
    }
}