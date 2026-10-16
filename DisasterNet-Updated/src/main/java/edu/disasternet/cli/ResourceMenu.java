package edu.disasternet.cli;

import edu.disasternet.algorithms.dp.DynamicProgramming;
import edu.disasternet.algorithms.flow.FlowAlgorithms;
import edu.disasternet.algorithms.stringalg.StringAlgorithms;
import edu.disasternet.models.Resource;
import edu.disasternet.data.DataStore;
import edu.disasternet.utils.ConsoleColors;
import edu.disasternet.utils.ConsoleInput;

import java.util.*;

public class ResourceMenu {
    private final List<Resource> resources = DataStore.RESOURCES;

    public ResourceMenu() {
    }

    public void show(Scanner scanner) {
        while (true) {
            System.out.println();
            System.out.println(ConsoleColors.color("============================================================", ConsoleColors.BLUE));
            System.out.println(ConsoleColors.color("                 RESOURCE MANAGEMENT", ConsoleColors.CYAN));
            System.out.println(ConsoleColors.color("============================================================", ConsoleColors.BLUE));
            System.out.println("1. Add Resource");
            System.out.println("2. View Resources");
            System.out.println("3. Search Resource");
            System.out.println("4. Allocate Resource");
            System.out.println("5. Optimize Resources");
            System.out.println("6. Supply Network");
            System.out.println("7. Detect Bottleneck");
            System.out.println("8. Back");
            System.out.print("Enter your choice: ");
            if (!scanner.hasNextLine()) return;
            String option = scanner.nextLine().trim();
            switch (option) {
                case "1" -> addResource(scanner);
                case "2" -> viewResources();
                case "3" -> searchResource(scanner);
                case "4" -> allocateResource(scanner);
                case "5" -> optimizeResources();
                case "6" -> supplyNetwork();
                case "7" -> bottleneck();
                case "8" -> { return; }
                default -> System.out.println(ConsoleColors.color("[WARNING] Invalid menu option.", ConsoleColors.YELLOW));
            }
        }
    }

    private void addResource(Scanner scanner) {
        System.out.print("Resource ID: ");
        String id = scanner.nextLine().trim();
        if (id.isBlank() || resources.stream().anyMatch(r -> r.getResourceId().equalsIgnoreCase(id))) { System.out.println(ConsoleColors.color("[WARNING] ID must be non-empty and unique.", ConsoleColors.YELLOW)); return; }
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        if (name.isBlank()) { System.out.println(ConsoleColors.color("[WARNING] Resource name is required.", ConsoleColors.YELLOW)); return; }
        int qty = ConsoleInput.readInt(scanner, "Quantity: ", 0);
        resources.add(new Resource(id, name, qty, 50, 10));
        System.out.println(ConsoleColors.color("[GREEN] Resource added.", ConsoleColors.GREEN));
    }

    private void viewResources() {
        System.out.println(ConsoleColors.color("\n=== RESOURCE SUMMARY ===", ConsoleColors.CYAN));
        System.out.printf("%-10s %-15s %-10s %-10s%n", "ID", "NAME", "QUANTITY", "VALUE");
        for (Resource resource : resources) {
            System.out.printf("%-10s %-15s %-10d %-10d%n", resource.getResourceId(), resource.getName(), resource.getQuantity(), resource.getValue());
        }
    }

