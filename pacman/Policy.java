package pacman;

import static pacman.Utils.max;
import static pacman.Utils.min;

/**
 *
 * @author Falie
 */
public interface Policy {
    public abstract boolean isBehaviourInPolicy(double userScore, double setupScore, double thisBscore, double currBScore);
    
    public abstract boolean isFamilyInPolicy(double userScore, double setupScore, double expectedFamilyScore, double currFScore);
}

class Const_Policy implements Policy{
    private final double min, max;

    public Const_Policy(double min, double max) {
        this.min = min<=0 ? 0 : min;
        this.max = max>=1 ? 1 : max;
    }
    
    @Override
    public boolean isBehaviourInPolicy(double userScore, double setupScore, double thisBscore, double currBScore) {
        return min<=thisBscore && thisBscore<=max;
    }

    @Override
    public boolean isFamilyInPolicy(double userScore, double setupScore, double expectedFamilyScore, double currFScore) {
        return min<=expectedFamilyScore && expectedFamilyScore<=max;
    }
}

class AroundUser_Policy implements Policy{
    private final double eps;

    public AroundUser_Policy(double eps) {
        this.eps = eps;
    }
    
    @Override
    public boolean isBehaviourInPolicy(double userScore, double setupScore, double thisBscore, double currBScore) {
        return userScore-eps<=thisBscore
                && thisBscore<=userScore+eps;
    }

    @Override
    public boolean isFamilyInPolicy(double userScore, double setupScore, double expectedFamilyScore, double currFScore) {
        return userScore-eps<=expectedFamilyScore
                && expectedFamilyScore<=userScore+eps;
    }
}

/**all contained between currB and user score+-eps*/
class ConvergingTransition_BtoU_Policy implements Policy{
    private final double eps;

    public ConvergingTransition_BtoU_Policy(double eps) {
        this.eps = eps;
    }
    
    @Override
    public boolean isBehaviourInPolicy(double userScore, double setupScore, double thisBScore, double currBScore) {
        return min(currBScore,userScore-eps)<thisBScore
                && thisBScore<max(currBScore, userScore+eps);
        /*return min(currBScore, max(userScore-eps, 0.0))<thisBScore
                && thisBScore<max(currBScore, min(userScore+eps, 1.0));*/
    }

    @Override
    public boolean isFamilyInPolicy(double userScore, double setupScore, double expectedFamilyScore, double currFScore) {
        return min(currFScore, userScore-eps)<expectedFamilyScore
                && expectedFamilyScore<max(currFScore, userScore+eps);
    }
}

/**all contained between setupScore and user score+-eps*/
class ConvergingTransition_StoU_Policy implements Policy{
    private final double eps;

    public ConvergingTransition_StoU_Policy(double eps) {
        this.eps = eps;
    }
    
    @Override
    public boolean isBehaviourInPolicy(double userScore, double setupScore, double thisBScore, double currBScore) {
        return min(setupScore, userScore-eps)<thisBScore
                && thisBScore<max(setupScore, userScore+eps);
    }

    @Override
    public boolean isFamilyInPolicy(double userScore, double setupScore, double expectedFamilyScore, double currFScore) {
        return min(setupScore, userScore-eps)<expectedFamilyScore
                && expectedFamilyScore<max(setupScore, userScore+eps);
    }
}

/**all contained between setupScore and user+score+eps / 2.. Avoids harsh substitutions*/
class SmoothTransition_BtoU_Policy implements Policy{
    private final double eps;

    public SmoothTransition_BtoU_Policy(double eps) {
        this.eps = eps;
    }
    
    @Override
    public boolean isBehaviourInPolicy(double userScore, double setupScore, double thisBScore, double currBScore) {
        double halfway = (currBScore + userScore)/2.;
        return min(currBScore, halfway-eps)<thisBScore
                && thisBScore<max(currBScore, halfway+eps);
    }

    @Override
    public boolean isFamilyInPolicy(double userScore, double setupScore, double expectedFamilyScore, double currFScore) {
        double halfway = ( currFScore + userScore)/2.;
        return min(currFScore, halfway-eps)<expectedFamilyScore
                && expectedFamilyScore<max(currFScore, halfway+eps);
    }
}

/**all contained between setupScore and user+score+eps / 2.. Avoids harsh substitutions*/
class SmoothTransition_StoU_Policy implements Policy{
    private final double eps;

    public SmoothTransition_StoU_Policy(double eps) {
        this.eps = eps;
    }
    
    @Override
    public boolean isBehaviourInPolicy(double userScore, double setupScore, double thisBScore, double currBScore) {
        double halfway = (setupScore + userScore)/2.;
        return min(setupScore, halfway-eps)<thisBScore
                && thisBScore<max(setupScore, halfway+eps);
    }

    @Override
    public boolean isFamilyInPolicy(double userScore, double setupScore, double expectedFamilyScore, double currFScore) {
        double halfway = (setupScore + userScore)/2.;
        return min(setupScore, halfway-eps)<expectedFamilyScore
                && expectedFamilyScore<max(setupScore, halfway+eps);
    }
}