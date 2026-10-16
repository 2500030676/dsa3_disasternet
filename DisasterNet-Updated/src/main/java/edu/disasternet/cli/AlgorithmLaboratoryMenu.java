package edu.disasternet.cli;

import edu.disasternet.simulations.DisasterSimulation;
import edu.disasternet.algorithms.dp.DynamicProgramming;
import edu.disasternet.algorithms.np.NpAlgorithms;
import edu.disasternet.algorithms.randomparallel.RandomParallelAlgorithms;
import edu.disasternet.data.DataStore;
import edu.disasternet.utils.ConsoleColors;
import java.util.*;
import java.util.Scanner;

/** Runs a connected scenario that exercises algorithms against emergency reports and response data. */
public final class AlgorithmLaboratoryMenu {
    public void show(Scanner scanner) {
        while (true) {
            System.out.println();
            System.out.println(ConsoleColors.color("==================== ALGORITHM LABORATORY ====================", ConsoleColors.BLUE));
            System.out.println("1. Run integrated emergency-response scenario");
            System.out.println("2. Dynamic programming lab");
            System.out.println("3. NP constraints and approximation lab");
            System.out.println("4. Randomized and parallel analytics lab");
            System.out.println("5. Back");
            System.out.print("Enter your choice: ");
            if (!scanner.hasNextLine()) return;
            switch (scanner.nextLine().trim()) {
                case "1" -> runScenario();
                case "2" -> runDynamicProgramming();
                case "3" -> runNpLab();
                case "4" -> runRandomParallelLab();
                case "5" -> { return; }
                default -> System.out.println(ConsoleColors.color("[WARNING] Invalid input. Please try again.", ConsoleColors.YELLOW));
            }
        }
    }

    private void runScenario() {
        try {
            System.out.println(ConsoleColors.color("\n=== LIVE DSA SCENARIO ===", ConsoleColors.CYAN));
            System.out.println("The run uses the current report stream and connects retrieval, indexing, planning, flow, matching and analytics.");
            System.out.println(DisasterSimulation.run());
            System.out.println("\nAlgorithms: Aho-Corasick, KMP, suffix array/Kasai, suffix automaton,");
            System.out.println("Damerau-Levenshtein, bitmask resource DP, Dinic, bipartite matching,");
            System.out.println("reservoir sampling and Blelloch scan.");
        } catch (Exception ex) {
            System.out.println(ConsoleColors.color("[WARNING] Scenario could not read its sample data: " + ex.getMessage(), ConsoleColors.YELLOW));
        }
    }

    private void runDynamicProgramming() {
        String noisy = "medcial";
        int edit = DynamicProgramming.damerauLevenshtein(noisy, "medical");
        List<DynamicProgramming.Item> items = DataStore.RESOURCES.stream().filter(r -> r.getQuantity() > 0)
            .map(r -> new DynamicProgramming.Item(r.getName(), r.getWeight(), r.getValue())).toList();
        var selection = DynamicProgramming.resourceSelection(items, 35);
        var matrices = DynamicProgramming.matrixChain(new int[]{40, 20, 30, 10, 30});
        System.out.println(ConsoleColors.color("\n=== DYNAMIC PROGRAMMING: RESPONSE PLANNING ===", ConsoleColors.CYAN));
        System.out.println("Noisy field term: " + noisy + " | distance to medical: " + edit + " (Damerau-Levenshtein, O(mn) time/space)");
        System.out.println("Available inventory candidates: " + items.size() + " | budget/weight limit: 35");
        System.out.println("Selected supplies: " + selection.items() + " | planning weight: " + selection.cost() + " | coverage value: " + selection.value());
        System.out.println("Matrix-chain plan: " + matrices.order() + " | scalar operations: " + matrices.cost());
        System.out.println("0/1 selection uses O(nB) time and O(nB) space; matrix chain uses O(n^3) time and O(n^2) space.");
    }

