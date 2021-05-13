package pacman;

/**
 *
 * @author Falie
 */
public class ChaseTarget_Inky_Std extends ChaseTargetGetter{
    protected final int offset;
    
    public ChaseTarget_Inky_Std(GameLogic logic) {
        super(logic);
        offset=2;
    }
    
    public ChaseTarget_Inky_Std(GameLogic logic, int offset) {
        super(logic);
        this.offset=offset;
    }

    @Override
    public Vector getChaseTarget(Ghost thisGhost) {
        Ghost refer = logic.InkyReferenceGhost();
        PacMan p = logic.pacman;
        int offPac_x =  p.coord_X() + p.dir.x*offset;
        int offPac_y =  p.coord_Y() + p.dir.y*offset;
        
        //arcade 8 bit buf
        if( p.dir == Direction.Up ){ offPac_x-=offset; }
        
        int x_dist = offPac_x - thisGhost.coord_X();
        int y_dist = offPac_y - thisGhost.coord_Y();
        
        target.reset(
                x_dist + offPac_x,
                y_dist + offPac_y
        );
        return target;
    }
}

 class ChaseTarget_Inky_BugFix extends ChaseTarget_Inky_Std{
    
    public ChaseTarget_Inky_BugFix(GameLogic logic) {
        super(logic);
    }
    
    public ChaseTarget_Inky_BugFix(GameLogic logic, int offset) {
        super(logic, offset);
    }

    @Override
    public Vector getChaseTarget(Ghost thisGhost) {
        Ghost refer = logic.InkyReferenceGhost();
        PacMan p = logic.pacman;
        int offPac_x =  p.coord_X() + p.dir.x*offset;
        int offPac_y =  p.coord_Y() + p.dir.y*offset;
        
        int x_dist = offPac_x - thisGhost.coord_X();
        int y_dist = offPac_y - thisGhost.coord_Y();
        
        target.reset(
                x_dist + offPac_x,
                y_dist + offPac_y
        );
        return target;
    }
}