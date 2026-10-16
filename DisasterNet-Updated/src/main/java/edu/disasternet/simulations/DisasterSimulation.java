package edu.disasternet.simulations;

import edu.disasternet.algorithms.stringalg.StringAlgorithms;
import edu.disasternet.algorithms.suffix.SuffixStructures;
import edu.disasternet.algorithms.dp.DynamicProgramming;
import edu.disasternet.algorithms.flow.FlowAlgorithms;
import edu.disasternet.algorithms.np.NpAlgorithms;
import edu.disasternet.algorithms.randomparallel.RandomParallelAlgorithms;
import edu.disasternet.data.DataStore;
import java.nio.file.*;
import java.util.*;

/** One coherent scenario that calls every course module. */
public final class DisasterSimulation {

    public record IncidentDetails(String disasterType, String location, String severity,
                                  int peopleAffected, String description) {
        public IncidentDetails {
            if (disasterType == null || disasterType.isBlank()) throw new IllegalArgumentException("Choose a disaster type.");
            if (location == null || location.isBlank()) throw new IllegalArgumentException("Enter the affected location.");
            if (severity == null || severity.isBlank()) throw new IllegalArgumentException("Choose a severity.");
            if (peopleAffected < 1) throw new IllegalArgumentException("People affected must be at least 1.");
            description = description == null ? "" : description.trim();
        }

        String asReport() {
            return String.format("%s emergency at %s. Severity %s. Approximately %d people affected. %s",
                disasterType, location, severity, peopleAffected, description);
        }
    }

    private DisasterSimulation() {}

    public static String run() throws Exception {
        return run(new IncidentDetails("Flood", "Coastal Ward 7, Chennai", "High", 1200,
            "Rising water has blocked the eastern access road; two families request evacuation."));
    }

