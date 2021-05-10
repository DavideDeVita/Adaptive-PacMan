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
}