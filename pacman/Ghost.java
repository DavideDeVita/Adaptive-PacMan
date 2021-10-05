package pacman;

import java.awt.Color;
import static java.lang.Integer.max;
import static java.lang.Integer.min;
import static pacman.Collectible.OutDoor;
import static pacman.Collectible.Wall;
import static pacman.Direction.*;
import static pacman.GhostPersonalState.*;

/**
 *
 * @author Falie
 */
public class Ghost extends Agent{
    public GhostPersonalState state;
    private final int startAfter_INDEX;
    private Float startAfter_yetToWait=null;
    private final Color color;
    public boolean isFrightened = false, 
            isCruiseElroy=false, hasToChooseDir=true;
    public ScatterTargetGetter scatterTile;
    public ChaseTargetGetter chaseTile;
    public Direction lastDir;
    public State lastTargetGetter=null;

    public Ghost(String name, GameLogic logic, int startX, int startY, int startAfter_INDEX, Direction dir, Color color) {
        super(name, logic, startX, startY, dir);
        lastDir=dir;
        this.color=color;
        this.startAfter_INDEX = startAfter_INDEX;
        //this.startAfter_yetToWait = getStartAfter_fromLvl();
        this.state = (board.elementIn(startX, startY)==Collectible.OutDoor) ? Out : Housed;
    }

    @Override
    public void resetPosition(int coord_x, int coord_y, Direction dir) {
        super.resetPosition(coord_x, coord_y, dir); //To change body of generated methods, choose Tools | Templates.
        this.lastDir=dir;
        this.startAfter_yetToWait = getStartAfter_fromLvl();
        this.state = (board.elementIn(coord_x, coord_y)==Collectible.OutDoor) ? Out : Housed;;
        this.hasToChooseDir=true;
        if(_Log.LOG_ACTIVE) _Log.d("Ghost.java", "Reset "+this+" state");
    }

    @Override
    public void update(float deltaSeconds) {
        if(_Log.LOG_ACTIVE) _Log.d("Ghost Update", this+" Start Update:\t state: "+state + (hasToChooseDir?" has to choose":""));
        if(startAfter_yetToWait==null)
            this.startAfter_yetToWait = getStartAfter_fromLvl();
        if(startAfter_yetToWait>=0){
            startAfter_yetToWait-=deltaSeconds;
            if(!logic.firstLife())
                startAfter_yetToWait-=deltaSeconds;
            if(startAfter_yetToWait<0)
                state=Exiting;
        }
        //if(_Log.LOG_ACTIVE) _Log.d("Ghost Update", (this+" state: "+state+"\t dir: "+dir);
        super.update(deltaSeconds); //To change body of generated methods, choose Tools | Templates.
        if(_Log.LOG_ACTIVE) _Log.d("Ghost Update", this+" end Update\tstate: "+state+"\t dir: "+dir+"\n");
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
        if(dir.isPerpendicularTo(lastDir) && !canTurn90(dir)){
            dir=lastDir;
            hasToChooseDir = true;
            
        }
    }
    
    protected Vector getTarget(){
        Vector target;
        switch ( getGlobalState() ){
            case Chase:
                target =  chaseTile.getChaseTarget(logic, this); //CHASE
                lastTargetGetter=State.Chase;
                return target;
            case Scatter:
            default:
                target = scatterTile.getScatterTarget(logic, this);
                lastTargetGetter=State.Scatter;
                return target;
        }
    }

