
import java.util.Arrays;

public class Evolution {

    // Evolves population to next generation
    public static void evolvePopulation(Population currentGen, Population nextGen, int generation) {
        SelectionMethod selection = SelectionMethod.TOS;
        CrossoverOperator crossover = CrossoverOperator.INTELLIGENT;

        // Performs natural selection and appends survivors to next generation
        switch (selection) {
            case RWS:
                rouletteWheelSelection(currentGen, nextGen);
                break;
            case SUS:
                stochasticUniversalSampling(currentGen, nextGen);
                break;
            case LRS:
                linearRankSelection(currentGen, nextGen);
                break;
            case TOS:
                tournamentSelection(currentGen, nextGen);
                break;
            case TRS:
                truncationSelection(currentGen, nextGen);
                break;
            case CS:
                combinedSelection(currentGen, nextGen, generation);
        }

        // Performs crossover and appends offspring to next generation
        for (int i = 0; i < nextGen.getSize() / 2 - 1; i += 2) {
            if (Math.random() <= Constants.CROSSOVER_PROBABILITY) {
                Individual[] offspring = new Individual[2];

                switch (crossover) {
                    case ONE_POINT:
                        offspring = onePointCrossover(nextGen.getIndividual(i), nextGen.getIndividual(i + 1));
                        break;
                    case K_POINT:
                        offspring = kPointCrossover(nextGen.getIndividual(i), nextGen.getIndividual(i + 1));
                        break;
                    case INTELLIGENT:
                        offspring[0] = intelligentCrossover(nextGen.getIndividual(i), nextGen.getIndividual(i + 1));

                        if (i + 2 < nextGen.getSize() / 2 - 1)
                            offspring[1] = intelligentCrossover(nextGen.getIndividual(i + 1), nextGen.getIndividual(i + 2));
                        else
                            offspring[1] = intelligentCrossover(nextGen.getIndividual(0), nextGen.getIndividual(nextGen.getSize() / 2 - 1));
                }

                offspring[0].mutate();
                offspring[1].mutate();

                nextGen.saveIndividual(i + nextGen.getSize() / 2, offspring[0]);
                nextGen.saveIndividual(i + 1 + nextGen.getSize() / 2, offspring[1]);
            } else {
                nextGen.saveIndividual(i + nextGen.getSize() / 2, nextGen.getIndividual(i).clone());
                nextGen.saveIndividual(i + 1 + nextGen.getSize() / 2, nextGen.getIndividual(i + 1).clone());
            }
        }

        // Special case if population size is odd
        if (nextGen.getSize() % 2 == 1 || (nextGen.getSize() / 2) % 2 == 1)
            nextGen.saveIndividual(nextGen.getSize() - 1, new Individual(true, (int) (Math.random() * (Constants.MAX_CHROMOSOME_LENGTH -
                    Constants.MIN_CHROMOSOME_LENGTH + 1) + Constants.MIN_CHROMOSOME_LENGTH)));

        nextGen.quickSortPop(0, nextGen.getSize() - 1);

        // Each non-elite individual is subjected to mutation under some mutation probability
        for (int i = Constants.ELITE_SURVIVORS; i < nextGen.getSize(); i++) {
            if (Math.random() <= Constants.MUTATION_PROBABILITY)
                nextGen.getIndividual(i).mutate();
        }

        nextGen.quickSortPop(0, nextGen.getSize() - 1);
    }

    /* Selection Methods (RWS, SUS, LRS, TOS, TRS, and CS) */

    // Selects portion of the fittest individuals
    public static void performElitism(Population population, Population eliteSurvivors) {
        population.quickSortPop(0, population.getSize() - 1);
        for (int i = 0; i < Constants.ELITE_SURVIVORS; i++)
            eliteSurvivors.saveIndividual(i, population.getIndividual(i));
    }

    // Probability of selection is ratio of individual's fitness to total fitness sum
    public static void rouletteWheelSelection(Population population, Population survivors) {
        double totalFitness = population.totalCost();
        double maxMinSum = population.getFittest().getCost() + population.getWorst().getCost();

        performElitism(population, survivors);

        for (int i = Constants.ELITE_SURVIVORS; i < population.getSize() / 2; i++) {
            double iSum = Math.random() * totalFitness;
            int j = 0;

            do {
                iSum -= maxMinSum - population.getIndividual(j).getCost();
                j++;
            } while (iSum > 0 && j < population.getSize());

            survivors.saveIndividual(i, population.getIndividual(j - 1).clone());
        }
    }

