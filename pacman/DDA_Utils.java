package pacman;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javafx.util.Pair;
import pacman.StateObservation_Player.PlayerState;
import pacman.StateObservation_Strategies.StrategyState;

/**
 *
 * @author Falie
 */
public class DDA_Utils {
    private final static String userScoreFilename = "src\\pacman\\userScore.txt",
            behaviourScoreTabularFilename = "src\\pacman\\familiesScore_tab.txt",
            behaviourScoreSingleValueFilename = "src\\pacman\\familiesScore_SBS.txt";
    private static BufferedReader br;
    private static double[][][][] userEval;
    private static Family families[];
    private static final Policy policy_SBS = new SmoothTransition_BtoU_Policy(0.0775),      //SBS
        policy_T = new SmoothTransition_BtoU_Policy(0.0225),                                //T
            policy = (Game_PacMan.adaptation == Game_PacMan.AdaptationType.T) ? policy_T : policy_SBS;
    
    private final static double emph_F = 16.*10., emph_B = 67;//8 * same as Families
    private final static int adapts_per_call = 5;
    public static int makeHarder=0, makeEasier=0;
    
    public static double[][][][] loadUserEval(){
        userEval = new double[UserScore_Learning.P][UserScore_Learning.D][UserScore_Learning.E][UserScore_Learning.L];
        int p=0, d=0, e=0, l=0;
        try {
            br = new BufferedReader(new FileReader(userScoreFilename));
            String line = br.readLine();
            while (line != null) {
                String[] splitLine = line.split(":");
                double score = (double)Double.parseDouble(splitLine[splitLine.length-1]);
                userEval[p][d][e][l] = score;
                //Update pdel
                l++;
                if(l==UserScore_Learning.L){
                    l=0;
                    e++;
                    if(e==UserScore_Learning.E){
                        e=0;
                        d++;
                        if(d==UserScore_Learning.D){
                            d=0;
                            p++;
                        }
                    }
                }
                
                // read next line
                line = br.readLine();
            }
            br.close();
        } catch (FileNotFoundException ex) {
            _Log.a(ex.getMessage());
        } catch (IOException ex) {
            _Log.a(ex.getMessage());
        }
        
        return userEval;
    }
    
    public static Family[] loadFamilies_tabular(Pair<Object, String> objs[][], Adapter adapters[]){
        families = new Family[Game_PacMan.FAMILIES];
        int b=0, p=0, d=0, l=0;
        double behEval[][][][] = new double[Game_PacMan.BEHAVIOURS][BehavioursScore_Learning.P][BehavioursScore_Learning.C][BehavioursScore_Learning.L];
        try {
            br = new BufferedReader(new FileReader(behaviourScoreTabularFilename));
            String line = br.readLine();
            while (line != null) {
                String[] splitLine = line.split(":");
                double score = (double)Double.parseDouble(splitLine[splitLine.length-1]);
                behEval[b][p][d][l] = score;
                //Update pdel
                l++;
                if(l==UserScore_Learning.L){
                    l=0;
                    d++;
                    if(d==UserScore_Learning.D){
                        d=0;
                        p++;
                        if(p==BehavioursScore_Learning.P){
                            p=0;
                            b++;
                        }
                    }
                }
                
                // read next line
                line = br.readLine();
            }
            br.close();
            
            int counter=1;
            for(int f=0; f<adapters.length; f++){
                Behaviour behs[] = new Behaviour[objs[f].length];
                behs[0] = new Behaviour_Tabular(objs[f][0], behEval[0]);
                for(int i=1; i<objs[f].length; i++){
                    //_Log.a("Parsing F", "f:"+f+" b:"+i+" is "+objs[f][i].getValue());
                    behs[i] = new Behaviour_Tabular(objs[f][i], behEval[counter]);
                    counter++;
                }
                families[f] = new Family(behs, adapters[f]);
            }
            
        } catch (FileNotFoundException ex) {
            _Log.a(ex.getMessage());
        } catch (IOException ex) {
            _Log.a(ex.getMessage());
        }
        
        return families;
    }
    
    public static Family[] loadFamilies_singleValue(Pair<Object, String> objs[][], Adapter adapters[]){
        families = new Family[Game_PacMan.FAMILIES];
        int b=0;
        double behEval[] = new double[Game_PacMan.BEHAVIOURS];
        try {
            br = new BufferedReader(new FileReader(behaviourScoreSingleValueFilename));
            String line = br.readLine();
            while (line != null) {
                String[] splitLine = line.split(":");
                double score = (double)Double.parseDouble(splitLine[splitLine.length-1]);
                behEval[b] = score;
                b++;
                
                // read next line
                line = br.readLine();
            }
            br.close();
            
            int counter=0;
            for(int f=0; f<adapters.length; f++){
                Behaviour behs[] = new Behaviour[objs[f].length];
                for(int i=0; i<objs[f].length; i++){
                    //_Log.a("Parsing F", "f:"+f+" b:"+i+" is "+objs[f][i].getValue());
                    behs[i] = new Behaviour_SingleScore(objs[f][i], behEval[counter]);
                    //_Log.a("Reading Scores", objs[f][i].getValue()+" is "+counter+"-th: "+behEval[counter]);
                    counter++;
                }
                families[f] = new Family(behs, adapters[f]);
            }
            
        } catch (FileNotFoundException ex) {
            _Log.a(ex.getMessage());
        } catch (IOException ex) {
            _Log.a(ex.getMessage());
        }
        
        return families;
    }
    
