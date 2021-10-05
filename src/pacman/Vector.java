package pacman;

/**
 *
 * @author Falie
 */
class Vector {
    int x, y;

    Vector(int x, int y) {
        this.x=x;
        this.y=y;
    }

    void add(Vector addMe) {
        x += addMe.x;
        y += addMe.y;
    }

    void reset(Vector setWith) {
        this.x = setWith.x;
        this.y = setWith.y;
    }

    void reset(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return x+" "+y;
    }
    
    @Override
    public Vector clone(){
        return new Vector(x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Vector) ){
            return false;
        }
        final Vector other = (Vector) obj;
        return this.x==other.x && this.y==other.y;
    }

    public void add(int x, int y) {
        this.x += x;
        this.y += y;
    }
}