    // Variant of RWS; Reduces risk of premature convergence
    public static void stochasticUniversalSampling(Population population, Population survivors) {
        double sum = population.getFittest().getCost();
        double delta = Math.random() * population.avgCost();
        int i = Constants.ELITE_SURVIVORS;
        int j = 0;

        performElitism(population, survivors);

        do {
            if (delta < sum) {
                survivors.saveIndividual(i, population.getIndividual(j).clone());
                delta += sum;
                i++;
            } else if (j + 1 == population.getSize()) {
                sum = population.getFittest().getCost();
                delta = Math.random() * population.avgCost();
                j = 0;
            } else {
                j++;
                sum += population.getIndividual(j).getCost();
            }
        } while (i < population.getSize() / 2);
    }

    // Variant of RWS; Selection probability is ratio of individual's rank to total rank sum; Reduces risk of premature convergence
    public static void linearRankSelection(Population population, Population survivors) {
        performElitism(population, survivors);

        for (int i = Constants.ELITE_SURVIVORS; i < population.getSize() / 2; i++) {
            for (int j = 0; j < population.getSize(); j++) {
                if ((population.getSize() - j) / (population.getSize() * (population.getSize() - 1.0))
                        <= Math.random() * (1.0 / (population.getSize() - 2.001))) {
                    survivors.saveIndividual(i, population.getIndividual(j).clone());
                    break;
                }
            }

            if (survivors.getPopulation()[i] == null)
                i--;
        }
    }

    // Selects fittest individual from each random group of random individuals
    public static void tournamentSelection(Population population, Population survivors) {
        performElitism(population, survivors);

        for (int i = Constants.ELITE_SURVIVORS; i < population.getSize() / 2; i++) {
            Population tournament = new Population(false, Constants.TOURNAMENT_SIZE);

            for (int j = 0; j < Constants.TOURNAMENT_SIZE; j++)
                tournament.saveIndividual(j, population.getIndividual((int) (Math.random() * population.getSize())));

            survivors.saveIndividual(i, tournament.getFittest().clone());
        }
    }

    // Selects first half of the fittest individuals
    public static void truncationSelection(Population population, Population survivors) {
        performElitism(population, survivors);

        for (int i = 0; i < population.getSize() / 2; i++)
            survivors.saveIndividual(i, population.getIndividual(i).clone());
    }

    // Determines and performs most reliable selection method
    public static void combinedSelection(Population currentGen, Population nextGen, int generation) {
        CSMethod[] candidateMethod = CSMethod.values();
        CSMethod recommendedMethod = candidateMethod[0];

        // Determines most reliable selection method
        for (int i = 1; i < candidateMethod.length; i++) {
            if (calcReliability(currentGen, generation, recommendedMethod) < calcReliability(currentGen, generation, candidateMethod[i]))
                recommendedMethod = candidateMethod[i];
        }

        // Performs most reliable selection method
        switch (recommendedMethod) {
            case RWS:
                rouletteWheelSelection(currentGen, nextGen);
                break;
            case SUS:
                stochasticUniversalSampling(currentGen, nextGen);
                break;
            case LRS:
                linearRankSelection(currentGen, nextGen);
                break;
            case TOS:
                tournamentSelection(currentGen, nextGen);
                break;
            case TRS:
                truncationSelection(currentGen, nextGen);
        }
    }

    // Calculates reliability of selection method; Depends on mean diversity and population quality
    public static double calcReliability(Population population, int generation, CSMethod method) {
        double highestCost = population.getFittest().getCost();
        double lowestCost = population.getWorst().getCost();
        double meanDiversity = 0;

        // Performs multiple runs of selection
        for (int i = 0; i < Constants.COMBINED_SELECTION_RUNS; i++) {
            Population survivors = new Population(false, population.getSize() / 2);

            switch (method) {
                case RWS:
                    rouletteWheelSelection(population, survivors);
                    break;
                case SUS:
                    stochasticUniversalSampling(population, survivors);
                    break;
                case LRS:
                    linearRankSelection(population, survivors);
                    break;
                case TOS:
                    tournamentSelection(population, survivors);
                    break;
                case TRS:
                    truncationSelection(population, survivors);
            }

            if (highestCost < survivors.getFittest().getCost())
                highestCost = survivors.getFittest().getCost();
            if (lowestCost > survivors.getWorst().getCost())
                lowestCost = survivors.getWorst().getCost();

            meanDiversity += survivors.diversity();
        }

        meanDiversity /= Constants.COMBINED_SELECTION_RUNS;

        // Returns reliability value of selection method
        return meanDiversity / generation + (generation - 1) * lowestCost
                / Math.sqrt(Math.pow(highestCost, 2) + Math.pow(lowestCost, 2)) / generation;
    }

