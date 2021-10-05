package pacman;

/**
 *
 * @author Falie
 */
public abstract class StateObservation{
    protected final float period;
    protected float cumulDeltaTime;

    public StateObservation(float period) {
        this.period = period;
        this.cumulDeltaTime=0F;
    }
    
    public void reset(){
        cumulDeltaTime=0F;
    }
    
    public void log(GameLogic logic, float deltaTime){
        periodicalLog(logic);
        cumulDeltaTime += deltaTime;
        if ( shouldObserve() ){
            occasionalLog(logic);
        }
    }
    
    public boolean shouldObserve() {
        return cumulDeltaTime >= period;
    }
    
    protected static float minGhostDistance(GameLogic logic){
        float min = Float.MAX_VALUE, curr;
        for(int g=0; g<logic.NUM_GHOSTS; g++){
            curr = Utils.euclidean_dist2(logic.pacman, logic.ghosts[g]);
            if ( curr < min )
                min = curr;
        }
        return (float)Math.sqrt(min);
    }

    protected abstract void periodicalLog(GameLogic logic);

    protected abstract void occasionalLog(GameLogic logic);
    
    protected abstract static class Value{
        protected boolean valid=false;
        
        void log(float value){
            valid = true;
        }
        abstract float read();
        void reset(){
            valid=false;
        }
        
        float readIntern(){
            return read();
        }
    }
    protected static final class _LAST extends Value{
        private float value=0f;
        
        @Override public void log(float value){
            super.log(value);
            this.value = value;
        }
        @Override public void reset(){
            super.reset();
            value=0f;
        }
        @Override float read() {
            if (!valid)
                throw new IllegalStateException("_LAST Value requested but never set");
            return value;
        }
    }
    protected static final class _AVG extends Value{
        private float cumulative=0f;
        private int count=0;
        
        @Override public void log(float value){
            super.log(value);
            cumulative+=value;
            count++;
        }
        @Override public void reset(){
            super.reset();
            cumulative=0f;
            count=0;
        }
        @Override float read() {
            if (!valid)
                throw new IllegalStateException("_AVG Value requested but never logged");
            return cumulative/count;
        }
    }
    protected static final class _MIN extends Value{
        private float min=Float.MAX_VALUE;
        
        @Override public void log(float value){
            super.log(value);
            min = (value<min) ? value : min;
        }
        
        @Override public void reset(){
            super.reset();
            min = Float.MAX_VALUE;
        }
        
        @Override public float read(){
            if (!valid)
                throw new IllegalStateException("_MIN Value requested but never logged");
            return min;
        }
    }
    protected static final class _MAX extends Value{
        private float max=Float.MAX_VALUE;
        
        @Override public void log(float value){
            super.log(value);
            max = (value>max) ? value : max;
        }
        
        @Override public void reset(){
            super.reset();
            max = Float.MAX_VALUE;
        }
        
        @Override public float read(){
            if (!valid)
                throw new IllegalStateException("_MAX Value requested but never logged");
            return max;
        }
    }

    /** intervals definisce le soglie:
     * [ 30, 60, 90, 120 ] vuol dire che:
     * <=30 è l'inervallo 0
     * 31-60 è l'intervallo 1
     * 61-90 è l'intervallo 2
     * 91-120 è l'intervallo 3
     * >120 è l'intervallo 4
    */
    protected static class Interval_Value extends Value{
        final Value intern;
        private final float intervals[];

        public Interval_Value(Value intern, float[] intervals) {
            this.intern = intern;
            this.intervals = intervals;
        }

        @Override
        public void log(float value) {
            super.log(value);
            intern.log(value);
        }

        @Override
        float read() {
            if (!valid)
                throw new IllegalStateException("_AVG Value requested but never logged");
            return binarySearch_Index( intern.read() );
        }

        @Override
        public String toString() {
            return read()+"\t("+intern.read()+")";
        }

        @Override
        void reset() {
            intern.reset();
        }

        @Override
        float readIntern() {
            return intern.read();
        }
        
        int binarySearch_Index(float value){
            if (value > intervals[intervals.length-1]){
                //_Log.a("State Obs", value+">"+intervals[intervals.length-1]);
                return intervals.length;
            }
            //else
            return _binarySearch_Index(value, 0, intervals.length-1);
        }
        
        int _binarySearch_Index(float value, int min, int max){
            if(min==max){
                //_Log.a("State Obs", value+"<="+intervals[min]);
                return min;
            }
            //else
            int med_index = (int)((min+max)/2);
            float med = intervals[med_index];
            if( value > med )
                return _binarySearch_Index(value, med_index+1, max);
            else
                return _binarySearch_Index(value, min, med_index);
        }
    }
}

class StateObservation_Player extends StateObservation{
    //Log only before read
    /*
    public Value gamePhase = new Interval_Value( new _LAST(), new float[]{16, 31, 46, 61, 76, 91, 106} ),
            dotsCollected = new Interval_Value( new _LAST(), new float[]{75, 150, 200}),
    */
    public Value gamePhase = new Interval_Value( new _LAST(), new float[]{16, 31, 46, 61} ),
            dotsCollected = new Interval_Value( new _LAST(), new float[]{75, 150, 175, 200, 225}),
            energizersCollected = new Interval_Value( new _LAST(), new float[]{0, 1, 2, 3}),
            livesLeft = new Interval_Value( new _LAST(), new float[]{1, 2});