    public static void adapt(GameLogic logic, PlayerState pState, StrategyState sState, int setup[]){
        _Log.a("Adapt", "Start of adapt for playerState:"+pState+"\tsState:"+sState);
        double oldSetupScore = setupScore(setup, sState);
        _Log.a("Old Setup", "userScore is "+userScore(pState)+"\t setupScore is "+oldSetupScore);
        for(int i=0; i<adapts_per_call; i++){
            Substitution subs = _adapt(pState, sState, setup);
            if(subs==null){
                _Log.a("Adapt", "No adaptation possible");
                continue;
            }
            families[subs.f].adapt(logic, subs.b);
            //_Log.a("Adapt", "Removing "+families[subs.f].behaviours[setup[subs.f]]+" and replacing with "+families[subs.f].behaviours[subs.b]);
            setup[subs.f]=subs.b;
        }
        _Log.a("New Setup", "userScore is "+userScore(pState)+"\t setupScore is "+setupScore(setup, sState)+" from "+oldSetupScore+"\n"
                + "\tplayerState:"+pState+"\tsState:"+sState);
        _Log.a("New Setup", setupToString(setup));
    }
    
    public static Substitution _adapt(PlayerState pState, Object sState, int setup[]){
        double uScore = userScore(pState),
            fScore = setupScore(setup, sState),
            F = families.length,
            fScore_F = F * fScore;
        
        double f_softmaxDenom = 0.;
        
        for(int f=0; f<F; f++){
            double b_softmaxDenom = 0.;
            for (int b=0; b<families[f].B; b++){
                double bScore = families[f].behaviours[b].score(sState),
                        currBScore = families[f].behaviours[ setup[f] ].score(sState);
                if( policy.isBehaviourInPolicy(uScore, fScore, bScore, currBScore) ){
                    families[f].behaviours[b].softmaxNum = Utils.exp( 
                        //e^ (10 * (1 - |newFcore-uScore|))
                        emph_B * (
                            1. - Math.abs(
                                    ( (fScore_F - currBScore + bScore)/F )
                                    - uScore
                                )
                            )
                        );
                    b_softmaxDenom += families[f].behaviours[b].softmaxNum;
                }
                else{
                    //_Log.a("Policy", families[f].behaviours[b]+" is out of policy");
                    families[f].behaviours[b].softmaxNum = 0.0;
                }
            }
            //double f_expectedChange = 0.;
            if(b_softmaxDenom>0.){
                double currFScore = families[f].behaviours[ setup[f] ].score(sState);
                families[f].expected=0.;
                for (int b=0; b<families[f].B; b++){
                    families[f].behaviours[b].probability = families[f].behaviours[b].softmaxNum / b_softmaxDenom;
                    //f_expectedChange += families[f].behaviours[b].score(sState) * families[f].behaviours[b].probability;
                    families[f].expected += families[f].behaviours[b].score(sState) * families[f].behaviours[b].probability;
                }
                if( policy.isFamilyInPolicy( uScore, fScore, families[f].expected, currFScore ) ){
                    families[f].softmaxNum = Utils.exp( 
                        //e^ (10 * (1 - |newFcore-uScore|))
                        emph_F * (
                            1.0 - Math.abs(
                                    //( (fScore_F - families[f].behaviours[ setup[f] ].score(sState) + f_expectedChange)/F )
                                    ( (fScore_F - families[f].behaviours[ setup[f] ].score(sState) + families[f].expected)/F )
                                    - uScore
                                )
                            )
                        );
                    f_softmaxDenom += families[f].softmaxNum;
                }
                else{
                    _Log.a("Policy", "#"+families[f].behaviours[0]+" is out of policy");
                    families[f].softmaxNum = 0.0;
                }
            }
            else{
                for (int b=0; b<families[f].B; b++){
                    families[f].behaviours[b].probability = 0.;
                }
                families[f].softmaxNum = 0.;
                families[f].expected = families[f].behaviours[setup[f]].score(sState);
            }
        }
        if( f_softmaxDenom>0. )
            for(int f=0; f<F; f++)
                families[f].probability = families[f].softmaxNum / f_softmaxDenom;
        else return null;
        //Fine calcolo delle probabilità
        int f = selectFamilyToAdapt(families);
        int b = selectNewBehaviour(families[f]);
        
        //old Behaviour Score > new Behaviour Score
        if(families[f].behaviours[setup[f]].score(sState)>families[f].behaviours[b].score(sState))
            makeEasier++;
        else if(families[f].behaviours[setup[f]].score(sState)<families[f].behaviours[b].score(sState))
            makeHarder++;
        else ;//Same as before
        
        _Log.a("_Adapt", "userScore:"+uScore+". setupScore:"+fScore);
        _Log.a("Adapt", "Family: "+f+"("+f(families[f].probability)+").\t setup set to "+families[f].behaviours[b]+"("+f(families[f].behaviours[b].probability)+") from "+families[f].behaviours[setup[f]]);
        {   String ret="\n";
            for (int i=0; i<families[f].B; i++)
                ret += "\t"+families[f].behaviours[i]+"\t\t: "+f(families[f].behaviours[i].probability)+" with score "+families[f].behaviours[i].score(sState)+"\n";
            _Log.a("Chances B", ret);
        }
        {   String ret="\n";
            for (int i=0; i<families.length; i++)
                ret += "\tFamily of "+families[i].behaviours[0]+"\t\t: "+f(families[i].probability)+" with expected "+families[i].expected+"\n";//+" and softScore "+families[i].softmaxNum+"\n";
            _Log.a("Chances F", ret);
        }
        return new Substitution(f, b);
    }
    
