package pacman;

/**
 *
 * @author Falie
 */
public class PacBot_ScoreBased_W extends PacBot_WanderDirection_Getter{
    private final int stepsAhead;
    private int stepsAhead_curr;
    private boolean dotsEncountered;
    private Direction Dirs[] = Direction.values();
    
    private final static float GHOST=0F, NEAR_GHOST=1F, NULL=0.5F, EMPTY=3F, TUNNEL=3f, DOT=10F, ENERG=9F;
    
    public PacBot_ScoreBased_W(GameLogic logic, int stepsAhead) {
        super(logic);
        this.stepsAhead=stepsAhead;
        this.stepsAhead_curr=stepsAhead;
    }

    @Override
    public Direction getWanderDirection(PacMan_Bot pac) {
        int coordX = pac.coord_X(),
                coordY = pac.coord_Y();
        Path path = board.pathIn(coordX, coordY);
        if( path.ways()>2 ){
            //long start = System.nanoTime();
            float scorePerDir[] = new float[Dirs.length],
                    min=Float.MAX_VALUE;
            boolean availableDirections[] = new boolean[Dirs.length];
            dotsEncountered=false;
            for(int i=0; i<Dirs.length; i++){
                if(logic.canGo(pac, Dirs[i])){
                    availableDirections[i]=true;
                    scorePerDir[i] = computeScore(coordX, coordY, Dirs[i], stepsAhead_curr-1);
                    if(scorePerDir[i]<min) 
                        min = scorePerDir[i];
                    //_Log.a("Score Based W", dir+": "+scorePerDir[i]);
                }
                else{
                    scorePerDir[i]=-1F;
                    availableDirections[i]=false;
                }
            }
            if(!dotsEncountered)
                stepsAhead_curr+=5;
            else
                stepsAhead_curr=stepsAhead;

            float rescale = min==0 ? 1F : min;//to avoid overflow
            
            //unavailable Directions must not be considered by softmax
            int available = Utils.count(availableDirections);
            if(available==0)
                _Log.a("Score Escape", "0 available directions in "+coordX+" "+coordY);
            float dotPerDirection_trimmed[] = new float[ Utils.count(availableDirections) ];
            int trim_i=0;
            for (int i=0; i<scorePerDir.length; i++){
                if(availableDirections[i]){
                    dotPerDirection_trimmed[trim_i++]=scorePerDir[i]/rescale;
                }
            }

            try{
                trim_i = Utils.argRandomFromSoftmax(dotPerDirection_trimmed);
            }
            catch (IllegalStateException ise){
                String exc = "";
                for (int j=0; j<dotPerDirection_trimmed.length; j++)
                    exc+=dotPerDirection_trimmed[j]+" ";
                _Log.a("Score Escape", ise.getMessage()+exc);
                return pac.dir;
            }
                
            for (int i=0; i<scorePerDir.length; i++){
                if(!availableDirections[i])
                    continue;
                trim_i--;
                if(trim_i<0){
                    if(_Log.LOG_ACTIVE) _Log.d("PacBot Update", "Returning dir "+i);
                    return Dirs[i];
                }
            }
            //_Log.a("Escape Score", "Time required: "+(System.nanoTime()-start)/1_000_000_000F+"s");
        }
        else
            return pac.cornerTurn(path);
        return pac.dir;
    }

    private int encountered;
    private float computeScore(int coordX, int coordY, Direction dir, int stepsLeft) {
        float subScore=0F;
        int newX=board.xFix(coordX+dir.x), 
                newY=board.yFix(coordY+dir.y);
        encountered=1;
        if(logic.couldPacGo(coordX, coordY, dir)){
            subScore += tileScore(newX, newY);// * (1+stepsLeft);
            
            if(stepsLeft>0){
                for(Direction d : Dirs)
                    if( d!= dir.opposite())
                        subScore+=_computeScore(newX, newY, d, stepsLeft-1);
            }
        }
        //if(_Log.LOG_ACTIVE) _Log.d("PacBot Update", "subCountDots: "+newX+" "+newY+" "+dir+" "+stepsLeft+" = "+subCount);
        return subScore/encountered;
    }

