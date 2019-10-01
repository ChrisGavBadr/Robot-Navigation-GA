
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import java.text.DecimalFormat;

enum MutationOperator {
    ADD, DELETE, CHANGE, SHORTEN/*, CORRECT*/
}

public class Individual {

    private ArrayList<Point2D> chromosome = new ArrayList<Point2D>();
    private double cost = 0;

    // Individual Constructor
    Individual() {
    }

    Individual(boolean initialize, int length) {
        chromosome.add(new Point2D.Double(0, 0));

        // Initializes Individual with randomly generates coordinates
        if (initialize) {
            for (int i = 1; i < length - 1; i++)
                chromosome.add(new Point2D.Double(Math.random() * Constants.MAX_X_VALUE, Math.random() * Constants.MAX_Y_VALUE));
        } else {
            for (int i = 1; i < length - 1; i++)
                chromosome.add(new Point2D.Double());
        }

        chromosome.add(Constants.GOAL_NODE);

        cost();
    }

    /* Important Functions */

    public Individual clone() {
        Individual clone = new Individual();

        for (Point2D coord : chromosome)
            clone.addNode(new Point2D.Double(coord.getX(), coord.getY()));

        return clone;
    }

    // Determines whether individual is feasible (i.e. doesn't hit obstacles)
    public boolean isFeasible() {
        for (Obstacle obstacle : Constants.OBSTACLES) {
            ArrayList<Line2D> boundaries = obstacle.getBoundaries();

            for (Line2D boundary : boundaries) {
                for (int i = 0; i < chromosome.size() - 1; i++) {
                    if (getPath(i).intersectsLine(boundary))
                        return false;
                }
            }
        }

        return true;
    }

    // Updates cost of individual
    private double cost() {
        if (isFeasible())
            cost = distanceTraveled();
        else
            cost = distanceTraveled() + Constants.PENALTY_FACTOR * (collisionDist() + 1);

        return cost;
    }

    // Calculates total distance traveled
    public double distanceTraveled() {
        double distance = 0;

        for (int i = 0; i < chromosome.size() - 1; i++)
            distance += chromosome.get(i).distance(chromosome.get(i + 1));

        return distance;
    }

    // Determines points of collision between individual and obstacles
    public Map<Integer, ArrayList<Point2D>> detectCollisions() {
        Map<Integer, ArrayList<Point2D>> collisions = new HashMap<Integer, ArrayList<Point2D>>();

        for (int i = 0; i < chromosome.size() - 1; i++) {
            ArrayList<Point2D> collisionsInPath = detectPathCollisions(i);

            if (i != 0 && collisions.get(i - 1) != null) {
                for (Point2D previous : collisions.get(i - 1)) {
                    for (int j = 0; j < collisionsInPath.size(); j++) {
                        if (Math.abs(previous.getX() - collisionsInPath.get(j).getX()) < Constants.FLOAT_THRESHOLD
                                && Math.abs(previous.getY() - collisionsInPath.get(j).getY()) < Constants.FLOAT_THRESHOLD) {
                            collisionsInPath.remove(j);
                            break;
                        }
                    }
                }
            }

            if (collisionsInPath.size() != 0)
                collisions.put(i, collisionsInPath);
        }

        return collisions;
    }

    // Determines points of collision between specific path and obstacles
    private ArrayList<Point2D> detectPathCollisions(int pathIndex) {
        ArrayList<Point2D> collisionsInPath = new ArrayList<Point2D>();

        for (Obstacle obstacle : Constants.OBSTACLES) {
            ArrayList<Line2D> boundaries = obstacle.getBoundaries();

            for (Line2D boundary : boundaries) {
                if (getPath(pathIndex).intersectsLine(boundary)) {
                    Line2D path = getPath(pathIndex);

                    // Line AB represented as a1x + b1y = c1
                    double a1 = path.getY2() - path.getY1();
                    double b1 = path.getX1() - path.getX2();
                    double c1 = a1 * path.getX1() + b1 * path.getY1();

                    // Line CD represented as a2x + b2y = c2
                    double a2 = boundary.getY2() - boundary.getY1();
                    double b2 = boundary.getX1() - boundary.getX2();
                    double c2 = a2 * boundary.getX1() + b2 * boundary.getY1();

                    double determinant = a1 * b2 - a2 * b1;

                    if (determinant != 0) {
                        double xIntersect = (b2 * c1 - b1 * c2) / determinant;
                        double yIntersect = (a1 * c2 - a2 * c1) / determinant;
                        boolean uniqueOccurrence = true;

                        for (Point2D collision : collisionsInPath) {
                            if (Math.abs(collision.getX() - xIntersect) < Constants.FLOAT_THRESHOLD
                                    && Math.abs(collision.getY() - yIntersect) < Constants.FLOAT_THRESHOLD) {
                                uniqueOccurrence = false;
                                break;
                            }
                        }

                        if (uniqueOccurrence)
                            collisionsInPath.add(new Point2D.Double(xIntersect, yIntersect));
                    }
                }
            }
        }

        return collisionsInPath;
    }

