import java.awt.geom.Point2D;

class Constants {
    // Experiment Settings
    static final int TRIAL_RUNS = 4;
    static final boolean PERFORM_LAB = false;
    static final boolean COLLECT_TRIAL_DATA = false;
    static final boolean COLLECT_GENERATION_DATA = false;

    // Initial Parameters for Population
    static final int POPULATION_SIZE = 100;
    static final int MAX_CHROMOSOME_LENGTH = 10;
    static final int MIN_CHROMOSOME_LENGTH = 5;
    static final int MAX_X_VALUE = 120;
    static final int MAX_Y_VALUE = 80;
    static final Point2D GOAL_NODE = new Point2D.Double(120, 80);

    // Initial Parameters for Algorithm
    static final int INITIAL_POPULATIONS = 5;            // # of candidate initial populations
    static final int ELITE_SURVIVORS = 5;                // # of fittest individuals selected per generation
    static final int TOURNAMENT_SIZE = 5;                // Size of group for tournament selection
    static final int COMBINED_SELECTION_RUNS = 5;        // Runs carried out for each selection method in Combined Selection
    static final double CROSSOVER_PROBABILITY = 0.90;    // Probability of crossover between two parents
    static final double MUTATION_PROBABILITY = 0.25;        // Probability of mutation per individual
    static final double PENALTY_FACTOR = 20;                // Multiplied with each individual's collision distance to be added to fitness

    // Termination Conditions
    static final int GENERATION_LIMIT = 1000000;
    static final double FITNESS_LIMIT = 160;

    // Environment Setup
    private static FileManager fm = new FileManager("C:\\Users\\chris\\IdeaProjects\\"
            + "RobotNavigationGA\\Environment 1.txt");
    static final Obstacle[] OBSTACLES = fm.getObstacles();

    // Graphics Constants
    static final boolean DRAW_FITTEST = true;
    static final int PANEL_LENGTH = 600;
    static final int PANEL_HEIGHT = 400;
    static final int MARGIN_THICKNESS = 75;
    static final int POINT_RADIUS = 5;
    static final double X_SCALE = (double) PANEL_LENGTH / MAX_X_VALUE;
    static final double Y_SCALE = (double) PANEL_HEIGHT / MAX_Y_VALUE;

    // Miscellaneous Constants
    static final double FLOAT_THRESHOLD = 0.00000001;    // Used for determining the equality of two floats
}
