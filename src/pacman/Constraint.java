package pacman;

/**
 *
 * @author Falie
 */
public abstract class Constraint {
    private final boolean active;

    public Constraint(boolean active) {
        this.active = active;
    }
    
    public final boolean constraintActive(){
        return active;
    }
}

class Ghost_UpTurn_Constraint extends Constraint{

    public Ghost_UpTurn_Constraint(boolean active) {
        super(active);
    }
}

class Ghost_180Turn_Constraint extends Constraint{

    public Ghost_180Turn_Constraint(boolean active) {
        super(active);
    }
}

class Ghost_Force180Turn_Constraint extends Constraint{

    public Ghost_Force180Turn_Constraint(boolean active) {
        super(active);
    }
}

class ElroyChasesOnScatter_Constraint extends Constraint{

    public ElroyChasesOnScatter_Constraint(boolean active) {
        super(active);
    }
}