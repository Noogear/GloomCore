package gloomcore.common.random;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class AliasMethodRandom{
    private final int[] alias;
    private final double[] probability;

    public AliasMethodRandom(List<Double> originalWeights) {
        if (originalWeights == null || originalWeights.isEmpty()) {
            throw new IllegalArgumentException("Weights list cannot be null or empty.");
        }

        final int size = originalWeights.size();
        this.alias = new int[size];
        this.probability = new double[size];
        final double sum = originalWeights.stream().mapToDouble(Double::doubleValue).sum();
        if (sum <= 0) {
            throw new IllegalArgumentException("Sum of weights must be positive.");
        }

        if (size == 1) {
            this.probability[0] = 1.0;
            return;
        }

        final double[] normalizedProbabilities = new double[size];
        for (int i = 0; i < size; i++) {
            double weight = originalWeights.get(i);
            if (weight < 0) {
                throw new IllegalArgumentException("Weights must be non-negative.");
            }
            normalizedProbabilities[i] = weight / sum;
        }

        final double average = 1.0 / size;
        int[] small = new int[size];
        int smallSize = 0;
        int[] large = new int[size];
        int largeSize = 0;

        for (int i = 0; i < size; i++) {
            if (normalizedProbabilities[i] < average - 1e-15) {
                small[smallSize++] = i;
            } else {
                large[largeSize++] = i;
            }
        }

        while (smallSize > 0 && largeSize > 0) {
            int less = small[--smallSize];
            int more = large[--largeSize];

            this.probability[less] = normalizedProbabilities[less] * size;
            this.alias[less] = more;

            normalizedProbabilities[more] += normalizedProbabilities[less] - average;

            if (normalizedProbabilities[more] < average - 1e-15) {
                small[smallSize++] = more;
            } else {
                large[largeSize++] = more;
            }
        }

        while (largeSize > 0) {
            this.probability[large[--largeSize]] = 1.0;
        }
        while (smallSize > 0) {
            this.probability[small[--smallSize]] = 1.0;
        }
    }

    public int next() {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final int column = random.nextInt(probability.length);

        return random.nextDouble() < probability[column] ? column : alias[column];
    }

}
