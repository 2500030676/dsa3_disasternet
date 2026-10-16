package edu.disasternet.cli;

import edu.disasternet.algorithms.dp.DynamicProgramming;
import edu.disasternet.algorithms.stringalg.StringAlgorithms;
import edu.disasternet.models.Incident;
import edu.disasternet.data.DataStore;
import edu.disasternet.utils.ConsoleColors;
import edu.disasternet.utils.ConsoleInput;
import edu.disasternet.algorithms.randomparallel.RandomParallelAlgorithms;

import java.util.*;

public class IncidentMenu {
    private final List<Incident> incidents = DataStore.INCIDENTS;

    public IncidentMenu() {
    }

    public void show(Scanner scanner) {
        while (true) {
            System.out.println();
            System.out.println(ConsoleColors.color("============================================================", ConsoleColors.BLUE));
            System.out.println(ConsoleColors.color("                 INCIDENT REPORTS", ConsoleColors.CYAN));
            System.out.println(ConsoleColors.color("============================================================", ConsoleColors.BLUE));
            System.out.println("1. Add Incident");
            System.out.println("2. Search Incident");
            System.out.println("3. Multi-Keyword Search");
            System.out.println("4. Detect Duplicate Reports");
            System.out.println("5. Correct Noisy Input");
            System.out.println("6. View All Incidents");
            System.out.println("7. Sort Incidents");
            System.out.println("8. Mark Incident Resolved");
            System.out.println("9. Back");
            System.out.println(ConsoleColors.color("------------------------------------------------------------", ConsoleColors.BLUE));
            System.out.print("Enter your choice: ");
            if (!scanner.hasNextLine()) return;
            String opt = scanner.nextLine().trim();
            switch (opt) {
                case "1" -> addIncident(scanner);
                case "2" -> searchIncident(scanner);
                case "3" -> multiKeywordSearch(scanner);
                case "4" -> detectDuplicate(scanner);
                case "5" -> correctNoisyInput(scanner);
                case "6" -> viewAll();
                case "7" -> sortIncidents();
                case "8" -> resolveIncident(scanner);
                case "9" -> { return; }
                default -> System.out.println(ConsoleColors.color("[WARNING] Invalid menu option.", ConsoleColors.YELLOW));
            }
        }
    }

    private void addIncident(Scanner scanner) {
        System.out.print("Incident ID: ");
        String id = scanner.nextLine().trim();
        if (id.isBlank() || incidents.stream().anyMatch(i -> i.getId().equalsIgnoreCase(id))) {
            System.out.println(ConsoleColors.color("[WARNING] ID must be non-empty and unique.", ConsoleColors.YELLOW));
            return;
        }
        System.out.print("Location: ");
        String location = scanner.nextLine().trim();
        System.out.print("Disaster type: ");
        String type = scanner.nextLine().trim();
        System.out.print("Description: ");
        String description = scanner.nextLine().trim();
        if (location.isBlank() || type.isBlank() || description.isBlank()) { System.out.println(ConsoleColors.color("[WARNING] Location, type and description are required.", ConsoleColors.YELLOW)); return; }
        System.out.print("Severity (LOW/HIGH/CRITICAL): ");
        String severity = scanner.nextLine().trim().toUpperCase(Locale.ROOT);
        if (!Set.of("LOW", "MEDIUM", "HIGH", "CRITICAL").contains(severity)) {
            System.out.println(ConsoleColors.color("[WARNING] Invalid severity. Use LOW, MEDIUM, HIGH or CRITICAL.", ConsoleColors.YELLOW));
            return;
        }
        int affected = ConsoleInput.readInt(scanner, "Affected people: ", 0);
        incidents.add(new Incident(id, location, type, description, severity, affected, "ACTIVE"));
        System.out.println(ConsoleColors.color("[GREEN] Incident added successfully.", ConsoleColors.GREEN));
    }