    public static double userScore(PlayerState state){
        return userEval[state.p][state.d][state.e][state.l];
    }

    public static double setupScore(int[] setup, Object sState) {
        double score = 0;
        //double score_E = 0, score_B = 0, score_P = 0, score_I = 0, score_C = 0;
        //int count_E=0, count_B=0, count_P=0, count_I=0, count_C=0;
        
        for(int f=0; f<families.length; f++){
            score += families[f].score(setup[f], sState);
            /*if(f<4){
                score_E += families[f].score(setup[f], sState);
                count_E++;
            }
            else if(f<8){
                score_B += families[f].score(setup[f], sState);
                count_B++;
            }
            else if(f<10){
                score_P += families[f].score(setup[f], sState);
                count_P++;
            }
            else if(f<12){
                score_I += families[f].score(setup[f], sState);
                count_I++;
            }
            else{
                score_C += families[f].score(setup[f], sState);
                count_C++;
            }*/
        }
        /*_Log.a("Environment", "score env: "+(score_E/count_E) );
        _Log.a("Blinky", "score Blinky: "+(score_B/count_B) );
        _Log.a("Pinky", "score Pinky: "+(score_P/count_P) );
        _Log.a("Inky", "score Inky: "+(score_I/count_I) );
        _Log.a("Clyde", "score Clyde: "+(score_C/count_C) );*/
        return score/families.length;
    }

    private static int selectFamilyToAdapt(Family[] families) {
        double chances[] = new double[families.length];
        for (int f=0; f<families.length; f++)
            chances[f] = families[f].probability;
        return Utils.argRandom(chances);
    }

    private static int selectNewBehaviour(Family family) {
        double chances[] = new double[family.B];
        for (int b=0; b<family.B; b++)
            chances[b] = family.behaviours[b].probability;
        return Utils.argRandom(chances);
    }

    public static String setupToString(int setup[]) {
        String ret = "";
        for (int f=0; f<families.length; f++){
            ret += families[f].behaviours[setup[f]].name+"   ";
            if(f%4==3)
                ret+="\n\t";
        }
        return ret;
    }
    
    private final static class Substitution{
        private final int f, b;

        public Substitution(int f, int b) {
            this.f = f;
            this.b = b;
        }
    }
    
    static String f(double f){
        return ((int)(f*10000))/100. + "%";
    }
}

/*B=3;
Beh:
    0.5     0.4     0.6
    0.8             0.7
    0.3             0.35
                    0.1
bScore=0.5
uScore = 0.4
mod[b] = 1 - ( (bScore*B -b^ +b)/B - uScore )
//contributo = p[] * score[]
sum_f 35938
f:0                             sum e = 25847   E[]=0.422       mod[f]: 1-0.074 = 0.926     e^10mod:10509   p[]:0.29
    b:0 0.5     mod: 0.9        e^10mod: 8103         P[0]=0.31       contributo: 0.155
    b:1 0.8     mod: 0.8        e^10mod: 2980         P[1]=0.12       contributo: 0.096
    b:2 0.3     mod: 0.96       e^10mod: 14764        P[2]=0.57        contributo: 0.171

f:1                             sum e^ : 8103   E[] = 0.4      mod[1]: 1-0.1: 0.9            e^10mod:8103   p[]:0.22
    b:0 0.4     mod: 0.9        e^10mod: 8103         P[1]=1        contributo: 0.4

f:2                             sum e^: 44882    E[] = 0.373      mod[1]: 1-0.024: 0.976     e^10mod: 17326         p[]:0.48     
    b:0 0.6     mod: 0.9        e^10mod: 8103         P[0]=0.18        contributo: 0.108
    b:1 0.7     mod: 0.87       e^10mod: 6002         P[1]=0.13       contributo: 0.091
    b:2 0.35    mod: 0.984      e^10mod: 18769        P[2]=0.42       contributo: 0.147
    b:3 0.1     mod: 0.94       e^10mod: 12088        P[3]=0.27        contributo: 0.027
    

supponiamo sia sorteggiato f:0
supponiamo sia sorteggiato p:2
setup[f]=p*/