    private void runNpLab() {
        var clauses = List.of(
            List.of(new NpAlgorithms.Literal("boats", true), new NpAlgorithms.Literal("roads_open", true)),
            List.of(new NpAlgorithms.Literal("boats", false), new NpAlgorithms.Literal("medical_kit", true)),
            List.of(new NpAlgorithms.Literal("medical_kit", false), new NpAlgorithms.Literal("roads_open", true))
        );
        var sat = NpAlgorithms.sat(clauses);
        var clique = NpAlgorithms.threeSatToClique(clauses);
        var cover = NpAlgorithms.vertexCover2Approx(clique);
        System.out.println(ConsoleColors.color("\n=== NP-CONSTRAINTS: RESPONSE PLAN ===", ConsoleColors.CYAN));
        System.out.println("Constraints: activate boats or ensure roads open; boats require medical kits; without kits roads must be open.");
        System.out.println("SAT feasible: " + sat.satisfiable() + " | assignment: " + sat.assignment());
        System.out.println("3-SAT reduction graph vertices: " + clique.length + " | maximum clique size: " + NpAlgorithms.maximumClique(clique).size());
        System.out.println("Maximum independent set size: " + NpAlgorithms.independentSet(clique).size());
        System.out.println("Approximate conflict-cover vertices: " + cover + " | exact cover size: " + NpAlgorithms.vertexCover(clique).size() + " (2-approximation; O(V^2) adjacency-matrix implementation)");
        System.out.println("SAT and maximum clique use exhaustive search (exponential time); the cover routine is an approximation.");
    }

    private void runRandomParallelLab() {
        List<Integer> input = DataStore.INCIDENTS.stream().map(i -> i.getAffectedPeople()).toList();
        var sort = RandomParallelAlgorithms.randomizedQuickSort(input, 2026L);
        List<edu.disasternet.models.Observation> sample = RandomParallelAlgorithms.reservoirSample(DataStore.OBSERVATIONS, 5, 2026L);
        int[] people = DataStore.OBSERVATIONS.stream().mapToInt(edu.disasternet.models.Observation::getAffectedPeople).toArray();
        var scan = RandomParallelAlgorithms.blellochScan(people);
        var tree = RandomParallelAlgorithms.reductionTree(people);
        int actualParallelTotal = RandomParallelAlgorithms.parallelReduce(people);
        int processors = Math.max(1, Runtime.getRuntime().availableProcessors());
        double estimate = RandomParallelAlgorithms.brentEstimate(people.length, (long) Math.ceil(Math.log(Math.max(1, people.length)) / Math.log(2)), processors);
        System.out.println(ConsoleColors.color("\n=== RANDOMIZED & PARALLEL: LIVE INCIDENT DATA ===", ConsoleColors.CYAN));
        System.out.println("Affected people sorted: " + sort.sorted() + " | pivots: " + sort.pivots());
        System.out.println("Reservoir sample (5 of " + DataStore.OBSERVATIONS.size() + "): " + sample.stream().map(edu.disasternet.models.Observation::getObservationId).toList());
        System.out.println("Blelloch cumulative affected counts: " + java.util.Arrays.toString(scan.inclusive()));
        System.out.println("Parallel stream reduction total: " + actualParallelTotal + " | reduction-tree trace total: " + tree.get(tree.size() - 1).get(0));
        System.out.println("Brent work/span estimate on " + processors + " processors: " + estimate);
        long batchSize = DataStore.OBSERVATIONS.size();
        System.out.println("Miller-Rabin: current observation batch size " + batchSize + " is prime = " + RandomParallelAlgorithms.millerRabin(batchSize)
            + " (deterministic bases for signed 64-bit values; O(log^2 n) with the overflow-safe modular multiply here).");
        System.out.println("Quicksort expected O(n log n); reservoir sampling O(n) time/O(k) space; scan/reduce O(n) work and O(log n) span.");
    }
}
