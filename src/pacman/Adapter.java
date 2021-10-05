package pacman;

/**
 *
 * @author Falie
 */
public interface Adapter {
    public void editSpecifics(Setup cs, Behaviour behaviour);
}

class GhostSpeed_Adapter implements Adapter{
    @Override public void editSpecifics(Setup cs, Behaviour behaviour) {
        Float obj = (Float)behaviour.obj;
        cs.lvl.set_Ghost_Speed( obj );
    }
}

class UpTurnConstraint_Adapter implements Adapter{
    @Override public void editSpecifics(Setup cs, Behaviour behaviour) {
        Ghost_UpTurn_Constraint obj = (Ghost_UpTurn_Constraint)behaviour.obj;
        cs.upTurn_Constraint = obj;
    }
}

class DistanceEvaluator_Adapter implements Adapter{
    @Override public void editSpecifics(Setup cs, Behaviour behaviour) {
        Ghost_DistanceEvaluator obj = (Ghost_DistanceEvaluator)behaviour.obj;
        cs.distance = obj;
    }
}

class FrightTime_Adapter implements Adapter{
    @Override public void editSpecifics(Setup cs, Behaviour behaviour) {
        Float obj = (Float)behaviour.obj;
        cs.lvl.set_Fright_Time( obj );
    }
}

class Elroy1_DotsLeft_Adapter implements Adapter{
    @Override public void editSpecifics(Setup cs, Behaviour behaviour) {
        Integer obj = (Integer)behaviour.obj;
        cs.lvl.set_Elroy_1_Activation( obj );
    }
}

class Elroy_Chases_Adapter implements Adapter{
    @Override public void editSpecifics(Setup cs, Behaviour behaviour) {
        Integer obj = (Integer)behaviour.obj;
        cs.lvl.set_Elroy_AlwaysChase( obj );
    }
}

class Blinky_ChaseTargetGetter_Adapter implements Adapter{
    @Override public void editSpecifics(Setup cs, Behaviour behaviour) {
        ChaseTargetGetter obj = (ChaseTargetGetter)behaviour.obj;
        cs.blinky_C = obj;
    }
}

class Blinky_ScatterTargetGetter_Adapter implements Adapter{
    @Override public void editSpecifics(Setup cs, Behaviour behaviour) {
        ScatterTargetGetter obj = (ScatterTargetGetter)behaviour.obj;
        cs.blinky_S = obj;
    }
}

class Pinky_ChaseTargetGetter_Adapter implements Adapter{
    @Override public void editSpecifics(Setup cs, Behaviour behaviour) {
        ChaseTargetGetter obj = (ChaseTargetGetter)behaviour.obj;
        cs.pinky_C = obj;
    }
}

class Pinky_ScatterTargetGetter_Adapter implements Adapter{
    @Override public void editSpecifics(Setup cs, Behaviour behaviour) {
        ScatterTargetGetter obj = (ScatterTargetGetter)behaviour.obj;
        cs.pinky_S = obj;
    }
}

class Inky_ChaseTargetGetter_Adapter implements Adapter{
    @Override public void editSpecifics(Setup cs, Behaviour behaviour) {
        ChaseTargetGetter obj = (ChaseTargetGetter)behaviour.obj;
        cs.inky_C = obj;
    }
}

class Inky_ScatterTargetGetter_Adapter implements Adapter{
    @Override public void editSpecifics(Setup cs, Behaviour behaviour) {
        ScatterTargetGetter obj = (ScatterTargetGetter)behaviour.obj;
        cs.inky_S = obj;
    }
}

class Clyde_ChaseTargetGetter_Adapter implements Adapter{
    @Override public void editSpecifics(Setup cs, Behaviour behaviour) {
        ChaseTargetGetter obj = (ChaseTargetGetter)behaviour.obj;
        cs.clyde_C = obj;
    }
}

class Clyde_ScatterTargetGetter_Adapter implements Adapter{
    @Override public void editSpecifics(Setup cs, Behaviour behaviour) {
        ScatterTargetGetter obj = (ScatterTargetGetter)behaviour.obj;
        cs.clyde_S = obj;
    }
}