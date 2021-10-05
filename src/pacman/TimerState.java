package pacman;

import static pacman.LevelSpecifics.*;
import static pacman.State.*;

/**
 *
 * @author Falie
 */
public enum TimerState { 
    Scatter_1(Scatter, DURATION_1ST_SCATTER), Chase_1(Chase, DURATION_1ST_CHASE),
    Scatter_2(Scatter, DURATION_2ND_SCATTER), Chase_2(Chase, DURATION_2ND_CHASE),
    Scatter_3(Scatter, DURATION_3RD_SCATTER), Chase_3(Chase, 0);
    
    public final State state;
    public final int index;
    TimerState(State state, int index){
        this.state=state;
        this.index=index;
    }
    
    public float timerStateDuration(LevelSpecifics specifics){
        return specifics.get(index);
    }

    public TimerState next() {
        if( isLast() ) return this;
        else return TimerState.values()[this.ordinal()+1];
    }

    public boolean isLast() {
        return this.ordinal()+1 >= TimerState.values().length;
    }

    static TimerState stateAt(float fullGameTimer, LevelSpecifics specifics) {
        TimerState[] values = values();
        for (int i=0; i<values.length; i++){
            fullGameTimer -= values[i].timerStateDuration(specifics);
            if(fullGameTimer <= 0) return values[i];
        }
        return values[values.length-1];
    }
}

enum State { 
    Scatter, Chase;
}