    // Calculates total distance traveled through obstacles
    public double collisionDist() {
        Map<Integer, ArrayList<Point2D>> collisionMap = detectCollisions();
        Set<Map.Entry<Integer, ArrayList<Point2D>>> collisionSet = collisionMap.entrySet();
        int offset = 0;
        int prevCollisionPath = 0;
        double collisionDist = 0;
        boolean isInObstacle = false;

        // Iterates through each Path-Collision Points Map
        for (Map.Entry<Integer, ArrayList<Point2D>> collidedPath : collisionSet) {
            ArrayList<Point2D> collisionPoints = collidedPath.getValue();

            // If node is in obstacle, then add remaining collision distance
            if (isInObstacle) {
                for (int i = prevCollisionPath; i < collidedPath.getKey(); i++)
                    collisionDist += chromosome.get(i).distance(chromosome.get(i + 1));

                collisionDist += chromosome.get(collidedPath.getKey()).distance(collisionPoints.get(0));
            }

            // Adds to collision distance for one path
            for (int i = offset; i < collisionPoints.size() - 1; i += 2)
                collisionDist += collisionPoints.get(i).distance(collisionPoints.get(i + 1));

            // If node is in obstacle, then add distance from intersection to node
            if ((collidedPath.getValue().size() - offset) % 2 == 1) {
                isInObstacle = true;
                collisionDist += collisionPoints.get(collisionPoints.size() - 1).distance(chromosome.get(collidedPath.getKey() + 1));
                offset = 1;
            } else {
                isInObstacle = false;
                offset = 0;
            }

            // Get index of this collision path; Used if isInObstacle == true
            prevCollisionPath = collidedPath.getKey() + 1;
        }

        return collisionDist;
    }

    // Draws environment with obstacles
    private void drawEnvironment(DrawingPanel panel, Graphics2D g) {
        panel.clear();
        g.setColor(Color.BLACK);

        for (Obstacle obstacle : Constants.OBSTACLES)
            obstacle.drawObstacle(panel, g);
    }

    // Draws path of individual on the environment
    public void drawIndividual(DrawingPanel panel, Graphics2D g) {
        DecimalFormat df = new DecimalFormat("000.000");

        drawEnvironment(panel, g);
        g.setColor(Color.BLUE);

        for (int i = 0; i < chromosome.size() - 1; i++) {
            g.fillOval((int) (chromosome.get(i).getX() * Constants.X_SCALE) + (Constants.MARGIN_THICKNESS - Constants.POINT_RADIUS + 1) / 2,
                    (int) (-chromosome.get(i).getY() * Constants.Y_SCALE + Constants.PANEL_HEIGHT) + (Constants.MARGIN_THICKNESS - Constants.POINT_RADIUS + 1) / 2,
                    Constants.POINT_RADIUS, Constants.POINT_RADIUS);

            g.drawLine((int) (chromosome.get(i).getX() * Constants.X_SCALE) + Constants.MARGIN_THICKNESS / 2,
                    (int) (-chromosome.get(i).getY() * Constants.Y_SCALE + Constants.PANEL_HEIGHT) + Constants.MARGIN_THICKNESS / 2,
                    (int) (chromosome.get(i + 1).getX() * Constants.X_SCALE) + Constants.MARGIN_THICKNESS / 2,
                    (int) (-chromosome.get(i + 1).getY() * Constants.Y_SCALE + Constants.PANEL_HEIGHT) + Constants.MARGIN_THICKNESS / 2);
        }

        g.fillOval((int) (chromosome.get(chromosome.size() - 1).getX() * Constants.X_SCALE) + (Constants.MARGIN_THICKNESS - Constants.POINT_RADIUS + 1) / 2,
                (int) (-chromosome.get(chromosome.size() - 1).getY() * Constants.Y_SCALE + Constants.PANEL_HEIGHT) + (Constants.MARGIN_THICKNESS - Constants.POINT_RADIUS + 1) / 2,
                Constants.POINT_RADIUS, Constants.POINT_RADIUS);

        g.setColor(Color.BLACK);
        g.drawString("Fitness: " + df.format(cost), Constants.PANEL_LENGTH - 150 + Constants.MARGIN_THICKNESS / 2,
                Constants.PANEL_HEIGHT - 25 + Constants.MARGIN_THICKNESS / 2);

        String status;

        if (isFeasible())
            status = "Feasible";
        else
            status = "Infeasible";

        g.drawString("Feasibility: " + status, Constants.PANEL_LENGTH - 150 + Constants.MARGIN_THICKNESS / 2, Constants.PANEL_HEIGHT - 10 + Constants.MARGIN_THICKNESS / 2);
        g.drawString("Chromosome Length: " + chromosome.size(), Constants.PANEL_LENGTH - 150 + Constants.MARGIN_THICKNESS / 2,
                Constants.PANEL_HEIGHT + 5 + Constants.MARGIN_THICKNESS / 2);

        highlightCollision(panel, g);
    }