    public StateObservation_Player(float period) {
        super(period);
        reset();
    }

    @Override public void reset() {
        cumulDeltaTime=0f;
        
        gamePhase.reset();
        energizersCollected.reset();
        dotsCollected.reset();
        livesLeft.reset();
    }
    
    @Override public void log(GameLogic logic, float deltaTime){
        periodicalLog(logic);
        cumulDeltaTime += deltaTime;
        if ( shouldObserve() ){
            occasionalLog(logic);
        }
    }
    
    @Override public void periodicalLog(GameLogic logic){    }
    
    @Override public void occasionalLog(GameLogic logic){
        gamePhase.log( logic.fullGameTimer );
        energizersCollected.log( logic.board.energizers - logic.energizersLeft );
        dotsCollected.log( logic.board.dots - logic.dotsLeft );
        livesLeft.log( logic.pac_lives );
    }

    @Override public boolean shouldObserve() {
        return cumulDeltaTime >= period;
    }
    
    public PlayerState getState(){
        PlayerState ret = new PlayerState(this);
        reset();
        return ret;
    }
    
    @Override
    public String toString(){
        if( shouldObserve() ){
            cumulDeltaTime=0f;
            String ret = "\n\t";
            ret += "gamePhase: "+gamePhase+"\n\t";
            ret += "energizersCollected: "+energizersCollected+"\n\t";
            ret += "dotsCollected: "+dotsCollected+"\n\t";
            ret += "livesLeft: "+livesLeft+"\n\t";
            return ret;
        }
        else return "there is still time\n";
    }
    
    public static class PlayerState{
        public final int p, d, e, l;

        public PlayerState(StateObservation_Player obs) {
            this.p = (int)obs.gamePhase.read();
            //_Log.a("Player State", "P: "+obs.gamePhase.readIntern()+" -> "+p);
            this.d = (int)obs.dotsCollected.read();
            //_Log.a("Player State", "D: "+obs.dotsCollected.readIntern()+" -> "+d);
            this.e = (int)obs.energizersCollected.read();
            //_Log.a("Player State", "E: "+obs.energizersCollected.readIntern()+" -> "+e);
            this.l = (int)obs.livesLeft.read();
            //_Log.a("Player State", "L: "+obs.livesLeft.readIntern()+" -> "+l);
        }

        public PlayerState(int p, int d, int e, int l) {
            this.p = p;
            this.d = d;
            this.e = e;
            this.l = l;
        }

        @Override
        public String toString() {
            return "("+p+","+d+","+e+","+l+")";
        }
    }
}

class StateObservation_Strategies extends StateObservation{
    //Log only before read
    /*
        public Value gamePhase = new Interval_Value( new _LAST(), new float[]{16, 31, 46, 61, 76, 91, 106} ),
            completionRatio = new Interval_Value( new _LAST(), new float[]{75, 150, 200}),
            */
    public Value gamePhase = new Interval_Value( new _LAST(), new float[]{16, 31, 46, 61} ),
            completionRatio = new Interval_Value( new _LAST(), new float[]{75, 150, 175, 200, 225}),
            livesLeft = new Interval_Value( new _LAST(), new float[]{1, 2});

    public StateObservation_Strategies(float period) {
        super(period);
        reset();
    }

    @Override public void reset() {
        super.reset();
        
        livesLeft.reset();
        gamePhase.reset();
        completionRatio.reset();
        //dotsDispersion.reset();
    }
    
    @Override public void periodicalLog(GameLogic logic){  }
    
    @Override public void occasionalLog(GameLogic logic){
        gamePhase.log( logic.fullGameTimer );
        completionRatio.log( logic.board.dots - logic.dotsLeft );
        //dotsDispersion.log( logic.board.computeDotDispersion() );
        livesLeft.log(logic.pac_lives);
    }
    
    public StrategyState getState(){
        StrategyState ret = new StrategyState(this);
        reset();
        return ret;
    }
    
    @Override
    public String toString(){
        if( shouldObserve() ){
            cumulDeltaTime=0f;
            String ret = "\n\t";
            ret += "Lives left: "+livesLeft+"\n\t";
            ret += "gamePhase: "+gamePhase+"\n\t";
            ret += "completionRatio: "+completionRatio+"\n\t";
            //ret += "dotsDispersion: "+dotsDispersion+"\n\t";
            return ret;
        }
        else return "there is still time\n";
    }
    
    public static class StrategyState{
        public final int p, c, l;

        public StrategyState(StateObservation_Strategies obs) {
            this.p = (int)obs.gamePhase.read();
            this.c = (int)obs.completionRatio.read();
            //this.d = (int)obs.dotsDispersion.read();
            this.l = (int)obs.livesLeft.read();
        }

        public StrategyState(int p, int c, int l) {
            this.p = p;
            this.c = c;
            this.l = l;
        }

        @Override
        public String toString() {
            return "("+p+","+c+","+l+")";
        }
    }
}