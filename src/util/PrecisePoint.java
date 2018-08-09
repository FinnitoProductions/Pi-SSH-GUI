package util;

import java.awt.Point;

/**
 * Represents a point where location values can be passed into the constructor.
 * @author Finn Frankis
 * @version Aug 9, 2018
 */
public class PrecisePoint extends Point {

    private double x;
    private double y;

    /**
     * Constructs a new PrecisePoint.
     * @param x the x-value of this point
     * @param y the y-value of this point
     */
    public PrecisePoint (double x, double y) {
        setLocation(x, y);
    }

    public double getX () {
        return x;
    }

    public double getY () {
        return y;
    }

    public void setX (double x) {
        this.x = x;
    }

    public void setY (double y) {
        this.y = y;
    }
    
    public void setLocation (double x, double y) {
        setX(x);
        setY(y);
    }

    @Override
    public void setLocation (Point p) {
        setLocation(p.getX(), p.getY());
    }

    @Override
    public String toString () {
        return "(" + getX() + ", " + getY() + ")";
    }

    @Override
    public boolean equals (Object o) {
        if (!(o instanceof Point))
            return false;
        Point p = (Point) o;
        return p.getX() == this.getX() && p.getY() == this.getY();
    }
}
