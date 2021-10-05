package pacman;

import javafx.util.Pair;
import pacman.StateObservation_Strategies.StrategyState;

/**
 *
 * @author Falie
 */
public class Family {
    public final Behaviour behaviours[];
    public final int B;
    private final Adapter adapter;
    public double softmaxNum, probability, expected;
    
    public Family(Behaviour[] behaviours, Adapter adapter) {
        this.behaviours = behaviours;
        this.B = behaviours.length;
        this.adapter=adapter;
    }
    
    public double score(int b, Object something){
        return behaviours[b].score(something);
    }
    
    public void adapt(GameLogic logic, int b){
        Setup cs = logic.getSetup();
        adapter.editSpecifics(cs, behaviours[b]);
        logic.setSetup(cs);
    }
}

abstract class Behaviour{
    public double softmaxNum, probability;
    public Object obj;
    public String name;

    public Behaviour(Pair<Object, String> obj) {
        this.obj = obj.getKey();
        this.name = obj.getValue();
    }
    
    public abstract double score(Object something);

    @Override
    public final String toString() {
        return name;
    }
}

class Behaviour_Tabular extends Behaviour{
    private final double table[][][];    

    public Behaviour_Tabular(Pair<Object, String> obj, double[][][] table) {
        super(obj);
        this.table = table;
}
    
    @Override
    public double score(Object something){
        StrategyState state = (StrategyState)something;
        return table[state.p][state.c][state.l];
    }
}

class Behaviour_SingleScore extends Behaviour{
    private final double score;

    public Behaviour_SingleScore(Pair<Object, String> obj, double score) {
        super(obj);
        this.score=score;
    }

    @Override
    public double score(Object something) {
        return score;
    }
}