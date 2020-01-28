import java.awt.geom.Point2D;

public class Constants {
    // Experiment Settings
    public static final int TRIAL_RUNS = 4;
    public static final boolean PERFORM_LAB = false;
    public static final boolean COLLECT_TRIAL_DATA = false;
    public static final boolean COLLECT_GENERATION_DATA = false;

    // Initial Parameters for Population
    public static final int POPULATION_SIZE = 100;
    public static final int MAX_CHROMOSOME_LENGTH = 10;
    public static final int MIN_CHROMOSOME_LENGTH = 5;
    public static final int MAX_X_VALUE = 120;
    public static final int MAX_Y_VALUE = 80;
    public static final Point2D GOAL_NODE = new Point2D.Double(120, 80);

    // Initial Parameters for Algorithm
    public static final int INITIAL_POPULATIONS = 5;            // # of candidate initial populations
    public static final int ELITE_SURVIVORS = 5;                // # of fittest individuals selected per generation
    public static final int TOURNAMENT_SIZE = 5;                // Size of group for tournament selection
    public static final int COMBINED_SELECTION_RUNS = 5;        // Runs carried out for each selection method in Combined Selection
    public static final double CROSSOVER_PROBABILITY = 0.90;    // Probability of crossover between two parents
    public static final double MUTATION_PROBABILITY = 0.25;     // Probability of mutation per individual
    public static final double PENALTY_FACTOR = 20;             // Multiplied with each individual's collision distance to be added to fitness

    // Termination Conditions
    public static final int GENERATION_LIMIT = 1000;
    public static final double FITNESS_LIMIT = 155;

    // Environment Setup
    private static FileManager fm = new FileManager("C:\\Users\\chris\\IdeaProjects\\"
            + "RobotNavigationGA\\Environment 1.txt");
    public static final Obstacle[] OBSTACLES = fm.getObstacles();

    // Graphics Constants
    public static final boolean DRAW_FITTEST = true;
    public static final int PANEL_LENGTH = 600;
    public static final int PANEL_HEIGHT = 400;
    public static final int MARGIN_THICKNESS = 75;
    public static final int POINT_RADIUS = 5;
    public static final double X_SCALE = (double) PANEL_LENGTH / MAX_X_VALUE;
    public static final double Y_SCALE = (double) PANEL_HEIGHT / MAX_Y_VALUE;

    // Miscellaneous Constants
    public static final double FLOAT_THRESHOLD = 0.00000001;    // Used for determining the equality of two floats
}
