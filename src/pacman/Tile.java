package pacman;

import static pacman.Path.Horizontal_Up;
import static pacman.Path.NoWay;
import static pacman.Path.Vertical_Dx;
import static pacman.Path.Vertical_Sx;

/**
 *
 * @author Falie
 */
public class Tile {
    Collectible contains;
    Path directions;

    public Tile() {
    }
}

enum Collectible { Empty, Dot, Energizer, Wall, Door, Fruit, Tunnel, House, OutDoor}

enum Path {
    //2 way
    Corridor_V (true, false, true, false),
    Corridor_H (false, true, false, true),
    Corner_NW (true, true, false, false),
    Corner_SW (false, true, true, false),
    Corner_SE (false, false, true, true),
    Corner_NE (true, false, false, true),
    Ghost_Door (true, false, true, false),
    // 4 way
    Cross (true, true, true, true),
    //No way
    NoWay (false, false, false, false),
    //3 way
    Vertical_Dx (true, false, true, true),
    Vertical_Sx (true, true, true, false), 
    Horizontal_Up (true, true, false, true),
    Horizontal_Up_Exc (true, true, false, true), //Exception tile up. Ghosts could not go up here
    Horizontal_Down (false, true, true, true);
    //can go?
    final boolean up, left, down, right;
    Path(boolean u, boolean l, boolean d, boolean r){
        this.up=u;
        this.left=l;
        this.down=d;
        this.right=r;
    }
    
    public boolean canGo(Direction d){
        switch(d){
            case Up:
                return up;
            case Left:
                return left;
            case Down:
                return down;
            case Right:
                return right;
        }
        return false;//in doubt.. well say no
    }
    
    public int ways(){
        int ret=0;
        if(up) ret++;
        if(left) ret++;
        if(down) ret++;
        if(right) ret++;
        return ret;
    }
    
    static Path getFromOpenings(boolean u, boolean l, boolean d, boolean r){
        if (u && l && d && r)
            return Cross;
        else if (u && l && d && !r)
            return Vertical_Sx;
        else if (u && !l && d && r)
            return Vertical_Dx;
        else if (u && !l && d && !r)
            return Corridor_V;
        else if (!u && l && d && r)
            return Horizontal_Down;
        else if (u && l && !d && r)
            return Horizontal_Up;
        else if (!u && l && !d && r)
            return Corridor_H;
        else if (u && l && !d && !r)
            return Corner_NW;
        else if (!u && l && d && !r)
            return Corner_SW;
        else if (!u && !l && d && r)
            return Corner_SE;
        else if (u && !l && !d && r)
            return Corner_NE;
        else
            return NoWay;
    }
}