    // Highlights collision areas on individual's path
    private void highlightCollision(DrawingPanel panel, Graphics2D g) {
        Map<Integer, ArrayList<Point2D>> collisionMap = detectCollisions();
        Set<Map.Entry<Integer, ArrayList<Point2D>>> collisionSet = collisionMap.entrySet();
        int offset = 0;
        int prevCollisionPath = 0;
        boolean isInObstacle = false;

        g.setColor(Color.RED);

        // Iterates through each Path-Collision Points Map
        for (Map.Entry<Integer, ArrayList<Point2D>> collidedPath : collisionSet) {
            ArrayList<Point2D> collisionPoints = collidedPath.getValue();

            // If node is in obstacle, then highlight collision areas
            if (isInObstacle) {
                for (int i = prevCollisionPath; i < collidedPath.getKey(); i++) {
                    g.drawLine((int) (chromosome.get(i).getX() * Constants.X_SCALE) + Constants.MARGIN_THICKNESS / 2,
                            (int) (-chromosome.get(i).getY() * Constants.Y_SCALE + Constants.PANEL_HEIGHT) + Constants.MARGIN_THICKNESS / 2,
                            (int) (chromosome.get(i + 1).getX() * Constants.X_SCALE) + Constants.MARGIN_THICKNESS / 2,
                            (int) (-chromosome.get(i + 1).getY() * Constants.Y_SCALE + Constants.PANEL_HEIGHT) + Constants.MARGIN_THICKNESS / 2);
                }

                g.drawLine((int) (chromosome.get(collidedPath.getKey()).getX() * Constants.X_SCALE) + Constants.MARGIN_THICKNESS / 2,
                        (int) (-chromosome.get(collidedPath.getKey()).getY() * Constants.Y_SCALE + Constants.PANEL_HEIGHT) + Constants.MARGIN_THICKNESS / 2,
                        (int) (collisionPoints.get(0).getX() * Constants.X_SCALE) + Constants.MARGIN_THICKNESS / 2,
                        (int) (-collisionPoints.get(0).getY() * Constants.Y_SCALE + Constants.PANEL_HEIGHT) + Constants.MARGIN_THICKNESS / 2);
            }

            // Highlights collision areas
            for (int i = offset; i < collisionPoints.size() - 1; i += 2) {
                g.drawLine((int) (collisionPoints.get(i).getX() * Constants.X_SCALE) + Constants.MARGIN_THICKNESS / 2,
                        (int) (-collisionPoints.get(i).getY() * Constants.Y_SCALE + Constants.PANEL_HEIGHT) + Constants.MARGIN_THICKNESS / 2,
                        (int) (collisionPoints.get(i + 1).getX() * Constants.X_SCALE) + Constants.MARGIN_THICKNESS / 2,
                        (int) (-collisionPoints.get(i + 1).getY() * Constants.Y_SCALE + Constants.PANEL_HEIGHT) + Constants.MARGIN_THICKNESS / 2);
            }

            // If node is in obstacle, then highlight collision area from intersection to node
            if ((collidedPath.getValue().size() - offset) % 2 == 1) {
                isInObstacle = true;
                g.drawLine((int) (collisionPoints.get(collisionPoints.size() - 1).getX() * Constants.X_SCALE) + Constants.MARGIN_THICKNESS / 2,
                        (int) (-collisionPoints.get(collisionPoints.size() - 1).getY() * Constants.Y_SCALE + Constants.PANEL_HEIGHT) + Constants.MARGIN_THICKNESS / 2,
                        (int) (chromosome.get(collidedPath.getKey() + 1).getX() * Constants.X_SCALE) + Constants.MARGIN_THICKNESS / 2,
                        (int) (-chromosome.get(collidedPath.getKey() + 1).getY() * Constants.Y_SCALE + Constants.PANEL_HEIGHT) + Constants.MARGIN_THICKNESS / 2);
                offset = 1;
            } else {
                isInObstacle = false;
                offset = 0;
            }

            // Get index of this collision path; Used if isInObstacle == true
            prevCollisionPath = collidedPath.getKey() + 1;
        }
    }

