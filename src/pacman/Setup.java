package pacman;

/**
 *
 * @author Falie
 */
public class Setup {
    String name;
    int pac_lives=3;
    LevelSpecifics lvl;
    Ghost_UpTurn_Constraint upTurn_Constraint;
    Ghost_180Turn_Constraint turn180_Constraint;
    Ghost_Force180Turn_Constraint force180Turn_Constraint;
    Ghost_DistanceEvaluator distance;
    float fright_time;
    ElroyChasesOnScatter_Constraint elroyChasesOnScatter_Constraint;
    InkyReferenceGhost_Getter inkyReferenceGhost_Getter;
    ScatterTargetGetter blinky_S, pinky_S, inky_S, clyde_S;
    ChaseTargetGetter blinky_C, pinky_C, inky_C, clyde_C;
    
    private boolean isReady() {
        return blinky_S!=null && pinky_S!=null && inky_S!=null && clyde_S!=null
            && blinky_C!=null && pinky_C!=null && inky_C!=null && clyde_C!=null
                && lvl!=null && upTurn_Constraint!=null && turn180_Constraint!=null
                    && force180Turn_Constraint!=null && inkyReferenceGhost_Getter!=null
                        && elroyChasesOnScatter_Constraint!=null && distance!=null;
    }
    
    public float get_lvl_stat_index(int index){
        return lvl.get(index);
    }

    @Override
    public String toString() {
        return name;
    }
    
    public static Setup std() {
        Setup ret = new Setup();
        ret.name = "Standard";
        ret.lvl = LevelSpecifics.lvl1_std();
        ret.upTurn_Constraint = new Ghost_UpTurn_Constraint(true);
        ret.turn180_Constraint = new Ghost_180Turn_Constraint(true);
        ret.force180Turn_Constraint = new Ghost_Force180Turn_Constraint(true);
        ret.elroyChasesOnScatter_Constraint = new ElroyChasesOnScatter_Constraint(true);
        ret.inkyReferenceGhost_Getter = new InkyReferenceGhost_Getter_Std();
        ret.distance = new Euclidean2DistanceEvaluator();
        
        ret.blinky_C = new ChaseTarget_Blinky_Std();
        ret.blinky_S = new ScatterTarget_Blinky_Std(25, -3);
        ret.pinky_C = new ChaseTarget_Pinky_Std();
        ret.pinky_S = new ScatterTargetGetter_Const(3, -3);
        ret.inky_C = new ChaseTarget_Inky_Std();
        ret.inky_S = new ScatterTargetGetter_Const(25, 32);
        ret.clyde_C = new ChaseTarget_Clyde_Std();
        ret.clyde_S = new ScatterTargetGetter_Const(3, 32);
        return ret;
    }
    public static Setup placid() {
        Setup ret = new Setup();
        ret.name = "Placid Setup";
        ret.lvl = LevelSpecifics.easiest();
        ret.upTurn_Constraint = new Ghost_UpTurn_Constraint(true);
        ret.turn180_Constraint = new Ghost_180Turn_Constraint(true);
        ret.force180Turn_Constraint = new Ghost_Force180Turn_Constraint(true);
        ret.elroyChasesOnScatter_Constraint = new ElroyChasesOnScatter_Constraint(true);
        ret.inkyReferenceGhost_Getter = new InkyReferenceGhost_Getter_Std();
        ret.distance = new ManhattanDistanceEvaluator();
        
        ret.blinky_C = new ChaseTarget_Blinky_RoamAround(5);
        ret.blinky_S = new ScatterTarget_Blinky_RandomArea(9, 11, 18, 17);
        ret.pinky_C = new ChaseTarget_Pinky_Std(8);
        ret.pinky_S = new ScatterTarget_RandomArea(9, 11, 18, 17);
        ret.inky_C = new ChaseTarget_Inky_Std(6);
        ret.inky_S = new ScatterTarget_RandomArea(9, 11, 18, 17);
        ret.clyde_C = new ChaseTarget_Clyde_Std(12);
        ret.clyde_S = new ScatterTarget_RandomArea(9, 11, 18, 17);
        return ret;
    }   //Pac-riature (Pe e' criature)
    public static Setup iWannaCry() {
        Setup ret = new Setup();
        ret.name = "I wanna cry";
        ret.lvl = LevelSpecifics.hardest();
        ret.upTurn_Constraint = new Ghost_UpTurn_Constraint(false);
        ret.turn180_Constraint = new Ghost_180Turn_Constraint(true);
        ret.force180Turn_Constraint = new Ghost_Force180Turn_Constraint(true);
        ret.elroyChasesOnScatter_Constraint = new ElroyChasesOnScatter_Constraint(true);
        ret.inkyReferenceGhost_Getter = new InkyReferenceGhost_Getter_Std();
        ret.distance = new Euclidean2DistanceEvaluator();
        
        ret.blinky_C = new ChaseTarget_Blinky_Std();
        ret.blinky_S = new ScatterTarget_Blinky_RandomArea(14, -2, 27, 8);
        ret.pinky_C = new ChaseTarget_Pinky_NextIntersection();
        ret.pinky_S = new ScatterTarget_RandomArea(0, -2, 13, 8);
        ret.inky_C = new ChaseTarget_Inky_Clipped(2.5f);
        ret.inky_S = new ScatterTarget_RandomArea(14, 20, 27, 32);
        ret.clyde_C = new ChaseTarget_Clyde_PacmanClosestEnerg(5);
        ret.clyde_S = new ScatterTarget_RandomArea(0, 20, 13, 32);
        return ret;
    }    //Pac-cheri o Pac-cirrASalut
    public static Setup unfair() {
        Setup ret = new Setup();
        ret.name = "Unfair";
        ret.lvl = LevelSpecifics.unfair();
        ret.upTurn_Constraint = new Ghost_UpTurn_Constraint(false);
        ret.turn180_Constraint = new Ghost_180Turn_Constraint(true);
        ret.force180Turn_Constraint = new Ghost_Force180Turn_Constraint(true);
        ret.elroyChasesOnScatter_Constraint = new ElroyChasesOnScatter_Constraint(true);
        ret.inkyReferenceGhost_Getter = new InkyReferenceGhost_Getter_Std();
        ret.distance = new ToroEuclidean2DistanceEvaluator();
        
        ret.blinky_C = new ChaseTarget_Blinky_Std();
        ret.blinky_S = new ScatterTarget_Blinky_RandomArea(14, -2, 27, 8);
        ret.pinky_C = new ChaseTarget_Pinky_NextIntersection();
        ret.pinky_S = new ScatterTarget_RandomArea(0, -2, 13, 8);
        ret.inky_C = new ChaseTarget_Inky_Clipped(2.5f);
        ret.inky_S = new ScatterTarget_RandomArea(14, 20, 27, 32);
        ret.clyde_C = new ChaseTarget_Clyde_PacmanClosestEnerg(5);
        ret.clyde_S = new ScatterTarget_RandomArea(0, 20, 13, 32);
        return ret;
    }    //Pac-chiagnere
}