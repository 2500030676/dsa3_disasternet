package edu.disasternet.cli;

import edu.disasternet.algorithms.flow.FlowAlgorithms;
import edu.disasternet.models.RescueTeam;
import edu.disasternet.models.Volunteer;
import edu.disasternet.data.DataStore;
import edu.disasternet.utils.ConsoleColors;
import edu.disasternet.utils.ConsoleInput;

import java.util.*;

public class TeamMenu {
    private final List<RescueTeam> teams = DataStore.TEAMS;
    private final List<Volunteer> volunteers = DataStore.VOLUNTEERS;

    public TeamMenu() {
    }

    public void show(Scanner scanner) {
        while (true) {
            System.out.println();
            System.out.println(ConsoleColors.color("============================================================", ConsoleColors.BLUE));
            System.out.println(ConsoleColors.color("                 RESCUE TEAMS & VOLUNTEERS", ConsoleColors.CYAN));
            System.out.println(ConsoleColors.color("============================================================", ConsoleColors.BLUE));
            System.out.println("1. Add Rescue Team");
            System.out.println("2. Add Volunteer");
            System.out.println("3. View Teams");
            System.out.println("4. View Volunteers");
            System.out.println("5. Assign Teams to Response Tasks");
            System.out.println("6. Analyze Matching and Conflict Cover");
            System.out.println("7. Release All Teams");
            System.out.println("8. Back");
            System.out.print("Enter your choice: ");
            if (!scanner.hasNextLine()) return;
            String option = scanner.nextLine().trim();
            switch (option) {
                case "1" -> addTeam(scanner);
                case "2" -> addVolunteer(scanner);
                case "3" -> viewTeams();
                case "4" -> viewVolunteers();
                case "5" -> assignTeams();
                case "6" -> optimizeAssignments();
                case "7" -> releaseAllTeams();
                case "8" -> { return; }
                default -> System.out.println(ConsoleColors.color("[WARNING] Invalid menu option.", ConsoleColors.YELLOW));
            }
        }
    }

    private void addTeam(Scanner scanner) {
        System.out.print("Team ID: ");
        String id = scanner.nextLine().trim();
        if (id.isBlank() || teams.stream().anyMatch(t -> t.getTeamId().equalsIgnoreCase(id))) { warnDuplicate(); return; }
        System.out.print("Team Name: ");
        String name = scanner.nextLine();
        System.out.print("Specialization: ");
        String specialization = scanner.nextLine();
        if (name.isBlank() || specialization.isBlank()) { System.out.println(ConsoleColors.color("[WARNING] Team name and specialization are required.", ConsoleColors.YELLOW)); return; }
        int members = ConsoleInput.readInt(scanner, "Members: ", 1);
        System.out.print("Location: ");
        String location = scanner.nextLine();
        if (location.isBlank()) { System.out.println(ConsoleColors.color("[WARNING] Location is required.", ConsoleColors.YELLOW)); return; }
        teams.add(new RescueTeam(id, name, specialization, members, location, true, members * 10));
        System.out.println(ConsoleColors.color("[GREEN] Team added successfully.", ConsoleColors.GREEN));
    }

    private void addVolunteer(Scanner scanner) {
        System.out.print("Volunteer ID: ");
        String id = scanner.nextLine().trim();
        if (id.isBlank() || volunteers.stream().anyMatch(v -> v.getVolunteerId().equalsIgnoreCase(id))) { warnDuplicate(); return; }
        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Skill: ");
        String skill = scanner.nextLine();
        System.out.print("Location: ");
        String location = scanner.nextLine();
        if (name.isBlank() || skill.isBlank() || location.isBlank()) { System.out.println(ConsoleColors.color("[WARNING] Volunteer name, skill and location are required.", ConsoleColors.YELLOW)); return; }
        volunteers.add(new Volunteer(id, name, skill, location, true));
        System.out.println(ConsoleColors.color("[GREEN] Volunteer added successfully.", ConsoleColors.GREEN));
    }

