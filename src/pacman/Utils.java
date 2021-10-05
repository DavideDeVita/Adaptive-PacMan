package pacman;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import java.util.Random;

/**
 *
 * @author Falie
 */
public class Utils {
    private final static Random R = new Random();
    
    public static int difference(int c, int v){
        return abs(c-v);
    }
    
    public static int count(boolean array[], boolean value){
        int count=0;
        for(int i=0; i<array.length; i++)
            if(array[i]==value)
                count++;
        return count;
    }
    public static int count(boolean array[]){
        return count(array, true);
    }
    
    public static int random(int min, int max){
        if(min==max) return min;
        return R.nextInt(max-min+1)+min;
    }
    public static float random(float min, float max){
        if(min==max) return min;
        return ( R.nextFloat() * max-min )+min;
    }
    public static double random(double min, double max){
        if(min==max) return min;
        return ( R.nextDouble()* max-min )+min;
    }
    
    public static int argRandom(int chances[]){
        int tot=0;
        for( int i=0; i<chances.length; i++)
            tot += chances[i];
        
        int rand = random(0, tot);
        for( int i=0; i<chances.length; i++){
            if(chances[i]==0)   continue;

            rand -= chances[i];
            if(rand <= 0)
                return i;
        }
        throw new IllegalStateException("random from chances found no solution");
    }
    public static int argRandom(float chances[]){
        float tot=0f;
        for( int i=0; i<chances.length; i++){
            tot += chances[i];
        }
        
        float rand = random(0, tot);
        float originalRand = rand;
        for( int i=0; i<chances.length; i++){
            if(chances[i]==0)   continue;

            rand -= chances[i];
            if(rand <= 0)
                return i;
        }
        throw new IllegalStateException("random from chances found no solution: origianl was "+originalRand+". tot was "+tot);
    }
    public static int argRandom(double chances[]){
        double tot=0;
        for( int i=0; i<chances.length; i++)
            tot += chances[i];
        
        double rand = random(0, tot);//-1
        for( int i=0; i<chances.length; i++){
            if(chances[i]==0)   continue;

            rand -= chances[i];
            if(rand <= 0)
                return i;
        }
        _Log.a("ArgRandom", "no solution, chances length: "+chances.length+"\ttot "+tot);
        for(int i=0; i<chances.length; i++)
            _Log.a("ArgRandom", "chances["+i+"] = "+chances[i]);
        throw new IllegalStateException("random from chances found no solution: tot was"+tot+"\n\t");
    }
    public static int argRandom(boolean options[]) {
        int tot=count(options, true);
        
        int rand = random(0, tot);
        for( int i=0; i<options.length; i++){
            if( options[i] ){
                rand--;
                if(rand <= 0)
                    return i;
            }
        }
        throw new IllegalStateException("random from chances found no solution");
    }
    
    public static int argRandom_logged(float chances[]){
        float tot=0f;
        for( int i=0; i<chances.length; i++){
            tot += chances[i];
            _Log.a("tot+="+chances[i]+" = "+tot);
        }
        
        float rand = random(0, tot);
        float originalRand = rand;
        _Log.a("rand is "+rand);
        for( int i=0; i<chances.length; i++){
            if(chances[i]==0)   continue;

            rand -= chances[i];
            if(rand <= 0)
                return i;
        }
        throw new IllegalStateException("random from chances found no solution: origianl was "+originalRand+". tot was "+tot);
    }
    
    public static double[] softmax(int chances[]){
        double softmax[] = new double[ chances.length ], 
                denom=0;
        for( int i=0; i<chances.length; i++){
            softmax[i] = Math.exp(chances[i]);
            denom += softmax[i];
        }
        for( int i=0; i<chances.length; i++)
            softmax[i] /= denom;
        
        return softmax;
    }
    public static double[] softmax(float chances[]){
        double softmax[] = new double[ chances.length ], 
                denom=0;
        for( int i=0; i<chances.length; i++){
            softmax[i] = Math.exp(chances[i]);
            denom += softmax[i];
        }
        for( int i=0; i<chances.length; i++)
            softmax[i] /= denom;
        
        return softmax;
    }
    