    private void searchResource(Scanner scanner) {
        System.out.print("Enter resource name or ID fragment: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) { System.out.println(ConsoleColors.color("[WARNING] Search term cannot be empty.", ConsoleColors.YELLOW)); return; }
        int count = 0;
        for (Resource resource : resources) {
            boolean nameMatch = resource.getName().equalsIgnoreCase(name);
            boolean idMatch = !StringAlgorithms.rabinKarp(resource.getResourceId().toLowerCase(Locale.ROOT), name.toLowerCase(Locale.ROOT)).matches().isEmpty();
            if (nameMatch || idMatch) {
                System.out.println("Found: " + resource.getResourceId() + " | " + resource.getName() + " Qty=" + resource.getQuantity());
                count++;
            }
        }
        if (count == 0) System.out.println(ConsoleColors.color("[WARNING] Resource not found.", ConsoleColors.YELLOW));
        System.out.println("ID lookup uses Rabin-Karp; expected O(n+m), worst O(nm).");
    }

    private void allocateResource(Scanner scanner) {
        System.out.print("Resource name: ");
        String name = scanner.nextLine();
        int amount = ConsoleInput.readInt(scanner, "Amount to allocate: ", 1);
        for (Resource resource : resources) {
            if (resource.getName().equalsIgnoreCase(name)) {
                if (resource.getQuantity() < amount) {
                    System.out.println(ConsoleColors.color("[WARNING] Not enough quantity.", ConsoleColors.YELLOW));
                    return;
                }
                resource.setQuantity(resource.getQuantity() - amount);
                System.out.println(ConsoleColors.color("[GREEN] Allocation successful.", ConsoleColors.GREEN));
                return;
            }
        }
        System.out.println(ConsoleColors.color("[WARNING] Resource not found.", ConsoleColors.YELLOW));
    }

    private void optimizeResources() {
        List<DynamicProgramming.Item> items = resources.stream().filter(r -> r.getQuantity() > 0)
                .map(r -> new DynamicProgramming.Item(r.getName(), r.getWeight(), r.getValue())).toList();
        var result = DynamicProgramming.resourceSelection(items, 35);
        System.out.println(ConsoleColors.color("\n=== RESOURCE OPTIMIZATION ===", ConsoleColors.CYAN));
        System.out.println("Current stocked resource types considered: " + items.size() + " | weight/budget limit: 35");
        System.out.println("Selected Resources: " + result.items());
        System.out.println("Total planning weight: " + result.cost() + " / 35 | Coverage value: " + result.value());
        System.out.println("0/1 knapsack complexity: O(nB) time and O(nB) space.");
    }

    private void supplyNetwork() {
        List<FlowAlgorithms.Edge> edges = networkEdges();
        var fordFulkerson = FlowAlgorithms.fordFulkerson(7, edges, 0, 6);
        var edmondsKarp = FlowAlgorithms.edmondsKarp(7, edges, 0, 6);
        var result = FlowAlgorithms.dinic(7, edges, 0, 6);
        System.out.println(ConsoleColors.color("\n=== MAX FLOW ANALYSIS ===", ConsoleColors.CYAN));
        System.out.println("Warehouse inventory units: " + resources.stream().mapToInt(Resource::getQuantity).sum());
        System.out.println("Available shelter spaces: " + DataStore.SHELTERS.stream().mapToInt(s -> s.getAvailable()).sum());
        System.out.println("Ford-Fulkerson flow: " + fordFulkerson.maxFlow());
        System.out.println("Edmonds-Karp flow: " + edmondsKarp.maxFlow());
        System.out.println("Dinic maximum flow: " + result.maxFlow());
        System.out.println("All methods should agree; source side of the Dinic minimum cut: " + result.sourceSide());
        System.out.println("Ford-Fulkerson O(EF); Edmonds-Karp O(VE^2); Dinic O(V^2E) general bound.");
    }

    private void bottleneck() {
        System.out.println(ConsoleColors.color("\n=== BOTTLENECK DETECTION ===", ConsoleColors.CYAN));
        List<FlowAlgorithms.Edge> edges = networkEdges();
        var result = FlowAlgorithms.dinic(7, edges, 0, 6);
        System.out.println("Maximum flow: " + result.maxFlow());
        System.out.println("Minimum cut edges (from reachable source-side to sink-side):");
        int cutCapacity = 0;
        for (FlowAlgorithms.Edge edge : edges) if (result.sourceSide().contains(edge.from()) && !result.sourceSide().contains(edge.to())) {
            System.out.println("  node " + edge.from() + " -> node " + edge.to() + " | capacity " + edge.capacity());
            cutCapacity += edge.capacity();
        }
        System.out.println("Cut capacity: " + cutCapacity + " (equals maximum flow by max-flow/min-cut theorem)");
        System.out.println("Dinic: O(V^2E) general bound; O(V+E) graph space.");
    }

    private List<FlowAlgorithms.Edge> networkEdges() {
        int stock = resources.stream().mapToInt(Resource::getQuantity).sum();
        int warehouseA = Math.max(1, stock / 2), warehouseB = Math.max(1, stock - warehouseA);
        int road = Math.max(1, DataStore.SHELTERS.stream().mapToInt(s -> s.getAvailable()).sum() / 2);
        return List.of(new FlowAlgorithms.Edge(0,1,warehouseA), new FlowAlgorithms.Edge(0,2,warehouseB),
                new FlowAlgorithms.Edge(1,3,Math.max(1, warehouseA / 2)), new FlowAlgorithms.Edge(1,4,Math.max(1, warehouseA / 2)),
                new FlowAlgorithms.Edge(2,4,Math.max(1, warehouseB / 3)), new FlowAlgorithms.Edge(2,5,Math.max(1, warehouseB * 2 / 3)),
                new FlowAlgorithms.Edge(3,6,Math.max(1, road / 4)), new FlowAlgorithms.Edge(4,6,Math.max(1, road / 2)),
                new FlowAlgorithms.Edge(5,6,Math.max(1, road / 4)));
    }
}
