
import java.awt.Graphics2D;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.text.DecimalFormat;

import drawingPanel.DrawingPanel;

public class MainGA {
	public static void main(String[] args) throws IOException {
		Population optimizedPop;
		String selectionMethod = "TOS";
		String trialName = selectionMethod + "-INTELLIGENT";
		
		// Runs the genetic algorithm
		if (Constants.PERFORM_LAB) {
			System.out.println("Performing Lab...");
			File trialData = null;
			FileWriter trialFW = null;
			
			if (Constants.COLLECT_TRIAL_DATA) {
				trialData = new File("C:\\Users\\cbadu\\Documents\\Learning Studios\\Trial Data\\"
						+ "Fitness Limit\\" + selectionMethod + "\\" + trialName + " Trials.txt");
				trialFW = new FileWriter(trialData);
				trialFW.write("\"Trial\",\"Generations\",\"Avg Cost\",\"Min Cost\",\"Max Cost\","
						+ "\"Median Cost\",\"Duration (s)\",\"Speed (gen/s)\"\n");
			}
			
			// Performs multiple genetic algorithm trials
			for (int i = 1; i <= Constants.TRIAL_RUNS; i++) {
				System.out.println("Trial " + i);
				File genData = null;
				FileWriter genFW = null;
				
				if (Constants.COLLECT_GENERATION_DATA) {
					genData = new File("C:\\Users\\cbadu\\Documents\\Learning Studios\\Generation Data\\" +
							trialName + " Trials\\" + trialName + " Trial " + i + ".txt");
					genFW = new FileWriter(genData);
					genFW.write("\"Generation\",\"Avg Length\",\"Diversity\",\"Avg Cost\",\"Min Cost\",\"Max Cost\",\"Median Cost\"\n");
				}
				
				performTrial(trialFW, genFW, i);
			}
			
			System.out.println("\n**** " + trialName + " experiment successfully executed ****");
		} else {
			optimizedPop = performGA();
			optimizedPop.printStatistics();
		}
	}
	
	// Generates multiple populations; Population with highest diversity is chosen as initial population
	public static Population generateInitialPop() {
		Population currentPop = new Population(true, Constants.POPULATION_SIZE);
		
		for (int i = 0; i < Constants.INITIAL_POPULATIONS; i++) {
			Population candidatePop = new Population(true, Constants.POPULATION_SIZE);
			
			if (candidatePop.diversity() > currentPop.diversity())
				currentPop = candidatePop;
		}
		
		return currentPop;
	}
	
	// Perform a genetic algorithm trial for the experiment
	public static Population performTrial(FileWriter trialFW, FileWriter genFW, int trial) throws IOException {
		Population currentGen = generateInitialPop();
		int generation = 1;
		
		double duration = System.nanoTime();
		
		// Iterates through each generation until certain criteria has been met
		do {
			// Collect Data for each Generation: Average Length, Diversity, Average Cost, Minimum Cost, Maximum Cost, Median Cost
			if (Constants.COLLECT_GENERATION_DATA)
				genFW.write(generation + "," + currentGen.avgLength() + "," + currentGen.diversity() + "," + currentGen.avgCost() + "," +
						currentGen.getFittest().getCost() + "," + currentGen.getWorst().getCost() + "," + currentGen.getMedian().getCost() + "\n");
			
			Population nextGen = new Population(false, Constants.POPULATION_SIZE);
			Evolution.evolvePopulation(currentGen, nextGen, generation);
			
			currentGen = nextGen;
			generation++;
		} while (currentGen.getFittest().getCost() > Constants.FITNESS_LIMIT);
		
		duration = -duration + System.nanoTime();
		
		// Collect Data for Trial: Average Cost, Minimum Cost, Maximum Cost, Median Cost, Duration, Speed
		if (Constants.COLLECT_TRIAL_DATA) {
			trialFW.write(trial + "," + generation + "," + currentGen.avgCost() + "," + currentGen.getFittest().getCost() + "," +
					currentGen.getWorst().getCost() + "," + currentGen.getMedian().getCost() + "," + duration * Math.pow(10, -9) +
					"," + generation / (duration * Math.pow(10, -9)) + "\n");
			trialFW.close();
		}
		
		// Collect Data for Optimized Generation
		if (Constants.COLLECT_GENERATION_DATA) {
			genFW.write(generation + "," + currentGen.avgLength() + "," + currentGen.diversity() + "," + currentGen.avgCost() + "," +
					currentGen.getFittest().getCost() + "," + currentGen.getWorst().getCost() + "," + currentGen.getMedian().getCost() + "\n");
			genFW.close();
		}
		
		return currentGen;
	}
	
	// Performs genetic algorithm
	public static Population performGA() {
		DecimalFormat df = new DecimalFormat("000.000");
		DrawingPanel panel = new DrawingPanel(Constants.PANEL_LENGTH + Constants.MARGIN_THICKNESS, Constants.PANEL_HEIGHT + Constants.MARGIN_THICKNESS);
		Graphics2D g = panel.getGraphics();
		Population currentGen = generateInitialPop();
		int generation = 1;
		
		if (Constants.DRAW_FITTEST)
			currentGen.getFittest().drawIndividual(panel, g);
		
		double duration = System.nanoTime();
		
		// Iterates through each generation until generation limit or satisfactory fitness
		do {
			System.out.println("Generation " + generation + ":\t" + df.format(currentGen.getFittest().getCost()) + "\t" +
					currentGen.getFittest().isFeasible());
			
			// Evolves current population
			Population nextGen = new Population(false, Constants.POPULATION_SIZE);
			Evolution.evolvePopulation(currentGen, nextGen, generation);
			
			// Displays fittest individual
			if (Constants.DRAW_FITTEST) {
				if (nextGen.getFittest().getCost() < currentGen.getFittest().getCost())
					nextGen.getFittest().drawIndividual(panel, g);
			}
			
			currentGen = nextGen;
			generation++;
		} while (currentGen.getFittest().getCost() > Constants.FITNESS_LIMIT || !currentGen.getFittest().isFeasible());
		
		duration = -duration + System.nanoTime();
		
		// Print summary of genetic algorithm
		System.out.println("\nDuration: \t" + duration * Math.pow(10, -9) + " seconds");
		System.out.println("Generation: \t" + generation);
		System.out.println("Speed of GA: \t" + generation / (duration * Math.pow(10, -9)) + " generations / second");
		
		return currentGen;
	}
}