    /* Crossover Operators (1-Point, K-Point, and Intelligent) */

    // Swaps genes of parents over one crossover point to produce 2 offspring
    public static Individual[] onePointCrossover(Individual parent1, Individual parent2) {
        Individual[] offspring = new Individual[2];
        offspring[0] = new Individual(false, parent2.getLength());
        offspring[1] = new Individual(false, parent1.getLength());

        int crossoverPoint = (int) (Math.random() * (Math.min(parent1.getLength(), parent2.getLength()) - 3)) + 2;

        for (int i = 0; i < crossoverPoint; i++) {
            offspring[0].changeNode(i, parent1.getNode(i));
            offspring[1].changeNode(i, parent2.getNode(i));
        }

        for (int i = crossoverPoint; i < offspring[0].getLength(); i++)
            offspring[0].changeNode(i, parent2.getNode(i));

        for (int i = crossoverPoint; i < offspring[1].getLength(); i++)
            offspring[1].changeNode(i, parent1.getNode(i));

        return offspring;
    }

    // Swaps genes of parents over a random number of crossover points to produce 2 offspring
    public static Individual[] kPointCrossover(Individual parent1, Individual parent2) {
        Individual[] offspring = new Individual[2];
        offspring[0] = new Individual(false, parent2.getLength());
        offspring[1] = new Individual(false, parent1.getLength());

        int[] crossoverPoints = new int[(int) (Math.random() * (Math.min(parent1.getLength(), parent2.getLength()) - 3)) + 1];

        // Creates an array of random crossover points
        for (int i = 0; i < crossoverPoints.length; i++) {
            boolean uniqueOccurrence;

            do {
                uniqueOccurrence = true;
                crossoverPoints[i] = (int) (Math.random() * (Math.min(parent1.getLength(),
                        parent2.getLength()) - 3)) + 2;

                for (int j = 0; j < i; j++) {
                    if (crossoverPoints[i] == crossoverPoints[j])
                        uniqueOccurrence = false;
                }
            } while (!uniqueOccurrence);
        }

        Arrays.sort(crossoverPoints);

        for (int i = 0; i < crossoverPoints[0]; i++) {
            offspring[0].changeNode(i, parent1.getNode(i));
            offspring[1].changeNode(i, parent2.getNode(i));
        }

        for (int i = 0; i < 2; i++) {
            for (int j = i; j < crossoverPoints.length - 1; j += 2) {
                for (int k = crossoverPoints[j]; k < crossoverPoints[j + 1]; k++) {
                    offspring[0].changeNode(k, parent2.getNode(k));
                    offspring[1].changeNode(k, parent1.getNode(k));
                }
            }
        }

        for (int i = crossoverPoints[crossoverPoints.length - 1]; i < offspring[0].getLength(); i++)
            offspring[0].changeNode(i, parent2.getNode(i));

        for (int i = crossoverPoints[crossoverPoints.length - 1]; i < offspring[1].getLength(); i++)
            offspring[1].changeNode(i, parent1.getNode(i));

        return offspring;
    }

    // Performs intelligent crossover to produce 1 offspring
    public static Individual intelligentCrossover(Individual parent1, Individual parent2) {
        Individual offspring = new Individual();
        boolean parent1Selected = false;
        int i = 0;

        do {
            if (i < Math.min(parent1.getLength(), parent2.getLength()) - 1
                    && parent1.getNode(i).distance(parent2.getNode(i)) < 2) {
                boolean p1IsFeasible = parent1.isFeasible();
                boolean p2IsFeasible = parent2.isFeasible();

                if (p1IsFeasible == p2IsFeasible && parent1.getNode(i + 1).distance(Constants.GOAL_NODE)
                        < parent2.getNode(i + 1).distance(Constants.GOAL_NODE) + Constants.FLOAT_THRESHOLD) {
                    offspring.addNode(i, parent1.getNode(i));
                    parent1Selected = true;
                } else if (p1IsFeasible == p2IsFeasible) {
                    offspring.addNode(i, parent2.getNode(i));
                    parent1Selected = false;
                } else if (p1IsFeasible) {
                    offspring.addNode(i, parent1.getNode(i));
                    parent1Selected = true;
                } else {
                    offspring.addNode(i, parent2.getNode(i));
                    parent1Selected = false;
                }
            } else if (parent1Selected)
                offspring.addNode(parent1.getNode(i));
            else
                offspring.addNode(parent2.getNode(i));

            i++;
        } while (offspring.getNode(i - 1).getX() != Constants.GOAL_NODE.getX()
                || offspring.getNode(i - 1).getY() != Constants.GOAL_NODE.getY());

        return offspring;
    }
}
