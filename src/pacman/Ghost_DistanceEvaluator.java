package pacman;

/**
 *
 * @author Falie
 */
public interface Ghost_DistanceEvaluator {
    public float eval(int x1, int y1, int x2, int y2);
}

class Euclidean2DistanceEvaluator implements Ghost_DistanceEvaluator{
    @Override
    public float eval(int x1, int y1, int x2, int y2) {
        return Utils.euclidean_dist2(x1, y1, x2, y2);
    }
}

class ToroEuclidean2DistanceEvaluator implements Ghost_DistanceEvaluator{
    @Override
    public float eval(int x1, int y1, int x2, int y2) {
        return Utils.toroEuclidean_dist2(x1, y1, x2, y2);
    }
}

class ManhattanDistanceEvaluator implements Ghost_DistanceEvaluator{
    @Override
    public float eval(int x1, int y1, int x2, int y2) {
        return Utils.manhattan_distance(x1, y1, x2, y2);
    }
}