    private void viewTeams() {
        System.out.println(ConsoleColors.color("\n=== TEAM SUMMARY ===", ConsoleColors.CYAN));
        System.out.printf("%-10s %-22s %-20s %-8s %-18s %-10s%n", "ID", "NAME", "SPECIALIZATION", "MEMBERS", "LOCATION", "AVAILABLE");
        for (RescueTeam team : teams) {
            System.out.printf("%-10s %-22s %-20s %-8d %-18s %-10s%n", team.getTeamId(), team.getName(), team.getSpecialization(), team.getMembers(), team.getLocation(), team.isAvailable() ? "YES" : "NO");
        }
    }

    private void viewVolunteers() {
        System.out.println(ConsoleColors.color("\n=== VOLUNTEER ROSTER ===", ConsoleColors.CYAN));
        System.out.printf("%-12s %-20s %-20s %-18s %-10s%n", "ID", "NAME", "SKILL", "LOCATION", "AVAILABLE");
        for (Volunteer volunteer : volunteers) System.out.printf("%-12s %-20s %-20s %-18s %-10s%n", volunteer.getVolunteerId(), volunteer.getName(), volunteer.getSkill(), volunteer.getLocation(), volunteer.isAvailable() ? "YES" : "NO");
    }

    private void assignTeams() {
        Map<String, List<String>> compat = compatibilityGraph();
        Map<String, String> match = FlowAlgorithms.bipartiteMatching(compat);
        System.out.println(ConsoleColors.color("\n=== TEAM ASSIGNMENT ===", ConsoleColors.CYAN));
        if (match.isEmpty()) System.out.println("No available teams match the current response tasks.");
        match.forEach((task, team) -> System.out.println(team + " -> " + task));
        for (String assignedName : match.values()) teams.stream().filter(team -> team.getName().equals(assignedName)).forEach(team -> team.setAvailable(false));
        System.out.println("Maximum Matching: " + match.size() + " / " + compat.size() + " available teams");
        System.out.println("Algorithm: augmenting-path bipartite matching; O(VE) time, O(V+E) space.");
    }

    private void optimizeAssignments() {
        Map<String, List<String>> compat = compatibilityGraph();
        Map<String, String> match = FlowAlgorithms.bipartiteMatching(compat);
        System.out.println(ConsoleColors.color("\n=== OPTIMIZED ASSIGNMENTS ===", ConsoleColors.CYAN));
        match.forEach((task, team) -> System.out.println(team + " -> " + task));
        System.out.println("Matching size: " + match.size());
        System.out.println("König cover size estimate: " + FlowAlgorithms.konigCover(compat, match).size());
        System.out.println("Minimum vertex cover equals maximum matching size for this bipartite graph (König's theorem).");
    }

    private Map<String, List<String>> compatibilityGraph() {
        List<String> tasks = List.of("Flood Rescue", "Medical Support", "Fire Rescue", "Search and Rescue", "Evacuation", "Logistics", "Urban Rescue", "Water Rescue", "Cyclone Response", "First Aid", "Food Distribution", "Shelter Support");
        Map<String, List<String>> graph = new LinkedHashMap<>();
        for (RescueTeam team : teams) {
            if (!team.isAvailable()) continue;
            List<String> compatible = new ArrayList<>();
            for (String task : tasks) if (team.getSpecialization().equalsIgnoreCase(task)
                    || (task.equals("Evacuation") && team.getSpecialization().toLowerCase(Locale.ROOT).contains("rescue"))
                    || (task.equals("Shelter Support") && team.getSpecialization().toLowerCase(Locale.ROOT).contains("logistics"))) compatible.add(task);
            graph.put(team.getName(), compatible);
        }
        return graph;
    }

    private void warnDuplicate() { System.out.println(ConsoleColors.color("[WARNING] ID must be non-empty and unique.", ConsoleColors.YELLOW)); }

    private void releaseAllTeams() {
        teams.forEach(team -> team.setAvailable(true));
        System.out.println(ConsoleColors.color("[GREEN] All rescue teams are available again.", ConsoleColors.GREEN));
    }
}
