package pacman;

import pacman.StateObservation_Player.PlayerState;
import pacman.StateObservation_Strategies.StrategyState;

/**
 *
 * @author Falie
 */
public class UserScore_Learning {
    private static float[][][][] player_V;
    public static int[][][][] player_N; //Occurrences
    //public final static int P=8, D=4, E=5, L=3;   OLD
    public final static int P=5, D=6, E=5, L=3;
    private static int last_p, last_d, last_e, last_l;
    private static boolean tooSoon;
    private final static float reward_Win=1f, reward_Lose=0f, startValue=0.5f//;
            , reward_nonTerminal=0.06f, value_terminalState=0f; //Default.. the reward is what matter
    private static /*final*/ float alphaNum=100f, gamma=0.95f;
    
    public static void TD_Learning_init(){
        /**Phase, Avg Min Dist, Dots, Energizers, Lives*/
        player_V = new float[P][D][E][L];
        player_N = new int[P][D][E][L];
        for(int p=0; p<P; p++){
        for(int d=0; d<D; d++){
        for(int e=0; e<E; e++){
        for(int l=0; l<L; l++){
            player_V[p][d][e][l]=startValue;
            player_N[p][d][e][l]=0; //ridondante credo
        }
        }
        }
        }
        tooSoon = true;
    }
    public static void TD_Learning_setAlphaGamma(float a, float g){
        /**a lower alphaNum decreases the apha value faster*/
        alphaNum=a;
        gamma=g;
    }
    public static float alpha(int p, int d, int e, int l){
        return alphaNum / (alphaNum - 1f + player_N[p][d][e][l]);
    }
    public static void TD_Learning(PlayerState state){
        /**Phase, Avg Min Dist, Dots, Energizers, Lives*/
        if(tooSoon)
            tooSoon=false;
        else{
            player_N[last_p][last_d][last_e][last_l]++;
            //_Log.a("["+last_p+"]"+"["+last_a+"]"+"["+last_d+"]"+"["+last_e+"]"+"["+last_l+"]++\t"+strategy_N[last_p][last_d][last_e][last_l]);
            player_V[last_p][last_d][last_e][last_l] =
                    player_V[last_p][last_d][last_e][last_l] + 
                    alpha(last_p, last_d, last_e, last_l) 
                     * (  reward_nonTerminal + //
                          (gamma*player_V[state.p][state.d][state.e][state.l])
                           - player_V[last_p][last_d][last_e][last_l]
                        );
            if(_Log.LOG_ACTIVE) _Log.i("TD_Learning", "\tupdate ("+last_p+", "+last_e+", "+last_l+") = "
                    +player_V[last_p][last_d][last_e][last_l]+"\t #"+player_N[last_p][last_d][last_e][last_l]);
        }
        last_p = state.p;
        last_d = state.d;
        last_e = state.e;
        last_l = state.l;
    }
    public static void TD_Learning(int p, int d, int e, int l){
        /**Phase, Avg Min Dist, Dots, Energizers, Lives*/
        if(tooSoon)
            tooSoon=false;
        else{
            player_N[last_p][last_d][last_e][last_l]++;
            //_Log.a("["+last_p+"]"+"["+last_a+"]"+"["+last_d+"]"+"["+last_e+"]"+"["+last_l+"]++\t"+strategy_N[last_p][last_d][last_e][last_l]);
            player_V[last_p][last_d][last_e][last_l] =
                    player_V[last_p][last_d][last_e][last_l] + 
                    alpha(last_p, last_d, last_e, last_l)
                      * (  reward_nonTerminal + //
                           (gamma*player_V[p][d][e][l])
                           - player_V[last_p][last_d][last_e][last_l]
                        );
            if(_Log.LOG_ACTIVE) _Log.i("TD_Learning", "\tupdate ("+last_p+", "+last_e+", "+last_l+") = "
                    +player_V[last_p][last_d][last_e][last_l]+"\t #"+player_N[last_p][last_d][last_e][last_l]);
        }
        last_p = p;
        last_d = d;
        last_e = e;
        last_l = l;
    }
    public static void TD_Learning_Win(){
        /**Phase, Avg Min Dist, Dots, Energizers, Lives*/
        if(!tooSoon){
            player_N[last_p][last_d][last_e][last_l]++;
            player_V[last_p][last_d][last_e][last_l] =
                    player_V[last_p][last_d][last_e][last_l] + 
                    alpha(last_p, last_d, last_e, last_l)*(  reward_Win
                           //+ (gamma*value_terminalState)
                           - player_V[last_p][last_d][last_e][last_l]
                        );
            if(_Log.LOG_ACTIVE) _Log.i("TD_Learning", "\tWin update ("+last_p+", "+last_e+", "+last_l+") = "+
                    player_V[last_p][last_d][last_e][last_l]+"\t #"+player_N[last_p][last_d][last_e][last_l]);
        }
    }
    public static void TD_Learning_Lose(){
        /**Phase, Avg Min Dist, Dots, Energizers, Lives*/
        if(!tooSoon){
            player_N[last_p][last_d][last_e][last_l]++;
            player_V[last_p][last_d][last_e][last_l] =
                    player_V[last_p][last_d][last_e][last_l] + 
                    alpha(last_p, last_d, last_e, last_l)*(  reward_Lose
                           //+ (gamma*value_terminalState)
                           - player_V[last_p][last_d][last_e][last_l]
                        );
            if(_Log.LOG_ACTIVE) _Log.i("TD_Learning", "\tLose update ("+last_p+", "+last_e+", "+last_l+") = "
                    +player_V[last_p][last_d][last_e][last_l]+"\t #"+player_N[last_p][last_d][last_e][last_l]);
        }
    }
    public static void TD_Learning_newStart(){
        /**Phase, Avg Min Dist, Dots, Energizers, Lives*/
        tooSoon = true;
    }
    public static String TD_Learning_read(){
        /**Phase, Avg Min Dist, Dots, Energizers, Lives*/
        String ret="";
        for(int p=0; p<P; p++){
            for(int d=0; d<D; d++){
                for(int e=0; e<E; e++){
                    for(int l=0; l<L; l++){
                        ret+="["+p+"]"+"["+d+"]"+"["+e+"]"+"["+l+"]"+
                                " = "+f(player_V[p][d][e][l])+"\t ("+player_N[p][d][e][l]+" times)\n";
                    }
                    ret+="\n";
                }
                ret+="\n";
            }
            ret+="\n";
        }
        return ret;
    }
    
    
    
