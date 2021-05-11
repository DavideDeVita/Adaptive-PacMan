package pacman;

import java.awt.Color;
import static java.lang.Integer.max;
import static java.lang.Integer.min;
import static pacman.Collectible.Wall;
import static pacman.Direction.*;
import static pacman.GhostPersonalState.*;

/**
 *
 * @author Falie
 */
public class Ghost extends Agent{
    public GhostPersonalState state;
    private final float startAfter;
    private float startAfter_yetToWait;
    private final Color color;
    public boolean isFrightened = false, 
            isCruiseElroy=false, hasToChooseDir=true;
    public ScatterTargetGetter scatterTile;
    public ChaseTargetGetter chaseTile;
    public Direction lastDir;

    public Ghost(String name, GameLogic logic, int startX, int startY, float startAfter, Direction dir, Color color) {
        super(name, logic, startX, startY, dir);
        lastDir=dir;
        this.color=color;
        this.startAfter = this.startAfter_yetToWait = startAfter;
        this.state = (board.elementIn(startX, startY)==Collectible.OutDoor) ? Out : Housed;
    }

    @Override
    public void resetPosition(int coord_x, int coord_y, Direction dir) {
        super.resetPosition(coord_x, coord_y, dir); //To change body of generated methods, choose Tools | Templates.
        this.lastDir=dir;
        this.startAfter_yetToWait = this.startAfter;
        this.state = (board.elementIn(coord_x, coord_y)==Collectible.OutDoor) ? Out : Housed;;
        this.hasToChooseDir=true;
        System.out.println("Reset "+this+" state");
    }

    @Override
    public void update(float deltaSeconds) {
        System.out.println(this+" state: "+state + (hasToChooseDir?" has to choose":""));
        if(startAfter_yetToWait>=0){
            startAfter_yetToWait-=deltaSeconds;
            if(startAfter_yetToWait<0)
                state=Exiting;
        }
        System.out.println(this+" state: "+state+"\t dir: "+dir);
        super.update(deltaSeconds); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void updateDirection(){
        //This is not required until it changes its position
        if(state == Housed){
            if(!logic.canGo(this, dir))
                turn180();
        }
        else{
            Path path = board.pathIn(coord_X(), coord_Y());
            if( hasToChooseDir){
                hasToChooseDir=false;
                if( path.ways()>2 ){
                    Vector target;
                    switch ( state ){
                        case Eaten:
                            target=this.board.getGhostHouse();
                            break;
                        case Out:
                            if( isFrightened ){
                                dir=chooseRandomDirection();
                                return;
                            }
                            target = getTarget();
                            break;
                        case Exiting:
                        default:
                            target=this.board.getOutDoor();
                    }
                    dir = chooseDirection(target);
                }
                else 
                    dir = this.cornerTurn(path);
            }
        }
    }
    
    protected Vector getTarget(){
        Vector target;
        switch ( getGlobalState() ){
            case Chase:
                return chaseTile.getChaseTarget(this); //CHASE
            case Scatter:
            default:
                return scatterTile.getScatterTarget(this);
        }
    }

    private Direction chooseDirection(Vector target) {
        int coordX=coord_X(), coordY=coord_Y();
        Direction best=dir;
        double min=Double.MAX_VALUE, currDist;
        System.out.println(this+"Target\t"+(target.x)+" "+(target.y));
        for(Direction d : Direction.values()){
            if(logic.canGo(this, d)){
                currDist = Utils.euclidean_dist2(target.x, target.y, coordX+d.x, coordY+d.y);
                System.out.println("can go "+d+".\t"+(coordX+d.x)+" "+(coordY+d.y)+" dists "+currDist);
                if(currDist<min){
                    min=currDist;
                    best=d;
                }
            }
        }
        System.out.println("best "+best);
        return best;
    }

    private Direction chooseRandomDirection() {
        int coordX=coord_X(), coordY=coord_Y();
        int options=0, i=0;
        boolean canGo[] = new boolean[Direction.values().length];
        for(Direction d : Direction.values()){
            if(logic.canGo(this, d)){
                options++;
                canGo[i]=true;
            }
            else
                canGo[i]=false;
            i++;
        }
        int r=Utils.random(0, options-1);
        i=0;
        while(r>=0){
            if(canGo[i])
                r--;
            i++;
        }
        return Direction.values()[i-1];
    }

    @Override
    public void updatePosition(float deltaSeconds){
        int undirectedMovement = (int)( deltaSeconds * logic.agentSpeed(this));
        
        if(undirectedMovement==0){
            deltaSecondsCarry+=deltaSeconds;
            return;
        }
        deltaSecondsCarry=0;
        
        int nextX = (x + dir.x * undirectedMovement + board.m_width)%board.m_width,
                nextY = (y + dir.y * undirectedMovement + board.m_height)%board.m_height;
        int coord_X = coord_X(), coord_Y = coord_Y();
        int halfTile_X = board.halfTile_X(nextX),
                halfTile_Y = board.halfTile_Y(nextY);
        switch(dir){
            case Up:
                if( board.elementIn(coord_X, coord_Y-1)==Wall )
                    nextY = min(nextY, halfTile_Y);
                nextX=halfTile_X;
                break;
            case Left:
                if( board.elementIn(coord_X-1, coord_Y)==Wall )
                    nextX = max(nextX, halfTile_X);
                nextY=halfTile_Y;
                break;
            case Down:
                if( board.elementIn(coord_X, coord_Y+1)==Wall )
                    nextY = max(nextY, halfTile_Y);
                nextX=halfTile_X;
                break;
            case Right:
                if( board.elementIn(coord_X+1, coord_Y)==Wall )
                    nextX = max(nextX, halfTile_X);
                nextY=halfTile_Y;
                break;
        }
        
        /**Update last direction ONLY IF the Maze coordinates changed (otherwise, on corners), ghosts can turn 180 anyway.*/
        {   int coordNextX = board.logicToCoordinate_X(nextX),
                coordNextY = board.logicToCoordinate_Y(nextY);
            if(coord_X!=coordNextX || coord_Y!=coordNextY){
                lastDir=dir;
                hasToChooseDir=true;
            }
        }
        this.x=nextX;
        this.y=nextY;
    }

    public Color getColor() {
        if( state==Eaten )
            return logic.eatenColor();
        else if( isFrightened )
            return logic.frightenedColor();
        else
            return color;
    }

    public void setScatterTile(ScatterTargetGetter scatterTile) {
        this.scatterTile = scatterTile;
    }

    public void setChaseTile(ChaseTargetGetter chaseTile) {
        this.chaseTile = chaseTile;
    }
    
    public void setCruiseElroy(boolean isIt){
        this.isCruiseElroy=isIt;
    }
    
    public State getGlobalState(){
        return logic.timerState.state;
    }

    void turn180() {
        System.out.println(this+" turns 180 dir was "+dir+". lastDir was "+lastDir);
        this.dir = dir.opposite();
        this.lastDir = lastDir.opposite();
        this.hasToChooseDir=true;//aggiunto dopo
    }
}
