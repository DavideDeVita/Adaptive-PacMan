package pacman;

/**
 *
 * @author Falie
 */
public class LevelSpecifics {
    public final static int PACMAN_SPEED=0, PACMAN_DOTS_SPEED=1, GHOST_SPEED=2, GHOST_TUNNEL_SPEED=3,
            FRIGHT_PACMAN_SPEED=4, FRIGHT_PACMAN_DOTS_SPEED=5, FRIGHT_GHOST_SPEED=6, FRIGHT_TIME=7,
            ELROY_1_DOTS_LEFT=8, ELROY_1_SPEED=9, ELROY_2_DOTS_LEFT=10, ELROY_2_SPEED=11, ELROY_ALWAYS_CHASE=12,
            DURATION_1ST_SCATTER=13, DURATION_1ST_CHASE=14, 
            DURATION_2ND_SCATTER=15, DURATION_2ND_CHASE=16,
            DURATION_3RD_SCATTER=17, DURATION_3RD_CHASE=18, DURATION_4TH_SCATTER=19,
            ACTIVATE_BLINKY=20, ACTIVATE_PINKY=21, ACTIVATE_INKY=22, ACTIVATE_CLYDE=23;
    /**PACMAN_SPEED:                The speed of PacMan
     * PACMAN_DOTS_SPEED=1:         The speed of PacMan when on Dots or Energizers
     * GHOST_SPEED:                 The speed of the Ghosts
     * GHOST_TUNNEL_SPEED:          The speed of the Ghosts in the tunnel
     * FRIGHT_PACMAN_SPEED=4:       The speed of PacMan when Energizer is active (Frightened State)
     * FRIGHT_PACMAN_DOTS_SPEED:    The speed of PacMan when Energizer is active (Frightened State) when on Dots or Energizers
     * FRIGHT_GHOST_SPEED:          The speed of the Ghosts when Frightened
     * FRIGHT_TIME                  The duration in seconds of the Frightened State
     * ELROY_1_DOTS_LEFT:           The threshold of missing dots to activate Cruise Elroy 1
     * ELROY_1_SPEED:               The speed of Cruise Elroy 1
     * ELROY_2_DOTS_LEFT            The threshold of missing dots to activate Cruise Elroy 2
     * ELROY_2_SPEED:               The speed of Cruise Elroy 1
     * ELROY_ALWAYS_CHASE:          The required phase (1 or 2) to force Cruise Elroy to always chase
     * DURATION_ith_SCATTER:        The duration of the i-th Scatter (i = 1, 2, 3,4)
     * DURATION_ith_CHASE:        The duration of the i-th Chase (i = 1, 2, 3)
     */
    private static int size=24;
    
    public float[] specifics;

    public LevelSpecifics(float... specifics) {
        this.specifics = specifics.clone();
    }
    
    public float get(int index){
        return specifics[index];
    }
    
    public float get_PacMan_Speed(){ return specifics[PACMAN_SPEED]; }
    public float get_PacMan_Dot_Speed(){ return specifics[PACMAN_DOTS_SPEED]; }
    public float get_Ghost_Speed(){ return specifics[GHOST_SPEED]; }
    public float get_Ghost_Tunnel_Speed(){ return specifics[GHOST_TUNNEL_SPEED]; }
    public float get_PacMan_Fright_Speed(){ return specifics[FRIGHT_PACMAN_SPEED]; }
    public float get_PacMan_Fright_Dot_Speed(){ return specifics[FRIGHT_PACMAN_DOTS_SPEED]; }
    public float get_Ghost_Fright_Speed(){ return specifics[FRIGHT_GHOST_SPEED]; }
    public int get_Fright_Time(){ return (int)specifics[FRIGHT_TIME]; }
    public int get_Elroy_1_Activation(){ return (int)specifics[ELROY_1_DOTS_LEFT]; }
    public float get_Elroy_1_Speed(){ return specifics[ELROY_1_SPEED]; }
    public int get_Elroy_2_Activation(){ return (int)specifics[ELROY_2_DOTS_LEFT]; }
    public float get_Elroy_2_Speed(){ return specifics[ELROY_2_SPEED]; }
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
}