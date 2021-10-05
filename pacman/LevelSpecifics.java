package pacman;

/**
 *
 * @author Falie
 */
public class LevelSpecifics {
    public final static int PACMAN_SPEED=0, GHOST_SPEED=1, FRIGHT_TIME=2,
            ELROY_1_DOTS_LEFT=3, ELROY_ALWAYS_CHASE=4,
            DURATION_1ST_SCATTER=5, DURATION_1ST_CHASE=6, 
            DURATION_2ND_SCATTER=7, DURATION_2ND_CHASE=8,
            DURATION_3RD_SCATTER=9, DURATION_3RD_CHASE=10, DURATION_4TH_SCATTER=11,
            ACTIVATE_BLINKY=12, ACTIVATE_PINKY=13, ACTIVATE_INKY=14, ACTIVATE_CLYDE=15;
    /** PACMAN_SPEED:                The speed of PacMan
    *   GHOST_SPEED:                 The speed of the Ghost
    *   FRIGHT_TIME                  The duration in seconds of the Frightened State
    *   ELROY_1_DOTS_LEFT:           The threshold of missing dots to activate Cruise Elroy 1
    *   ELROY_ALWAYS_CHASE:          The required phase (1 or 2) to force Cruise Elroy to always chase
    *   DURATION_ith_SCATTER:        The duration of the i-th Scatter (i = 1, 2, 3,4)
    *   DURATION_ith_CHASE:        The duration of the i-th Chase (i = 1, 2, 3)
     */
    private static int size=16;
    
    public float[] specifics;
    
    public final static LevelSpecifics lvl1_std(){
        return new LevelSpecifics(new float[]{
            0.8f, -0.05f, 5,
                    20, 1,
                        9, 20, 7, 20, 5, 20, 5,
                            0f, 2f, 5f, 8f});
    }

    public final static LevelSpecifics easiest() {
        return new LevelSpecifics(new float[]{
            0.8f, -0.075f, 5.5f,
                    20, 3,
                        9, 20, 7, 20, 5, 20, 5,
                            0f, 2f, 5f, 8f});
    }
    public final static LevelSpecifics hardest() {
        return new LevelSpecifics(new float[]{
            0.8f, +0.05f, 4f,
                    100, 1,
                        9, 20, 7, 20, 5, 20, 5,
                            0f, 2f, 5f, 8f});
    }
    public final static LevelSpecifics unfair() {
        return new LevelSpecifics(new float[]{
            0.75f, +0.075f, 0.5f,
                    150, 1,
                        7, 20, 5, 20, 3, 20, 1,
                            0f, 0f, 0f, 0f});//Not really changed due to private field in ghost
    }

    public LevelSpecifics(float... specifics) {
        this.specifics = specifics.clone();
    }
    
    public float get(int index){
        return specifics[index];
    }
    
    public float get_PacMan_Speed(){ return specifics[PACMAN_SPEED]; }
    public float get_PacMan_Dot_Speed(){
        return 0.71f + 0.8f * (specifics[PACMAN_SPEED]-0.8f);
    }
    public float get_Ghost_Speed(){ return get_PacMan_Speed() + specifics[GHOST_SPEED]; }
    public float get_Ghost_Tunnel_Speed(){
        return specifics[PACMAN_SPEED] / 2f; 
    }
    public float get_PacMan_Fright_Speed(){
        return (specifics[PACMAN_SPEED]+1f)/2f; 
    }
    public float get_PacMan_Fright_Dot_Speed(){ 
        return 0.71f + 0.8f * (get_PacMan_Fright_Speed()-0.8f);
    }
    public float get_Ghost_Fright_Speed(){ 
        return get_Ghost_Tunnel_Speed() + 0.1f; 
    }
    public float get_Fright_Time(){ return specifics[FRIGHT_TIME]; }
    public int get_Elroy_1_Activation(){ return (int)specifics[ELROY_1_DOTS_LEFT]; }
    public float get_Elroy_1_Speed(){ return get_Ghost_Speed() + 0.05f; }
    public int get_Elroy_2_Activation(){ return (int)(specifics[ELROY_1_DOTS_LEFT]/2); }
    public float get_Elroy_2_Speed(){ return get_Ghost_Speed() + 0.1f; }
    public int get_Elroy_AlwaysChase(){ return (int)specifics[ELROY_ALWAYS_CHASE]; }
    public float get_Duration_1st_Scatter(){ return specifics[DURATION_1ST_SCATTER]; }
    public float get_Duration_1st_Chase(){ return specifics[DURATION_1ST_SCATTER]; }
    public float get_Duration_2nd_Scatter(){ return specifics[DURATION_2ND_SCATTER]; }
    public float get_Duration_2nd_Chase(){ return specifics[DURATION_2ND_SCATTER]; }
    public float get_Duration_3rd_Scatter(){ return specifics[DURATION_3RD_SCATTER]; }
    public float get_Duration_3rd_Chase(){ return specifics[DURATION_3RD_SCATTER]; }
    public float get_Duration_4th_Scatter(){ return specifics[DURATION_4TH_SCATTER]; }
    public float get_Activate_Blinky(){ return specifics[ACTIVATE_BLINKY]; }
    public float get_Activate_Pinky(){ return specifics[ACTIVATE_PINKY]; }
    public float get_Activate_Inky(){ return specifics[ACTIVATE_INKY]; }
    public float get_Activate_Clyde(){ return specifics[ACTIVATE_CLYDE]; }
    
    
    public void set_PacMan_Speed(float setTo){ specifics[PACMAN_SPEED] = setTo; }
    public void set_Ghost_Speed(float setTo){ specifics[GHOST_SPEED] = setTo; }
    public void set_Fright_Time(float setTo){ specifics[FRIGHT_TIME] = setTo; }
    public void set_Elroy_1_Activation(int setTo){ specifics[ELROY_1_DOTS_LEFT] = setTo; }
    public void set_Elroy_AlwaysChase(int setTo){ specifics[ELROY_ALWAYS_CHASE] = setTo; }
    public void set_Duration_1st_Scatter(float setTo){ specifics[DURATION_1ST_SCATTER] = setTo; }
    public void set_Duration_1st_Chase(float setTo){ specifics[DURATION_1ST_SCATTER] = setTo; }
    public void set_Duration_2nd_Scatter(float setTo){ specifics[DURATION_2ND_SCATTER] = setTo; }
    public void set_Duration_2nd_Chase(float setTo){ specifics[DURATION_2ND_SCATTER] = setTo; }
    public void set_Duration_3rd_Scatter(float setTo){ specifics[DURATION_3RD_SCATTER] = setTo; }
    public void set_Duration_3rd_Chase(float setTo){ specifics[DURATION_3RD_SCATTER] = setTo; }
    public void set_Duration_4th_Scatter(float setTo){ specifics[DURATION_4TH_SCATTER] = setTo; }
    public void set_Activate_Blinky(float setTo){ specifics[ACTIVATE_BLINKY] = setTo; }
    public void set_Activate_Pinky(float setTo){ specifics[ACTIVATE_PINKY] = setTo; }
    public void set_Activate_Inky(float setTo){ specifics[ACTIVATE_INKY] = setTo; }
    public void set_Activate_Clyde(float setTo){ specifics[ACTIVATE_CLYDE] = setTo; }
}