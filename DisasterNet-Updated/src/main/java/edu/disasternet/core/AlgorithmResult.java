package edu.disasternet.core;

import java.util.*;

/**
 * Uniform evidence bundle: makes Input -> Algorithm -> Output -> Complexity explicit.
 */
public record AlgorithmResult(
    String name,
    String problem,
    Object input,
    Object output,
    List<String> steps,
    String timeComplexity,
    String spaceComplexity,
    long elapsedNanos
) {
    public AlgorithmResult {
        steps = List.copyOf(steps);
    }

    public String display() {
        return "\n== " + name + " ==\n"
            + "Problem: " + problem
            + "\nInput: " + input
            + "\nOutput: " + output
            + "\nSteps:\n - " + String.join("\n - ", steps)
            + "\nComplexity: time " + timeComplexity
            + ", space " + spaceComplexity
            + "\nMeasured: " + String.format("%.3f ms", elapsedNanos / 1_000_000.0);
    }
}