    /**Truncates the float to 3 digits max*/
    static String f(float f){
        return ((int)(f*1000))/1000f + "";
    }
}

 class BehavioursScore_Learning {
    private static float[][][][] strategy_V;
    public static int[][][][] strategy_N; //Occurrences
    //public final static int P=8, C=4, L=3;  OLD
    public final static int P=5, C=6, L=3;
    private static int last_p, last_c, last_l;
    private static boolean tooSoon;
    private final static float reward_Win=1f, reward_Lose=0f, startValue=0.5f//;
            , reward_nonTerminal=-0.06f, value_terminalState=0f; //Default.. the reward is what matter
    private static /*final*/ float alphaNum=100f, gamma=0.95f;
    
    public static void TD_Learning_init(int B){
        /**Phase, Avg Min Dist, Dots, Energizers, Lives*/
        strategy_V = new float[B][P][C][L];
        strategy_N = new int[B][P][C][L];
        for(int b=0; b<B; b++){
        for(int p=0; p<P; p++){
        for(int c=0; c<C; c++){
        for(int l=0; l<L; l++){
            strategy_V[b][p][c][l]=startValue;
            strategy_N[b][p][c][l]=0; //ridondante credo
        }
        }
        }
        }
        tooSoon = true;
    }
    public static void TD_Learning_setAlphaGamma(float a, float g){
        /**a lower alphaNum decreases the apha value faster*/
        alphaNum=a;
        gamma=g;
    }
    public static float alpha(int b, int p, int c, int l){
        return alphaNum / (alphaNum - 1f + strategy_N[b][p][c][l]);
    }
    public static void TD_Learning(StrategyState state, int b){
        /**Phase, Avg Min Dist, Dots, Energizers, Lives*/
        if(tooSoon)
            tooSoon=false;
        else{
            strategy_N[b][last_p][last_c][last_l]++;
            //_Log.a("["+last_p+"]"+"["+last_a+"]"+"["+last_d+"]"+"["+last_e+"]"+"["+last_l+"]++\t"+strategy_N[last_p][last_a][last_e][last_l]);
            strategy_V[b][last_p][last_c][last_l] =
                    strategy_V[b][last_p][last_c][last_l] + 
                    alpha(b, last_p, last_c, last_l) 
                     * (  reward_nonTerminal + //
                          (gamma*strategy_V[b][state.p][state.c][state.l])
                           - strategy_V[b][last_p][last_c][last_l]
                        );
            if(_Log.LOG_ACTIVE) _Log.i("TD_Learning", "\tupdate ("+b+", "+last_p+", "+last_c+", "+last_l+") = "
                    +strategy_V[b][last_p][last_c][last_l]+"\t #"+strategy_N[b][last_p][last_c][last_l]);
        }
        last_p = state.p;
        last_c = state.c;
        last_l = state.l;
    }
    public static void TD_Learning(int b, int p, int c, int l){
        /**Phase, Avg Min Dist, Dots, Energizers, Lives*/
        if(tooSoon)
            tooSoon=false;
        else{
            strategy_N[b][last_p][last_c][last_l]++;
            //_Log.a("["+last_p+"]"+"["+last_a+"]"+"["+last_d+"]"+"["+last_e+"]"+"["+last_l+"]++\t"+strategy_N[last_p][last_a][last_e][last_l]);
            strategy_V[b][last_p][last_c][last_l] =
                    strategy_V[b][last_p][last_c][last_l] + 
                    alpha(b, last_p, last_c, last_l) 
                      * (  reward_nonTerminal + //
                           (gamma*strategy_V[b][p][c][l])
                           - strategy_V[b][last_p][last_c][last_l]
                        );
            if(_Log.LOG_ACTIVE) _Log.i("TD_Learning", "\tupdate ("+b+", "+last_p+", "+last_c+", "+last_l+") = "
                    +strategy_V[b][last_p][last_c][last_l]+"\t #"+strategy_N[b][last_p][last_c][last_l]);
        }
        last_p = p;
        last_c = c;
        last_l = l;
    }
    public static void TD_Learning_Win(int b){
        /**Phase, Avg Min Dist, Dots, Energizers, Lives*/
        if(!tooSoon){
            strategy_N[b][last_p][last_c][last_l]++;
            strategy_V[b][last_p][last_c][last_l] =
                    strategy_V[b][last_p][last_c][last_l] + 
                    alpha(b, last_p, last_c, last_l) 
                    *(  reward_Win
                           //+ (gamma*value_terminalState)
                           - strategy_V[b][last_p][last_c][last_l]
                        );
            if(_Log.LOG_ACTIVE) _Log.i("TD_Learning", "\tWin update ("+b+", "+last_p+", "+last_c+", "+last_l+") = "+
                    strategy_V[b][last_p][last_c][last_l]+"\t #"+strategy_N[b][last_p][last_c][last_l]);
        }
    }
    public static void TD_Learning_Lose(int b){
        /**Phase, Avg Min Dist, Dots, Energizers, Lives*/
        if(!tooSoon){
            strategy_N[b][last_p][last_c][last_l]++;
            strategy_V[b][last_p][last_c][last_l] =
                    strategy_V[b][last_p][last_c][last_l] + 
                    alpha(b, last_p, last_c, last_l) 
                    *(  reward_Lose
                           //+ (gamma*value_terminalState)
                           - strategy_V[b][last_p][last_c][last_l]
                        );
            if(_Log.LOG_ACTIVE) _Log.i("TD_Learning", "\tWin update ("+b+", "+last_p+", "+last_l+") = "+
                    strategy_V[b][last_p][last_c][last_l]+"\t #"+strategy_N[b][last_p][last_c][last_l]);
        }
    }
    public static void TD_Learning_newStart(){
        /**Phase, Avg Min Dist, Dots, Energizers, Lives*/
        tooSoon = true;
    }
    public static String TD_Learning_read(int B){
        /**Phase, Avg Min Dist, Dots, Energizers, Lives*/
        String ret="";
        for(int b=0; b<B; b++){
            for(int p=0; p<P; p++){
                for(int c=0; c<C; c++){
                    for(int l=0; l<L; l++){
                                ret+="["+b+"]"+"["+p+"]"+"["+c+"]"+"["+l+"]"+
                                        " = "+f(strategy_V[b][p][c][l])+"\t ("+strategy_N[b][p][c][l]+" times)\n";
                    }
                    ret+="\n";
                }
                ret+="\n";
            }
            ret+="\n";
        }
        return ret;
    }
    
    
    
    /**Truncates the float to 3 digits max*/
    static String f(float f){
        return ((int)(f*1000))/1000f + "";
    }
}