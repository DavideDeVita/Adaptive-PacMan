package pacman;

/**
 *
 * @author Falie
 */
public class CustomSpecifics {
    //Custom Specific
    private int pac_lives;
    
    private LevelSpecifics levelSpecific;
    private Ghost_UpTurn_Constraint upTurn_Constraint;
    private Ghost_180Turn_Constraint turn180_Constraint;
    private Ghost_Force180Turn_Constraint force180Turn_Constraint;
    private InkyReferenceGhost_Getter inkyReferenceGhost_Getter;

    public CustomSpecifics() {
        pac_lives=3;
        this.levelSpecific = getLevel_1_Specifics();
        upTurn_Constraint = std_Ghost_UpTurn_Constraint();
        turn180_Constraint = std_Ghost_180Turn_Constraint();
        force180Turn_Constraint = std_Ghost_Force180Turn_Constraint();
        inkyReferenceGhost_Getter = std_InkyReferenceGhost_Getter();
    }

    public int getPacLives() {
        return pac_lives;
    }

    public LevelSpecifics getLevelSpecific() {
        return levelSpecific;
    }
    public void setLevelSpecific(LevelSpecifics levelSpecific) {
        this.levelSpecific = levelSpecific;
    }

    public Ghost_UpTurn_Constraint getUpTurn_Constraint() {
        return upTurn_Constraint;
    }
    public void setUpTurn_Constraint(Ghost_UpTurn_Constraint upTurn_Constraint) {
        this.upTurn_Constraint = upTurn_Constraint;
    }

    public Ghost_180Turn_Constraint getTurn180_Constraint() {
        return turn180_Constraint;
    }
    public void setTurn180_Constraint(Ghost_180Turn_Constraint turn180_Constraint) {
        this.turn180_Constraint = turn180_Constraint;
    }

    public Ghost_Force180Turn_Constraint getForce180Turn_Constraint() {
        return force180Turn_Constraint;
    }
    public void setForce180Turn_Constraint(Ghost_Force180Turn_Constraint force180turn_Constraint) {
        this.force180Turn_Constraint = force180turn_Constraint;
    }

    public InkyReferenceGhost_Getter getInkyReferenceGhost_Getter() {
        return inkyReferenceGhost_Getter;
    }
    public void setInkyReferenceGhost_Getter(InkyReferenceGhost_Getter inkyReferenceGhost_Getter) {
        this.inkyReferenceGhost_Getter = inkyReferenceGhost_Getter;
    }
    
    //statics stadnards
    public static LevelSpecifics getLevel_1_Specifics(){
        return new LevelSpecifics( new float[]{
            0.8f, 0.71f, 0.75f, 0.4f,
                0.9f, 0.79f, 0.5f, 6,
                    20, 0.8f, 10, 0.85f, 1,
                        11, 20, 7, 20, 5, 20, 5,
                            0f, 4f, 8f, 12f}
        );
    }

    public static Ghost_180Turn_Constraint std_Ghost_180Turn_Constraint() {
        return new Ghost_180Turn_Constraint(true);
    }

    public static Ghost_Force180Turn_Constraint std_Ghost_Force180Turn_Constraint() {
        return new Ghost_Force180Turn_Constraint(true);
    }

    public static Ghost_UpTurn_Constraint std_Ghost_UpTurn_Constraint() {
        return new Ghost_UpTurn_Constraint(true);
    }

    public static InkyReferenceGhost_Getter std_InkyReferenceGhost_Getter() {
        return new InkyReferenceGhost_Getter_Std();
    }
    
    public static ScatterTargetGetter std_Blinky_Scatter(GameLogic logic){
        return new ScatterTarget_Blinky_Std(logic, 25, -3);
    }
    public static ScatterTargetGetter std_Pinky_Scatter(GameLogic logic){
        return new ScatterTargetGetter_Const(logic, 3, -3);
    }
    public static ScatterTargetGetter std_Inky_Scatter(GameLogic logic){
        return new ScatterTargetGetter_Const(logic, 25, 32);
    }
    public static ScatterTargetGetter std_Clyde_Scatter(GameLogic logic){
        return new ScatterTargetGetter_Const(logic, 3, 32);
    }

    static ChaseTargetGetter std_Blinky_Chase(GameLogic logic) {
        return new ChaseTarget_Blinky_Std(logic);
    }
    static ChaseTargetGetter std_Pinky_Chase(GameLogic logic) {
        return new ChaseTarget_Pinky_Std(logic);
    }
    static ChaseTargetGetter std_Inky_Chase(GameLogic logic) {
        return new ChaseTarget_Inky_Std(logic);
    }
    static ChaseTargetGetter std_Clyde_Chase(GameLogic logic) {
        return new ChaseTarget_Clyde_Std(logic);
    }
    
    static ChaseTargetGetter bugFix_Pinky_Chase(GameLogic logic) {
        return new ChaseTarget_Pinky_BugFix(logic);
    }
    static ChaseTargetGetter bugFix_Inky_Chase(GameLogic logic) {
        return new ChaseTarget_Inky_BugFix(logic);
    }
    static ChaseTargetGetter pacmanNearestDot_Clyde_Chase(GameLogic logic) {
        return new ChaseTarget_Clyde_PacmanClosestDot(logic);
    }
}
