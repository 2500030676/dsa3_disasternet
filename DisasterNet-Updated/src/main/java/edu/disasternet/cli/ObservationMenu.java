package edu.disasternet.cli;

import edu.disasternet.algorithms.randomparallel.RandomParallelAlgorithms;
import edu.disasternet.algorithms.stringalg.StringAlgorithms;
import edu.disasternet.models.Observation;
import edu.disasternet.data.DataStore;
import edu.disasternet.utils.ConsoleColors;
import edu.disasternet.utils.ConsoleInput;

import java.util.*;

public class ObservationMenu {
    private final List<Observation> observations = DataStore.OBSERVATIONS;

    public ObservationMenu() {
    }

    public void show(Scanner scanner) {
        while (true) {
            System.out.println();
            System.out.println(ConsoleColors.color("============================================================", ConsoleColors.BLUE));
            System.out.println(ConsoleColors.color("                 FIELD OBSERVATIONS & ANALYTICS", ConsoleColors.CYAN));
            System.out.println(ConsoleColors.color("============================================================", ConsoleColors.BLUE));
            System.out.println("1. Add Observation");
            System.out.println("2. View Observations");
            System.out.println("3. Search Observations");
            System.out.println("4. Reservoir Sample");
            System.out.println("5. Cumulative Statistics");
            System.out.println("6. Parallel Analytics");
            System.out.println("7. Rank by affected people (Randomized QuickSort)");
            System.out.println("8. Back");
            System.out.print("Enter your choice: ");
            if (!scanner.hasNextLine()) return;
            String option = scanner.nextLine().trim();
            switch (option) {
                case "1" -> addObservation(scanner);
                case "2" -> viewObservations();
                case "3" -> searchObservations(scanner);
                case "4" -> reservoirSample();
                case "5" -> cumulativeStatistics();
                case "6" -> parallelAnalytics();
                case "7" -> rankByImpact();
                case "8" -> { return; }
                default -> System.out.println(ConsoleColors.color("[WARNING] Invalid menu option.", ConsoleColors.YELLOW));
            }
        }
    }

    private void addObservation(Scanner scanner) {
        System.out.print("Observation ID: ");
        String id = scanner.nextLine().trim();
        if (id.isBlank() || observations.stream().anyMatch(o -> o.getObservationId().equalsIgnoreCase(id))) { System.out.println(ConsoleColors.color("[WARNING] ID must be non-empty and unique.", ConsoleColors.YELLOW)); return; }
        System.out.print("Location: ");
        String location = scanner.nextLine();
        System.out.print("Type: ");
        String type = scanner.nextLine();
        System.out.print("Description: ");
        String desc = scanner.nextLine();
        if (location.isBlank() || type.isBlank() || desc.isBlank()) { System.out.println(ConsoleColors.color("[WARNING] Location, type and description are required.", ConsoleColors.YELLOW)); return; }
        int affected = ConsoleInput.readInt(scanner, "Affected people: ", 0);
        observations.add(new Observation(id, location, type, desc, affected));
        System.out.println(ConsoleColors.color("[GREEN] Observation recorded.", ConsoleColors.GREEN));
    }

    private void viewObservations() {
        System.out.println(ConsoleColors.color("\n=== FIELD OBSERVATIONS ===", ConsoleColors.CYAN));
        for (Observation obs : observations) {
            System.out.println(obs.getObservationId() + " | " + obs.getLocation() + " | " + obs.getType() + " | " + obs.getAffectedPeople() + " | " + obs.getDescription());
        }
    }

    private void searchObservations(Scanner scanner) {
        System.out.print("Search keyword: ");
        String keyword = scanner.nextLine();
        if (keyword.isBlank()) { System.out.println(ConsoleColors.color("[WARNING] Search term cannot be empty.", ConsoleColors.YELLOW)); return; }
        System.out.println(ConsoleColors.color("\n=== OBSERVATION SEARCH ===", ConsoleColors.CYAN));
        for (Observation obs : observations) {
            if (!StringAlgorithms.kmp(obs.getDescription().toLowerCase(Locale.ROOT), keyword.toLowerCase(Locale.ROOT)).matches().isEmpty()) {
                System.out.println(obs.getObservationId() + " | " + obs.getLocation() + " | " + obs.getDescription());
            }
        }
        System.out.println("Search algorithm: KMP; O(n+m) time, O(m) space.");
    }

    private void reservoirSample() {
        List<Observation> sample = RandomParallelAlgorithms.reservoirSample(observations, Math.min(5, observations.size()), System.nanoTime());
        System.out.println(ConsoleColors.color("\n=== RESERVOIR SAMPLE ===", ConsoleColors.CYAN));
        sample.forEach(obs -> System.out.println(obs.getObservationId() + " | " + obs.getLocation() + " | affected " + obs.getAffectedPeople() + " | " + obs.getDescription()));
        System.out.println("Uniform sample of " + sample.size() + " observations from a stream of " + observations.size() + "; O(n) time, O(k) space.");
    }

    private void cumulativeStatistics() {
        int[] values = observations.stream().mapToInt(Observation::getAffectedPeople).toArray();
        var result = RandomParallelAlgorithms.blellochScan(values);
        System.out.println(ConsoleColors.color("\n=== BLELLOCH SCAN ===", ConsoleColors.CYAN));
        System.out.println("Input affected counts: " + Arrays.toString(values));
        System.out.println("Inclusive prefix: " + Arrays.toString(result.inclusive()));
        System.out.println("Stages: " + result.stages().size() + " | O(n) work, O(log n) parallel span, O(n) space.");
    }

    private void parallelAnalytics() {
        int[] values = observations.stream().mapToInt(Observation::getAffectedPeople).toArray();
        var tree = RandomParallelAlgorithms.reductionTree(values);
        System.out.println(ConsoleColors.color("\n=== PARALLEL REDUCE ===", ConsoleColors.CYAN));
        System.out.println(tree);
        int parallelTotal = RandomParallelAlgorithms.parallelReduce(values);
        System.out.println("Parallel-stream total: " + parallelTotal + " | reduction tree total: " + tree.get(tree.size() - 1).get(0));
        System.out.println("Reduction tree has " + tree.size() + " levels; O(n) work and O(log n) parallel span.");
    }

    private void rankByImpact() {
        List<Integer> input = observations.stream().map(Observation::getAffectedPeople).toList();
        var result = RandomParallelAlgorithms.randomizedQuickSort(input, System.nanoTime());
        System.out.println(ConsoleColors.color("\n=== RANDOMIZED QUICKSORT: OBSERVATION IMPACT ===", ConsoleColors.CYAN));
        System.out.println("Input impact values: " + input);
        System.out.println("Sorted ascending: " + result.sorted());
        System.out.println("Comparisons: " + result.comparisons() + " | pivot trace: " + result.pivots());
        System.out.println("Expected O(n log n), worst O(n^2) time; expected O(log n) recursion space.");
    }
}