    public static int argRandomFromSoftmax(int chances[]){
        double softmax[] = softmax(chances);
        try{    int ret = argRandom(softmax);
                return ret;
        }
        catch(IllegalStateException ise){
            for(int i=0; i<chances.length; i++)
                _Log.a("ArgRandom", "chances["+i+"] = "+chances[i]);
            for(int i=0; i<softmax.length; i++)
                _Log.a("ArgRandom", "softmax["+i+"] = "+softmax[i]);
            throw ise;
        }
    }
    public static int argRandomFromSoftmax(float chances[]){
        double softmax[] = softmax(chances);
        
        return argRandom(softmax);
    }
    
    public static int min(int... args){
        int min = args[0];
        for(int i = 1; i<args.length; i++)
            if (min>args[i])
                min = args[i];
        return min;
    }
    public static int max(int... args){
        int max = args[0];
        for(int i = 1; i<args.length; i++)
            if (max<args[i])
                max = args[i];
        return max;
    }
    public static float min(float... args){
        float min = args[0];
        for(int i = 1; i<args.length; i++)
            if (min>args[i])
                min = args[i];
        return min;
    }
    public static float max(float... args){
        float max = args[0];
        for(int i = 1; i<args.length; i++)
            if (max<args[i])
                max = args[i];
        return max;
    }
    public static double min(double... args){
        double min = args[0];
        for(int i = 1; i<args.length; i++)
            if (min>args[i])
                min = args[i];
        return min;
    }
    public static double max(double... args){
        double max = args[0];
        for(int i = 1; i<args.length; i++)
            if (max<args[i])
                max = args[i];
        return max;
    }
    
    public static float exp(float arg) {
        return (float)Math.exp(arg);
    }
    public static double exp(double arg) {
        return Math.exp(arg);
    }
    
    public static double euclidean_dist(Vector c, Vector d){
        return sqrt( pow(c.x - d.x, 2)+pow(c.y - d.y, 2) );
    }
    public static float euclidean_dist2(Vector c, Vector d){
        return (float)(pow(c.x - d.x, 2) + pow(c.y - d.y, 2) );
    }
    public static float manhattan_distance(Vector c, Vector d){
        return 1f * ( difference(c.x, d.x) + difference(c.y, d.y) );
    }
    
    public static double euclidean_dist(Agent c, Agent d){
        return sqrt( pow(c.coord_X() - d.coord_X(), 2)+pow(c.coord_Y() - d.coord_Y(), 2) );
    }
    public static float euclidean_dist2(Agent c, Agent d){
        if(c==null || d== null)
            return Float.MAX_VALUE;
        return (float)(pow(c.coord_X() - d.coord_X(), 2) + pow(c.coord_Y() - d.coord_Y(), 2) );
    }
    public static float manhattan_distance(Agent c, Agent d){
        if(c==null || d== null)
            return Float.MAX_VALUE;
        return 1f * ( difference(c.coord_X(), d.coord_X()) + difference(c.coord_Y(), d.coord_Y()) );
    }
    
    public static double euclidean_dist(int c_x, int c_y, int d_x, int d_y){
        return sqrt( pow(c_x - d_x, 2)+pow(c_y - d_y, 2) );
    }
    public static float euclidean_dist2(int c_x, int c_y, int d_x, int d_y){
        return (float)( pow(c_x - d_x, 2) + pow(c_y - d_y, 2) );
    }
    public static float toroEuclidean_dist2(int c_x, int c_y, int d_x, int d_y){
        return min(
                euclidean_dist2(c_x, c_y, d_x, d_y),
                euclidean_dist2( min(c_x, d_x)+Board.cols, c_y, max(c_x, d_x), d_y)
        );
    }
    public static float manhattan_distance(int c_x, int c_y, int d_x, int d_y){
        return 1f * ( difference(c_x, d_x) + difference(c_y, d_y) );
    }
    
    public static int x_difference(Agent c, Agent d){
        return abs( c.coord_X() - d.coord_X() );
    }
    public static int y_difference(Agent c, Agent d){
        return abs( c.coord_Y() - d.coord_Y() );
    }

    static int argmax(float[] array) {
        int argmax=-1;
        float max=-Float.MAX_VALUE;
        for (int i=0; i<array.length; i++){
            if( array[i]>max ){
                max = array[i];
                argmax = i;
            }
        }
        return argmax;
    }

    static boolean between(int x, int min, int max) {
        return min<=x && x<=max;
    }

    static boolean chance(float chance) {
        return random(0f, 1f)<=chance;
    }
}
