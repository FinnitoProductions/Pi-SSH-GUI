package util;

/**
 * Represents a point where location values can be passed into the constructor.
 * @author Finn Frankis
 * @version Aug 9, 2018
 */
public class Point extends java.awt.Point {
    
    /**
     * Constructs a new Point.
     * @param x the x-value of this point
     * @param y the y-value of this point
     */
    public Point (double x, double y)
    {
        setLocation(x, y);
    }
}