    public String toString() {
        DecimalFormat df = new DecimalFormat("000.00");
        String stringObstacle = "{(" + df.format(chromosome.get(0).getX()) + ", " + df.format(chromosome.get(0).getY()) + ")";

        for (int i = 1; i < chromosome.size(); i++)
            stringObstacle += ", (" + df.format(chromosome.get(i).getX()) + ", " + df.format(chromosome.get(i).getY()) + ")";

        stringObstacle += "}";

        return stringObstacle;
    }

    // Mutation Operators (Add/Delete/Change Node and Shorten/Correct the Path)

    // Mutates individual
    public void mutate() {
        MutationOperator[] mutations = MutationOperator.values();

        // Performs random mutation
        switch (mutations[(int) (Math.random() * mutations.length)]) {
            case ADD:
                if (chromosome.size() != Constants.MAX_CHROMOSOME_LENGTH) {
                    chromosome.add((int) (Math.random() * (chromosome.size() - 2)) + 1,
                            new Point2D.Double(Math.random() * Constants.MAX_X_VALUE, Math.random() * Constants.MAX_Y_VALUE));
                    break;
                }
            case DELETE:
                if (chromosome.size() != Constants.MIN_CHROMOSOME_LENGTH) {
                    chromosome.remove((int) (Math.random() * (chromosome.size() - 2)) + 1);
                    break;
                }
            case CHANGE:
                chromosome.set((int) (Math.random() * (chromosome.size() - 2)) + 1,
                        new Point2D.Double(Math.random() * Constants.MAX_X_VALUE, Math.random() * Constants.MAX_Y_VALUE));
                break;
            case SHORTEN:
                shortenPath();
                break;
			/* case CORRECT:
				correctPath();
				break; */
            default:
                chromosome.set((int) (Math.random() * (chromosome.size() - 2)) + 1,
                        new Point2D.Double(Math.random() * Constants.MAX_X_VALUE, Math.random() * Constants.MAX_Y_VALUE));
        }

        cost();
    }

    public void addNode(int index, double x, double y) {
        chromosome.add(index, new Point2D.Double(x, y));
        cost();
    }

    public void addNode(int index, Point2D coord) {
        chromosome.add(index, new Point2D.Double(coord.getX(), coord.getY()));
        cost();
    }

    public void addNode(double x, double y) {
        chromosome.add(new Point2D.Double(x, y));
        cost();
    }

    public void addNode(Point2D coord) {
        chromosome.add(new Point2D.Double(coord.getX(), coord.getY()));
        cost();
    }

    public void deleteNode(int index) {
        chromosome.remove(index);
        cost();
    }

    public void changeNode(int index, double x, double y) {
        chromosome.set(index, new Point2D.Double(x, y));
        cost();
    }

    public void changeNode(int index, Point2D coord) {
        chromosome.set(index, new Point2D.Double(coord.getX(), coord.getY()));
        cost();
    }

    // Removes unnecessary coordinates in individual
    public void shortenPath() {
        boolean isShortest;

        do {
            isShortest = true;

            for (int i = 0; i < chromosome.size() - 2; i++) {
                if (detectPathCollisions(i).size() == 0 && detectPathCollisions(i + 1).size() == 0
                        && chromosome.size() > Constants.MIN_CHROMOSOME_LENGTH) {
                    chromosome.remove(i + 1);
                    isShortest = false;
                }
            }
        } while (!isShortest);

        cost();
    }
	
	/* public void correctPath() {
		for (int i = 1; i < chromosome.size() - 1; i++) {
			boolean intersects = false;
			double x;
			double y;
			
			do {
				x = Math.random() * Constants.GOAL_NODE.getX();
				y = Math.random() * Constants.GOAL_NODE.getY();
				
				Line2D path = new Line2D.Double(new Point2D.Double(x, y), chromosome.get(i + 1));
				
				for (Obstacle obstacle : Constants.OBSTACLES) {
					if (obstacle.intersects(path)) {
						intersects = true;
						break;
					}
				}
			} while (intersects);
			
			chromosome.set(i, new Point2D.Double(x, y));
		}
		
		cost();
	} */

    /* Getters */

    public ArrayList<Point2D> getChromosome() {
        return chromosome;
    }

    public double getCost() {
        return cost;
    }

    public Point2D getNode(int index) {
        return chromosome.get(index);
    }

    public Line2D getPath(int index) {
        return new Line2D.Double(chromosome.get(index), chromosome.get(index + 1));
    }

    public int getLength() {
        return chromosome.size();
    }
}