    private float _computeScore(int coordX, int coordY, Direction dir, int stepsLeft) {
        float subScore=0F;
        int newX=board.xFix(coordX+dir.x), 
                newY=board.yFix(coordY+dir.y);
        
        if(logic.couldPacGo(coordX, coordY, dir)){
            subScore += tileScore(newX, newY);// * (1+stepsLeft);
            encountered++;
            if(stepsLeft>0){
                for(Direction d : Dirs)
                    if( d!= dir.opposite())
                        subScore+=_computeScore(newX, newY, d, stepsLeft-1);
            }
        }
        //if(_Log.LOG_ACTIVE) _Log.d("PacBot Update", "subCountDots: "+newX+" "+newY+" "+dir+" "+stepsLeft+" = "+subCount);
        return subScore;
    }
    
    private float tileScore(int coordX, int coordY){
        if( isGhost(coordX, coordY) )
            return GHOST;
        if( isNearGhost(coordX, coordY) )
            return NEAR_GHOST;
        
        switch( logic.board.elementIn(coordX, coordY) ){
            case Dot:
                dotsEncountered=true;
                return DOT;
            case Energizer:
                dotsEncountered=true;
                return ENERG;
            case Fruit:
                return EMPTY;
            case Tunnel:
                return TUNNEL;
            case OutDoor:
            case Empty:
                return EMPTY;
            case Wall:
            case House:
            case Door:
            default:
                return NULL;
        }
    }

    private boolean isGhost(int coordX, int coordY) {
        for (int g=0; g<logic.ghosts.length; g++)
            if( logic.ghosts[g].coord_X()==coordX && logic.ghosts[g].coord_Y()==coordY )
                return true;
        return false;
    }