    private Direction chooseDirection(Vector target) {
        Vector self = tile();
        Direction best=dir;
        double min=Double.MAX_VALUE, currDist;
        if(_Log.LOG_ACTIVE) _Log.d("Ghost Update", this+"Target\t"+(target.x)+" "+(target.y));
        for(Direction d : Direction.values()){
            if(logic.canGo(this, d)){
                currDist = logic.distance(target.x, target.y, self.x+d.x, self.y+d.y);
                if(_Log.LOG_ACTIVE) _Log.d("Ghost Update", "can go "+d+".\t"+(self.x+d.x)+" "+(self.y+d.y)+" dists "+currDist);
                if(currDist<min){
                    min=currDist;
                    best=d;
                }
            }
        }
        if(_Log.LOG_ACTIVE) _Log.d("Ghost Update", "best "+best);
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
            if(options==0){
                _Log.a("Random Error", this+" ("+state+". fright? "+isFrightened+") in "+coordX+" "+coordY+": "+board.elementIn(coordX, coordY)+"\n"
                        + "Up: "+board.elementIn(coordX, coordY-1)+"\n"
                        + "Left: "+board.elementIn(coordX-1, coordY)+"\n"
                        + "Down: "+board.elementIn(coordX, coordY+1)+"\n"
                        + "Right: "+board.elementIn(coordX+1, coordY)+"\n");
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
        int undirectedMovement = (int)( (deltaSeconds+deltaSecondsCarry) * logic.agentSpeed(this));
        //_Log.a(this+" Move", undirectedMovement+" steps");
        if(undirectedMovement<2){
            deltaSecondsCarry+=deltaSeconds;
            return;
        }
        deltaSecondsCarry=0;
        
        int nextX = board.capMovement_X(this, x + dir.x * undirectedMovement ),
                nextY = board.capMovement_Y(this, y + dir.y * undirectedMovement);
        int coord_X = coord_X(), coord_Y = coord_Y(); //Old Coords
        int halfTile_X = board.coord_to_logicalHalfTile_X(coord_X),//.halfTile_X(nextX),
                halfTile_Y = board.coord_to_logicalHalfTile_Y(coord_Y);//.halfTile_Y(nextY);
        switch(dir){
            case Up:
                if( board.elementIn(coord_X, coord_Y-1)==Wall )
                    nextY = max(nextY, halfTile_Y);
                nextX=halfTile_X;
                break;
            case Left:
                if( board.elementIn(coord_X-1, coord_Y)==Wall )
                    nextX = max(nextX, halfTile_X);
                nextY=halfTile_Y;
                break;
            case Down:
                if( board.elementIn(coord_X, coord_Y+1)==Wall )
                    nextY = min(nextY, halfTile_Y);
                nextX=halfTile_X;
                break;
            case Right:
                if( board.elementIn(coord_X+1, coord_Y)==Wall )
                    nextX = min(nextX, halfTile_X);
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

    void setTileGetters(ScatterTargetGetter scatterTile, ChaseTargetGetter chaseTile) {
        this.scatterTile = scatterTile;
        this.chaseTile = chaseTile;
    }
    
    public void setCruiseElroy(boolean isIt){
        this.isCruiseElroy=isIt;
    }
    
    public State getGlobalState(){
        return logic.timerState.state;
    }

    public void turn180() {
        if(_Log.LOG_ACTIVE) _Log.d("Ghost.java", this+" turns 180 dir was "+dir+". lastDir was "+lastDir);
        if(board.elementIn(tile())==OutDoor && dir==Up){
            this.dir=Right;
            this.lastDir=Right;
        }
        else{
            this.dir = dir.opposite();
            this.lastDir = lastDir.opposite();
        }
        this.hasToChooseDir=true;//aggiunto dopo
    }

    public boolean isNotAThreat() {
        return isFrightened || state!=Out;
    }

    @Override
    protected void onResetFromWall() {
        this.hasToChooseDir=true;
        //dir and lastDir can be left like this? suppose so
    }
    
    public void delayedStart(float deltaSeconds){
        startAfter_yetToWait= getStartAfter_fromLvl() - deltaSeconds;
        if(startAfter_yetToWait<0)
            state=Exiting;
    }
    
    private float getStartAfter_fromLvl(){
        return logic.getSetup().get_lvl_stat_index(startAfter_INDEX);
    }
}
