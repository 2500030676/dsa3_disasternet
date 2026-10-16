package edu.disasternet;

import edu.disasternet.algorithms.stringalg.StringAlgorithms;
import edu.disasternet.algorithms.suffix.SuffixStructures;
import edu.disasternet.algorithms.dp.DynamicProgramming;
import edu.disasternet.algorithms.flow.FlowAlgorithms;
import edu.disasternet.algorithms.np.NpAlgorithms;
import edu.disasternet.algorithms.randomparallel.RandomParallelAlgorithms;
import java.util.*;

/**
 * Dependency-free correctness tests; run with:
 *   java -ea -cp target/test-classes;target/classes edu.disasternet.AlgorithmTests
 * on Windows.
 */
public class AlgorithmTests {

    static void ok(boolean x, String m) {
        if (!x) throw new AssertionError(m);
    }

    public static void main(String[] z) throws Exception {
        ok(StringAlgorithms.kmp("aaaa", "aa").matches().equals(List.of(0, 1, 2)), "KMP overlaps");
        ok(StringAlgorithms.zSearch("flood flood", "flood").matches().equals(List.of(0, 6)), "Z");
        ok(StringAlgorithms.rabinKarp("ABC123ABC", "ABC").matches().equals(List.of(0, 6)), "RK");

        var ac = new StringAlgorithms.AhoCorasick(List.of("FLOOD", "FIRE"));
        ok(ac.search("flood fire").size() == 2, "Aho");

        int[] sa = SuffixStructures.suffixArray("banana");
        ok(Arrays.equals(sa, new int[]{5, 3, 1, 0, 4, 2}), "suffix array");
        ok(Arrays.equals(SuffixStructures.sais("banana"), sa), "SA-IS");
        ok(new SuffixStructures.SuffixAutomaton("flood water").contains("water"), "SAM");

        ok(DynamicProgramming.levenshtein("kitten", "sitting") == 3, "lev");
        ok(DynamicProgramming.damerauLevenshtein("medcial", "medical") == 1, "DL");
        ok(DynamicProgramming.weighted("madical", "medical", Map.of('e', 5)) == 5, "weighted");

        ok(DynamicProgramming.resourceSelection(
            List.of(
                new DynamicProgramming.Item("Boat", 5, 8),
                new DynamicProgramming.Item("Kit", 4, 7)
            ), 5
        ).value() == 8, "knapsack");

        ok(DynamicProgramming.matrixChain(new int[]{10, 30, 5, 60}).cost() == 4500, "MCM");

        var e = List.of(
            new FlowAlgorithms.Edge(0, 1, 3),
            new FlowAlgorithms.Edge(0, 2, 2),
            new FlowAlgorithms.Edge(1, 2, 1),
            new FlowAlgorithms.Edge(1, 3, 2),
            new FlowAlgorithms.Edge(2, 3, 3)
        );
        ok(FlowAlgorithms.fordFulkerson(4, e, 0, 3).maxFlow() == 5, "FF");
        ok(FlowAlgorithms.edmondsKarp(4, e, 0, 3).maxFlow() == 5, "EK");
        ok(FlowAlgorithms.dinic(4, e, 0, 3).maxFlow() == 5, "Dinic");

        Map<String, List<String>> bm = Map.of("A", List.of("X"), "B", List.of("Y"));
        var matching = FlowAlgorithms.bipartiteMatching(bm);
        ok(matching.size() == 2 && FlowAlgorithms.konigCover(bm, matching).size() == 2, "matching/Konig");

        var sat = NpAlgorithms.sat(List.of(
            List.of(new NpAlgorithms.Literal("Boat", true)),
            List.of(new NpAlgorithms.Literal("Boat", false), new NpAlgorithms.Literal("Kit", true))
        ));
        ok(sat.satisfiable(), "SAT");

        boolean[][] g = {
            {false, true, true},
            {true, false, true},
            {true, true, false}
        };
        ok(NpAlgorithms.maximumClique(g).size() == 3, "clique");
        ok(NpAlgorithms.independentSet(g).size() == 1, "independent");
        ok(NpAlgorithms.vertexCover2Approx(g).size() <= 2 * NpAlgorithms.vertexCover(g).size(), "approx bound");

        ok(RandomParallelAlgorithms.randomizedQuickSort(List.of(3, 1, 2, 2), 4).sorted().equals(List.of(1, 2, 2, 3)), "sort");
        ok(RandomParallelAlgorithms.millerRabin(2_147_483_647L), "prime");
        ok(!RandomParallelAlgorithms.millerRabin(221), "composite");
        ok(Arrays.equals(
            RandomParallelAlgorithms.blellochScan(new int[]{10, 15, 20, 12, 30}).inclusive(),
            new int[]{10, 25, 45, 57, 87}
        ), "scan");
        ok(RandomParallelAlgorithms.reductionTree(new int[]{120, 230, 90, 160}).get(2).get(0) == 600, "reduce");

        var incident = new edu.disasternet.simulations.DisasterSimulation.IncidentDetails(
            "Earthquake", "North Campus", "Critical", 1001, "Two buildings are unsafe.");
        String response = edu.disasternet.simulations.DisasterSimulation.run(incident);
        ok(response.contains("Disaster: Earthquake"), "scenario disaster input");
        ok(response.contains("Location: North Campus"), "scenario location input");
        ok(response.contains("People affected (parallel reduction): 1001"), "scenario population input");
        ok(response.contains("Resource budget for Critical severity: INR 65000"), "severity controls resource budget");

        boolean rejectsBlankLocation = false;
        try {
            new edu.disasternet.simulations.DisasterSimulation.IncidentDetails("Flood", " ", "High", 10, "");
        } catch (IllegalArgumentException expected) {
            rejectsBlankLocation = true;
        }
        ok(rejectsBlankLocation, "incident validation");

        var userInput = new java.util.Scanner("Flood\nCoastal Ward 7, Chennai\nHigh\n1200\nRising water has blocked the eastern access road.\n");
        var incidentFromUser = edu.disasternet.Main.readIncident(userInput);
        ok("Flood".equals(incidentFromUser.disasterType()), "CLI disaster input");
        ok("Coastal Ward 7, Chennai".equals(incidentFromUser.location()), "CLI location input");
        ok("High".equals(incidentFromUser.severity()), "CLI severity input");
        ok(incidentFromUser.peopleAffected() == 1200, "CLI population input");

        System.out.println("All algorithm correctness tests passed.");
    }
}
