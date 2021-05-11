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
        System.out.println("r: "+min+" "+max);
        return R.nextInt(max-min+1)+min;
    }
    public static float random(float min, float max){
        if(min==max) return min;
        System.out.println("r: "+min+" "+max);
        return ( R.nextFloat() * max-min )+min;
    }
    public static double random(double min, double max){
        if(min==max) return min;
        System.out.println("r: "+min+" "+max);
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
        float tot=0;
        for( int i=0; i<chances.length; i++)
            tot += chances[i];
        
        float rand = random(0, tot);
        for( int i=0; i<chances.length; i++){
            if(chances[i]==0)   continue;

            rand -= chances[i];
            if(rand <= 0)
                return i;
        }
        throw new IllegalStateException("random from chances found no solution");
    }
    public static int argRandom(double chances[]){
        double tot=0;
        for( int i=0; i<chances.length; i++)
            tot += chances[i];
        
        double rand = random(0, tot);
        System.out.println("r: "+rand);
        for( int i=0; i<chances.length; i++){
            if(chances[i]==0)   continue;

            rand -= chances[i];
            if(rand <= 0){
                System.out.println("Returning "+i);
                return i;
            }
        }
        throw new IllegalStateException("random from chances found no solution:\n\t");
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
        
        return argRandom(softmax);
    }
    public static int argRandomFromSoftmax(float chances[]){
        double softmax[] = softmax(chances);
        
        return argRandom(softmax);
    }
    
    public static float min(float a, float b) {
        return Math.min(b, b);
    }
    public static float max(float a, float b) {
        return Math.max(a, b);
    }
    
    public static double euclidean_dist(Vector c, Vector d){
        return sqrt( pow(c.x - d.x, 2)+pow(c.y - d.y, 2) );
    }
    public static float euclidean_dist2(Vector c, Vector d){
        return (float)(pow(c.x - d.x, 2) + pow(c.y - d.y, 2) );
    }
    
    public static double euclidean_dist(Agent c, Agent d){
        return sqrt( pow(c.coord_X() - d.coord_X(), 2)+pow(c.coord_Y() - d.coord_Y(), 2) );
    }
    public static float euclidean_dist2(Agent c, Agent d){
        if(c==null || d== null)
            return Float.MAX_VALUE;
        return (float)(pow(c.coord_X() - d.coord_X(), 2) + pow(c.coord_Y() - d.coord_Y(), 2) );
    }
    
    public static double euclidean_dist(int c_x, int c_y, int d_x, int d_y){
        return sqrt( pow(c_x - d_x, 2)+pow(c_y - d_y, 2) );
    }
    public static float euclidean_dist2(int c_x, int c_y, int d_x, int d_y){
        return (float)( pow(c_x - d_x, 2) + pow(c_y - d_y, 2) );
    }
    
    public static int x_difference(Agent c, Agent d){
        return abs( c.coord_X() - d.coord_X() );
    }
    public static int y_difference(Agent c, Agent d){
        return abs( c.coord_Y() - d.coord_Y() );
    }

    static int argmax(float[] array) {
        int argmax=-1;
        float max=-1;
        for (int i=0; i<array.length; i++){
            if( array[i]>max ){
                max = array[i];
                argmax = i;
            }
        }
        return argmax;
    }
}
