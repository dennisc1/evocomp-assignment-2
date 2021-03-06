import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by Dennis on 22/03/2017.
 */
public class GLS extends BaseAlgorithm {

    public int nIterationsConverged = 0;

    private int populationSize;

    Population population;

    GLS(Graph graph, String localSearchType, boolean skipConflictlessNodes, int maxLocalOptima, int maxCPUTime, int populationSize) {
        super(graph, localSearchType, skipConflictlessNodes, maxLocalOptima, maxCPUTime);
        this.populationSize = populationSize;
    }

    /**
     * Partition the graph
     *
     * @return
     */
    public Solution partition() {
        this.population = new Population(populationSize);

        // generate initial population
        while (this.population.population.size() < populationSize) {
            Solution solution = new Solution(generateRandomBitArray(graph.nodes.length));
            solution.fitness = evaluateSolution(solution);

            solution = hillClimb(solution);


            saveNewOptimum(solution);

            if (this.population.contains(solution)) {
                continue;
            }

            this.population.addSolution(solution);
        }

        Random rand = new Random();
        // continue till stopping criteria is met
        while (!shouldStop()) {
            // generate 2 distinct indices
            int index1 = rand.nextInt(this.population.population.size());
            int index2 = rand.nextInt(this.population.population.size() - 1);
            if (index2 >= index1) index2++;

            Solution child = uniformCrossover(this.population.population.get(index1), this.population.population.get(index2));
            child.fitness = evaluateSolution(child);

            child = hillClimb(child);

            saveNewOptimum(child);

            // if child already in population
            if (this.population.contains(child)) {
                continue;
            }

            sortPopulation();
            if (child.fitness > this.population.population.get(populationSize - 1).fitness) {
                continue;
            }

            // if child is unique and improvement of population, add it
            this.population.replaceSolution(child, populationSize - 1);

            this.population.iterations++;

            // not necessary, since a solutions has to be unique to enter the population
            /*
            if (hasConverged(population)) {
                nIterationsConverged = population.iterations;
                break;
            }*/
        }

        return this.bestSolution;
    }

    /**
     * Uniform crossover on two parents
     *
     * @param parent1
     * @param parent1
     * @return childBitArray
     */
    public Solution uniformCrossover(Solution parent1, Solution parent2) {
        List<Integer> childBitArray = new ArrayList<>(parent1.bitArray.size());

        // keep track of the bits where parents disagree
        List<Integer> disagreedBitIndexes = new ArrayList<>(parent1.bitArray.size());


        // invert one parent if hamming distance it larger than l/2
        boolean invertOneParent = false;
        if (getHammingDistance(parent1.bitArray, parent2.bitArray) > (parent1.bitArray.size() / 2)) {
            invertOneParent = true;
        }

        for (int i = 0; i < parent1.bitArray.size(); i++) {
            int parentBit1 = parent1.bitArray.get(i);
            int parentBit2 = parent2.bitArray.get(i);

            // invert one parent if hamming distance it larger than l/2
            if (invertOneParent) parentBit1 = 1 - parentBit1;

            if (parentBit1 == parentBit2) {
                childBitArray.add(i, parentBit1);
            } else {
                // set stub bit, will be overwritten
                childBitArray.add(i, -1);
                disagreedBitIndexes.add(i);
            }
        }


        // sample from the evenly distributed random bit array for the disagreed bits
        List<Integer> randomBitArray = generateRandomBitArray(disagreedBitIndexes.size());

        for (int i = 0; i < disagreedBitIndexes.size(); i++) {
            childBitArray.set(disagreedBitIndexes.get(i), randomBitArray.get(i));
        }


        return new Solution(childBitArray);
    }

    /**
     * Calculate hamming distance
     *
     * @param bitArray1
     * @param bitArray2
     * @return hamming distance
     */
    private int getHammingDistance(List<Integer> bitArray1, List<Integer> bitArray2) {
        int distance = 0;
        for (int i = 0; i < bitArray1.size(); i++) {
            if (bitArray1.get(i).intValue() != bitArray2.get(i).intValue()) {
                distance++;
            }
        }

        return distance;
    }

    private void sortPopulation() {
        Collections.sort(this.population.population, (Solution a1, Solution a2) -> a1.fitness - a2.fitness);
    }
}