    public static String run(IncidentDetails incident) throws Exception {
        long start = System.nanoTime();

        List<String> lines = new ArrayList<>(Files.readAllLines(Path.of("data/reports.txt")));
        DataStore.INCIDENTS.forEach(record -> lines.add(record.getId() + " | " + record.getLocation() + " | " + record.getDescription()));
        DataStore.COMMUNICATIONS.forEach(record -> lines.add(record.getSender() + ": " + record.getMessage()));
        DataStore.OBSERVATIONS.forEach(record -> lines.add(record.getLocation() + ": " + record.getDescription()));
        lines.add(incident.asReport());
        String reports = String.join("\n", lines);

        var ac = new StringAlgorithms.AhoCorasick(List.of(
            "FLOOD", "FIRE", "TRAPPED", "INJURED", "OXYGEN",
            "EVACUATION", "COLLAPSE", "MISSING", "MEDICAL", "ROAD BLOCKED",
            "CYCLONE", "EARTHQUAKE", "LANDSLIDE"
        ));
        var alerts = ac.search(reports);
        var kmp = StringAlgorithms.kmp(reports.toLowerCase(), incident.location().toLowerCase());

        int[] sa = SuffixStructures.sais(reports);
        int[] lcp = SuffixStructures.kasai(reports, sa);
        int max = 0;
        for (int x : lcp) max = Math.max(max, x);
        var sam = new SuffixStructures.SuffixAutomaton(reports.toLowerCase());

        int resourceBudget = switch (incident.severity().toLowerCase(Locale.ROOT)) {
            case "low" -> 25000;
            case "moderate" -> 35000;
            case "critical" -> 65000;
            default -> 50000;
        };
        int selectionBudget = switch (incident.severity().toLowerCase(Locale.ROOT)) {
            case "low" -> 15; case "moderate" -> 22; case "critical" -> 35; default -> 28;
        };
        List<DynamicProgramming.Item> stock = DataStore.RESOURCES.stream().filter(r -> r.getQuantity() > 0)
            .map(r -> new DynamicProgramming.Item(r.getName(), r.getWeight(), r.getValue())).toList();
        var pack = DynamicProgramming.resourceSelection(stock, selectionBudget);

        int totalStock = DataStore.RESOURCES.stream().mapToInt(r -> r.getQuantity()).sum();
        int totalShelterCapacity = DataStore.SHELTERS.stream().mapToInt(s -> s.getAvailable()).sum();
        int warehouseA = Math.max(1, totalStock / 2), warehouseB = Math.max(1, totalStock - warehouseA);
        int roadCapacity = Math.max(1, totalShelterCapacity / 2);
        List<FlowAlgorithms.Edge> edges = List.of(
            new FlowAlgorithms.Edge(0, 1, warehouseA), new FlowAlgorithms.Edge(0, 2, warehouseB),
            new FlowAlgorithms.Edge(1, 3, Math.max(1, warehouseA / 2)), new FlowAlgorithms.Edge(1, 4, Math.max(1, warehouseA / 2)),
            new FlowAlgorithms.Edge(2, 4, Math.max(1, warehouseB / 3)), new FlowAlgorithms.Edge(2, 5, Math.max(1, warehouseB * 2 / 3)),
            new FlowAlgorithms.Edge(3, 6, Math.max(1, roadCapacity / 4)), new FlowAlgorithms.Edge(4, 6, Math.max(1, roadCapacity / 2)),
            new FlowAlgorithms.Edge(5, 6, Math.max(1, roadCapacity / 4))
        );
        var flow = FlowAlgorithms.dinic(7, edges, 0, 6);

        List<String> tasks = List.of("Flood Rescue", "Medical Support", "Fire Rescue", "Search and Rescue", "Evacuation", "Logistics", "Urban Rescue", "Water Rescue", "Cyclone Response", "First Aid");
        Map<String, List<String>> compat = new LinkedHashMap<>();
        for (var team : DataStore.TEAMS) if (team.isAvailable()) compat.put(team.getName(), tasks.stream().filter(task -> team.getSpecialization().equalsIgnoreCase(task)
            || (task.equals("Evacuation") && team.getSpecialization().toLowerCase(Locale.ROOT).contains("rescue"))).toList());
        var match = FlowAlgorithms.bipartiteMatching(compat);

        int[] pop = DataStore.OBSERVATIONS.stream().mapToInt(o -> o.getAffectedPeople()).toArray();
        int[] incidentPopulation = splitPopulation(incident.peopleAffected());
        var sample = RandomParallelAlgorithms.reservoirSample(DataStore.OBSERVATIONS, Math.min(5, DataStore.OBSERVATIONS.size()), 42);
        var scan = RandomParallelAlgorithms.blellochScan(pop);
        var npFeasibility = NpAlgorithms.sat(List.of(
            List.of(new NpAlgorithms.Literal("boat_dispatch", true), new NpAlgorithms.Literal("roads_clear", true)),
            List.of(new NpAlgorithms.Literal("boat_dispatch", false), new NpAlgorithms.Literal("medical_kit", true))));

        long ms = (System.nanoTime() - start) / 1_000_000;

        return "EMERGENCY RESPONSE SUMMARY\n"
            + "Disaster: " + incident.disasterType()
            + "\nLocation: " + incident.location()
            + "\nSeverity: " + incident.severity()
            + "\nIncident details: " + (incident.description().isBlank() ? "No additional details provided." : incident.description())
            + "\nLive report/communication/observation records processed: " + lines.size()
            + "\nCritical keyword types: " + alerts.size()
            + " (automaton states: " + ac.states() + ")"
            + "\nKMP location matches for '" + incident.location() + "': " + kmp.matches().size()
            + "\nSuffix index length: " + sa.length
            + ", longest repeated substring length: " + max
            + ", 'water entered' indexed: " + sam.contains("water entered")
            + "\nNoisy report 'medcial' -> 'medical' Damerau distance: "
            + DynamicProgramming.damerauLevenshtein("medcial", "medical")
            + "\nResource selection: " + stock.size() + " in-stock types; severity budget INR " + resourceBudget + " (DP weight cap " + selectionBudget + ")"
            + "\nSelected supply package: " + pack.items() + " (planning weight " + pack.cost() + ")"
            + ", coverage score: " + pack.value()
            + "\nMaximum supply flow: " + flow.maxFlow()
            + "\nCritical min-cut source side: " + flow.sourceSide()
            + "\nTeams assigned: " + match.size() + "/" + compat.size() + " " + match
            + "\nSAT response constraints feasible: " + npFeasibility.satisfiable() + " " + npFeasibility.assignment()
            + "\nPeople affected (parallel reduction): "
            + RandomParallelAlgorithms.reductionTree(incidentPopulation).get(RandomParallelAlgorithms.reductionTree(incidentPopulation).size() - 1).get(0)
            + "\nCumulative observation impact (parallel reduction): "
            + RandomParallelAlgorithms.reductionTree(pop).get(RandomParallelAlgorithms.reductionTree(pop).size() - 1).get(0)
            + "\nCumulative statistics: " + Arrays.toString(scan.inclusive())
            + "\nField observations sampled: " + sample.size()
            + "\nProcessing time: " + ms + " ms";
    }

    private static int[] splitPopulation(int total) {
        int base = total / 4;
        int remainder = total % 4;
        int[] regions = new int[4];
        for (int i = 0; i < regions.length; i++) regions[i] = base + (i < remainder ? 1 : 0);
        return regions;
    }
}