    private void searchIncident(Scanner scanner) {
        System.out.print("Enter keyword: ");
        String keyword = scanner.nextLine().trim();
        if (keyword.isEmpty()) { System.out.println(ConsoleColors.color("[WARNING] Search term cannot be empty.", ConsoleColors.YELLOW)); return; }
        System.out.println(ConsoleColors.color("\n=== KMP SEARCH ===", ConsoleColors.CYAN));
        System.out.println("Keyword: " + keyword);
        int matches = 0;
        long comparisons = 0;
        for (Incident incident : incidents) {
            var trace = StringAlgorithms.kmp(incident.getDescription().toLowerCase(Locale.ROOT), keyword.toLowerCase(Locale.ROOT));
            comparisons += trace.comparisons();
            if (!trace.matches().isEmpty()) {
                System.out.printf("%-12s %-16s %-20s%n", incident.getId(), incident.getLocation(), incident.getDisasterType());
                matches++;
            }
        }
        System.out.println("Matching incidents: " + matches);
        System.out.println("Character comparisons: " + comparisons);
        System.out.println("Time Complexity: O(n + m)");
        System.out.println("Space Complexity: O(m)");
    }

    private void multiKeywordSearch(Scanner scanner) {
        System.out.print("Enter emergency keywords separated by spaces: ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) { System.out.println(ConsoleColors.color("[WARNING] Enter at least one keyword.", ConsoleColors.YELLOW)); return; }
        List<String> words = Arrays.asList(input.split("\\s+"));
        StringAlgorithms.AhoCorasick automaton = new StringAlgorithms.AhoCorasick(words);
        System.out.println(ConsoleColors.color("\n=== AHO-CORASICK RESULTS ===", ConsoleColors.CYAN));
        int matchedReports = 0;
        for (Incident incident : incidents) {
            Map<String, List<Integer>> matches = automaton.search(incident.getDescription());
            if (!matches.isEmpty()) {
                System.out.println("\n" + incident.getId() + " | " + incident.getLocation());
                matches.forEach((word, positions) -> System.out.println("  " + word + " at positions " + positions));
                matchedReports++;
            }
        }
        System.out.println("Matched reports: " + matchedReports + " | automaton states: " + automaton.states());
        System.out.println("Time: O(total text + matches + dictionary); Space: O(total keyword length)");
    }

    private void detectDuplicate(Scanner scanner) {
        if (incidents.size() < 2) {
            System.out.println(ConsoleColors.color("[WARNING] Need at least two incidents to compare.", ConsoleColors.YELLOW));
            return;
        }
        int first = ConsoleInput.readInt(scanner, "Choose first incident index (0-based): ", 0);
        int second = ConsoleInput.readInt(scanner, "Choose second incident index (0-based): ", 0);
        if (first < 0 || second < 0 || first >= incidents.size() || second >= incidents.size()) {
            System.out.println(ConsoleColors.color("[WARNING] Invalid incident index.", ConsoleColors.YELLOW));
            return;
        }
        String a = incidents.get(first).getDescription();
        String b = incidents.get(second).getDescription();
        var trace = StringAlgorithms.zSearch(a.toLowerCase(Locale.ROOT), b.toLowerCase(Locale.ROOT));
        System.out.println(ConsoleColors.color("\n=== DUPLICATE DETECTION ===", ConsoleColors.CYAN));
        System.out.println("Report A : " + incidents.get(first).getId());
        System.out.println("Report B : " + incidents.get(second).getId());
        int distance = DynamicProgramming.levenshtein(a.toLowerCase(Locale.ROOT), b.toLowerCase(Locale.ROOT));
        double similarity = 1.0 - (double) distance / Math.max(1, Math.max(a.length(), b.length()));
        System.out.printf("Shared exact fragments (Z-function): %d | edit distance: %d | similarity: %.0f%%%n", trace.matches().size(), distance, similarity * 100);
        System.out.println("Status: " + (similarity >= 0.75 ? "POSSIBLE DUPLICATE" : "DIFFERENT REPORTS"));
        System.out.println("Z-function: O(n+m) time/space; Levenshtein: O(nm) time/space.");
    }

    private void correctNoisyInput(Scanner scanner) {
        System.out.print("Enter noisy input: ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) { System.out.println(ConsoleColors.color("[WARNING] Input cannot be empty.", ConsoleColors.YELLOW)); return; }
        Set<String> vocabulary = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Incident incident : incidents) {
            for (String token : (incident.getDescription() + " " + incident.getDisasterType() + " " + incident.getLocation()).split("[^A-Za-z]+"))
                if (token.length() >= 3) vocabulary.add(token.toLowerCase(Locale.ROOT));
        }
        for (var resource : DataStore.RESOURCES) for (String token : resource.getName().split("[^A-Za-z]+")) if (token.length() >= 3) vocabulary.add(token.toLowerCase(Locale.ROOT));
        List<String> suggestions = new ArrayList<>(vocabulary);
        suggestions.sort(Comparator.comparingInt((String word) -> DynamicProgramming.damerauLevenshtein(input.toLowerCase(Locale.ROOT), word)).thenComparing(String::compareTo));
        System.out.println(ConsoleColors.color("\n=== INPUT CORRECTION ===", ConsoleColors.CYAN));
        System.out.println("Input: " + input);
        System.out.println("Closest terms from current incident and supply vocabulary:");
        int shown = 0;
        for (String suggestion : suggestions) {
            int distance = DynamicProgramming.damerauLevenshtein(input.toLowerCase(Locale.ROOT), suggestion);
            if (distance > Math.max(2, input.length() / 2) || shown == 5) break;
            int weighted = DynamicProgramming.weighted(input.toLowerCase(Locale.ROOT), suggestion, Map.of('e', 2, 'a', 2, 'i', 2));
            System.out.println("- " + suggestion + " (Damerau distance=" + distance + ", weighted edit cost=" + weighted + ")");
            shown++;
        }
        if (shown == 0) System.out.println("No close candidates found.");
        System.out.println("Dynamic-programming edit distances take O(mn) time and space.");
    }

    private void viewAll() {
        System.out.println(ConsoleColors.color("\n=== INCIDENT LIST ===", ConsoleColors.CYAN));
        System.out.printf("%-12s %-15s %-18s %-12s %-10s%n", "ID", "LOCATION", "TYPE", "SEVERITY", "PEOPLE");
        for (Incident incident : incidents) {
            System.out.printf("%-12s %-15s %-18s %-12s %-10d%n", incident.getId(), incident.getLocation(), incident.getDisasterType(), incident.getSeverity(), incident.getAffectedPeople());
        }
    }

    private void sortIncidents() {
        List<Integer> impacts = incidents.stream().map(Incident::getAffectedPeople).toList();
        var result = RandomParallelAlgorithms.randomizedQuickSort(impacts, System.nanoTime());
        Map<Integer, Deque<Incident>> byImpact = new HashMap<>();
        for (Incident incident : incidents) byImpact.computeIfAbsent(incident.getAffectedPeople(), key -> new ArrayDeque<>()).add(incident);
        incidents.clear();
        for (int impact : result.sorted()) incidents.add(byImpact.get(impact).removeFirst());
        viewAll();
        System.out.println("Randomized QuickSort comparisons: " + result.comparisons() + " | pivots: " + result.pivots());
        System.out.println("Expected O(n log n), worst O(n^2) time; expected O(log n) recursion space.");
    }

    private void resolveIncident(Scanner scanner) {
        System.out.print("Incident ID to resolve: ");
        String id = scanner.nextLine().trim();
        for (Incident incident : incidents) if (incident.getId().equalsIgnoreCase(id)) {
            incident.setStatus("RESOLVED");
            System.out.println(ConsoleColors.color("[GREEN] " + id + " marked RESOLVED.", ConsoleColors.GREEN));
            return;
        }
        System.out.println(ConsoleColors.color("[WARNING] Incident ID not found.", ConsoleColors.YELLOW));
    }
}