    private boolean isNearGhost(int coordX, int coordY) {
        for (int g=0; g<logic.ghosts.length; g++)
            for (int d=0; d<Dirs.length; d++)
                if( logic.ghosts[g].coord_X()==coordX+Dirs[d].x && logic.ghosts[g].coord_Y()==coordY+Dirs[d].y )
                    return true;
        return false;
    }
}

 class PacBot_ScoreBased_E extends PacBot_EscapeDirection_Getter{
    private final int stepsAhead;
    private int stepsAhead_curr;
    private boolean dotsEncountered;
    private Direction Dirs[] = Direction.values();
    
    private final static float GHOST=0F, NEAR_GHOST=0.5F, NULL=0.5F, EMPTY=5F, TUNNEL=7f, DOT=9F, ENERG=12F;
    
    public PacBot_ScoreBased_E(GameLogic logic, int stepsAhead) {
        super(logic);
        this.stepsAhead=stepsAhead;
        this.stepsAhead_curr=stepsAhead;
    }

    @Override
    public Direction getEscapeDirection(PacMan_Bot pac) {
        int coordX = pac.coord_X(),
                coordY = pac.coord_Y();
        Path path = board.pathIn(coordX, coordY);
        if( path.ways()>2 ){
            //long start = System.nanoTime();
            float scorePerDir[] = new float[Dirs.length],
                    min=Float.MAX_VALUE;
            boolean availableDirections[] = new boolean[Dirs.length];
            dotsEncountered=false;
            for(int i=0; i<Dirs.length; i++){
                if(logic.canGo(pac, Dirs[i])){
                    availableDirections[i]=true;
                    scorePerDir[i] = computeScore(coordX, coordY, Dirs[i], stepsAhead_curr-1);
                    if(scorePerDir[i]<min) 
                        min = scorePerDir[i];
                    //_Log.a("Score Based E", dir+": "+scorePerDir[i]);
                }
                else{
                    scorePerDir[i]=-1f;
                    availableDirections[i]=false;
                }
            }
            if(!dotsEncountered)
                stepsAhead_curr+=5;
            else
                stepsAhead_curr=stepsAhead;
            
            if (!availableDirections[Utils.argmax(scorePerDir)]){
                String why="\n\t"+scorePerDir[0]+"\t"+scorePerDir[1]+"\t"+scorePerDir[2]+"\t"+scorePerDir[3]+"\n";
                _Log.a("Score Escape", "argmax is not available:");
            }
            return Dirs[ Utils.argmax(scorePerDir) ];
            /*float rescale = min==0 ? 1F : min;//to avoid overflow
            
            //unavailable Directions must not be considered by softmax
            int available = Utils.count(availableDirections);
            if(available==0)
                _Log.a("Score Escape", "0 available directions in "+coordX+" "+coordY);
            float dotPerDirection_trimmed[] = new float[ Utils.count(availableDirections) ];
            int trim_i=0;
            for (i=0; i<scorePerDir.length; i++){
                if(availableDirections[i]){
                    dotPerDirection_trimmed[trim_i++]=scorePerDir[i]/rescale;
                }
            }

            try{
                trim_i = Utils.argRandomFromSoftmax(dotPerDirection_trimmed);
            }
            catch (IllegalStateException ise){
                String exc = "";
                for (int j=0; j<dotPerDirection_trimmed.length; j++)
                    exc+=dotPerDirection_trimmed[j]+" ";
                _Log.a("Score Escape", ise.getMessage()+exc);
                return pac.dir;
            }
                
            for (i=0; i<scorePerDir.length; i++){
                if(!availableDirections[i])
                    continue;
                trim_i--;
                if(trim_i<0){
                    if(_Log.LOG_ACTIVE) _Log.d("PacBot Update", "Returning dir "+i);
                    return Dirs[i];
                }
            }
            //_Log.a("Escape Score", "Time required: "+(System.nanoTime()-start)/1_000_000_000F+"s");
                    */
        }
        else
            return pac.cornerTurn(path);
    }

    private int encountered;
    private float computeScore(int coordX, int coordY, Direction dir, int stepsLeft) {
        float subScore=0F;
        int newX=board.xFix(coordX+dir.x), 
                newY=board.yFix(coordY+dir.y);
        encountered=1;
        if(logic.couldPacGo(coordX, coordY, dir)){
            subScore += tileScore(newX, newY);// * (1+stepsLeft);
            
            if(stepsLeft>0){
                for(Direction d : Dirs)
                    if( d!= dir.opposite())
                        subScore+=_computeScore(newX, newY, d, stepsLeft-1);
            }
        }
        //if(_Log.LOG_ACTIVE) _Log.d("PacBot Update", "subCountDots: "+newX+" "+newY+" "+dir+" "+stepsLeft+" = "+subCount);
        return subScore/encountered;
    }

    private float _computeScore(int coordX, int coordY, Direction dir, int stepsLeft) {
        float subScore=0F;
        int newX=board.xFix(coordX+dir.x), 
                newY=board.yFix(coordY+dir.y);
        
        if(logic.couldPacGo(coordX, coordY, dir)){
            subScore += tileScore(newX, newY);// * (1+stepsLeft);
            encountered++;
            if(stepsLeft>0){
                for(Direction d : Dirs)
                    if( d!= dir.opposite())
                        subScore+=_computeScore(newX, newY, d, stepsLeft-1);
            }
        }
        //if(_Log.LOG_ACTIVE) _Log.d("PacBot Update", "subCountDots: "+newX+" "+newY+" "+dir+" "+stepsLeft+" = "+subCount);
        return subScore;
    }
    
    private float tileScore(int coordX, int coordY){
        if( isGhost(coordX, coordY) )
            return GHOST;
        if( isNearGhost(coordX, coordY) )
            return NEAR_GHOST;
        
        switch( logic.board.elementIn(coordX, coordY) ){
            case Dot:
                dotsEncountered=true;
                return DOT;
            case Energizer:
                dotsEncountered=true;
                return ENERG;
            case Fruit:
                return EMPTY;
            case Tunnel:
                return TUNNEL;
            case OutDoor:
            case Empty:
                return EMPTY;
            case Wall:
            case House:
            case Door:
            default:
                return NULL;
        }
    }

    private boolean isGhost(int coordX, int coordY) {
        for (int g=0; g<logic.ghosts.length; g++)
            if( logic.ghosts[g].coord_X()==coordX && logic.ghosts[g].coord_Y()==coordY )
                return true;
        return false;
    }

    private boolean isNearGhost(int coordX, int coordY) {
        for (int g=0; g<logic.ghosts.length; g++)
            for (int d=0; d<Dirs.length; d++)
                if( logic.ghosts[g].coord_X()==coordX+Dirs[d].x && logic.ghosts[g].coord_Y()==coordY+Dirs[d].y )
                    return true;
        return false;
    }
}