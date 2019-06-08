
import java.text.DecimalFormat;

public class Population {
	
	private Individual[] population;
	
	// Population Constructor
	Population(boolean initialize, int populationSize) {
		population = new Individual[populationSize];
		
		// Initializes population with randomly generated individuals
		if (initialize) {
			for (int i = 0; i < populationSize; i++) {
				population[i] = new Individual(true, (int) (Math.random() * (Constants.MAX_CHROMOSOME_LENGTH -
						Constants.MIN_CHROMOSOME_LENGTH + 1) + Constants.MIN_CHROMOSOME_LENGTH));
			}
			
			quickSortPop(0, population.length - 1);
		}
	}
	
	/* Important Functions */
	
	// Quick Sorts population by fitness
	public void quickSortPop(int low, int high) {		
		if (low < high) {
			int pi = partition(low, high);
			
			quickSortPop(low, pi - 1);
			quickSortPop(pi + 1, high);
		}
	}
	
	// Used for Quick Sort
	private int partition(int low, int high) {
		double pivot = population[high].getCost();
		int i = low - 1;
		
		for (int j = low; j < high; j++) {
			if (population[j].getCost() < pivot) {
				i++;
				
				Individual temp = population[i];
				population[i] = population[j];
				population[j] = temp;
			}
		}
		
		Individual temp = population[i + 1];
		population[i + 1] = population[high];
		population[high] = temp;
		
		return i + 1;
	}
	
	// Calculates total cost / fitness
	public double totalCost() {
		double totalCost = 0;
		
		for (Individual indiv : population)
			totalCost += indiv.getCost();
		
		return totalCost;
	}
	
	// Calculates average cost / fitness
	public double avgCost() {
		return totalCost() / population.length;
	}
	
	// Calculates average chromosome length of population
	public double avgLength() {
		double chromosomeSum = 0;
		
		for (Individual indiv : population)
			chromosomeSum += indiv.getLength();
		
		return chromosomeSum / population.length;
	}
	
	// Calculates population diversity (Standard Deviation of fitnesses)
	public double diversity() {
		double averageCost = avgCost();
		double squareDeviationSum = 0;
		
		for (Individual indiv : population)
			squareDeviationSum += Math.pow(indiv.getCost() - averageCost, 2);
		
		return Math.sqrt(squareDeviationSum / population.length);
	}
	
	public void saveIndividual(int index, Individual indiv) {
		population[index] = indiv;
	}
	
	public String toString() {
		DecimalFormat df = new DecimalFormat("0000.000");
		String stringPopulation = "";
		
		for (int i = 0; i < population.length; i++)
			stringPopulation += "Individual " + (i + 1) + "\t | Fit: " + df.format(population[i].getCost())
				+ " | " + population[i].toString() + "\n";
		
		return stringPopulation;
	}
	
	// Prints statistics of population
	public void printStatistics() {
		DecimalFormat df = new DecimalFormat("0000.000");
		
		System.out.println("\nPopulation\n" + toString());
		System.out.println("Fittest \t | Fit: " + df.format(getFittest().getCost()) + " | " +  getFittest().toString());
		System.out.println("Least Fit \t | Fit: " + df.format(getWorst().getCost()) + " | " + getWorst().toString());
		System.out.println("Median Fit \t | Fit: " + df.format(getMedian().getCost()) + " | " + getMedian().toString());
		System.out.println("\nAverage Cost: \t" + df.format(avgCost()));
		System.out.println("Diversity: \t" + df.format(diversity()));
	}
	
	/* Getters */
	
	public Individual[] getPopulation() {
		return population;
	}
	
	public int getSize() {
		return population.length;
	}
	
	public Individual getIndividual(int index) {
		return population[index];
	}
	
	public Individual getFittest() {
		int index = 0;
		
		for (int i = 1; i < population.length; i++) {
			if (population[index].getCost() > population[i].getCost())
				index = i;
		}
		
		return population[index];
	}
	
	public Individual getMedian() {
		quickSortPop(0, population.length - 1);
		return population[(population.length - 1) / 2];
	}
	
	public Individual getWorst() {
		int index = 0;
		
		for (int i = 1; i < population.length; i++) {
			if (population[index].getCost() < population[i].getCost())
				index = i;
		}
		
		return population[index];
	}
}
