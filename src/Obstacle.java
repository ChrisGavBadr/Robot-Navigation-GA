
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

import java.util.ArrayList;

public class Obstacle extends Polygon {

    private static final long serialVersionUID = 1L;

    private Polygon obstacle;

    // Obstacle Constructors
    Obstacle() {
        obstacle = new Polygon();
    }

    Obstacle(int[] x, int[] y, int n) {
        obstacle = new Polygon(x, y, n);
    }

    /* Important Functions */

    // Determines whether specified path intersects obstacle
    public boolean intersects(Line2D path) {
        ArrayList<Line2D> boundaries = getBoundaries();

        for (Line2D boundary : boundaries) {
            if (path.intersectsLine(boundary))
                return true;
        }

        return false;
    }

    // Draws obstacle on the environment
    public void drawObstacle(DrawingPanel panel, Graphics2D g) {
        ArrayList<Line2D> boundaries = getBoundaries();

        for (Line2D boundary : boundaries) {
            g.drawLine((int) (boundary.getX1() * Constants.X_SCALE) + Constants.MARGIN_THICKNESS / 2,
                    (int) (-boundary.getY1() * Constants.Y_SCALE + Constants.PANEL_HEIGHT) + Constants.MARGIN_THICKNESS / 2,
                    (int) (boundary.getX2() * Constants.X_SCALE) + Constants.MARGIN_THICKNESS / 2,
                    (int) (-boundary.getY2() * Constants.Y_SCALE + Constants.PANEL_HEIGHT) + Constants.MARGIN_THICKNESS / 2);

            g.fillOval((int) (boundary.getX1() * Constants.X_SCALE) + (Constants.MARGIN_THICKNESS - Constants.POINT_RADIUS + 1) / 2,
                    (int) (-boundary.getY1() * Constants.Y_SCALE + Constants.PANEL_HEIGHT) + (Constants.MARGIN_THICKNESS - Constants.POINT_RADIUS + 1) / 2,
                    Constants.POINT_RADIUS, Constants.POINT_RADIUS);
        }
    }

    public String toString() {
        ArrayList<Point2D> vertices = getVertices();
        String stringObstacle = "Obstacle: {(" + vertices.get(0).getX() + ", " + vertices.get(0).getY() + ")";

        for (int i = 1; i < vertices.size(); i++)
            stringObstacle += ", (" + vertices.get(i).getX() + ", " + vertices.get(i).getY() + ")";

        stringObstacle += "}";

        return stringObstacle;
    }

    /* Getters */

    public ArrayList<Line2D> getBoundaries() {
        ArrayList<Point2D> vertices = getVertices();
        ArrayList<Line2D> boundaries = new ArrayList<Line2D>();

        for (int i = 0; i < vertices.size() - 1; i++) {
            if (i != vertices.size() - 2)
                boundaries.add(new Line2D.Double(vertices.get(i), vertices.get(i + 1)));
            else
                boundaries.add(new Line2D.Double(vertices.get(i), vertices.get(0)));
        }

        return boundaries;
    }

    public ArrayList<Point2D> getVertices() {
        ArrayList<Point2D> vertices = new ArrayList<Point2D>();
        double[] coords = new double[2];

        for (PathIterator pi = obstacle.getPathIterator(null); !pi.isDone(); pi.next()) {
            pi.currentSegment(coords);
            vertices.add(new Point2D.Double(coords[0], coords[1]));
        }

        return vertices;
    }

    public Polygon getPolygon() {
        return obstacle;
    }
}
