package edu.disasternet.cli;

import edu.disasternet.utils.ConsoleColors;
import edu.disasternet.data.DataStore;
import java.util.Scanner;

/** Reports live shared session data so changes made in menus appear immediately. */
public final class SystemStatisticsMenu {
    private SystemStatisticsMenu() {}

    public static void show(Scanner scanner) {
        System.out.println();
        System.out.println(ConsoleColors.color("====================== SYSTEM STATISTICS ======================", ConsoleColors.BLUE));
        long active = DataStore.INCIDENTS.stream().filter(i -> "ACTIVE".equalsIgnoreCase(i.getStatus())).count();
        long resolved = DataStore.INCIDENTS.stream().filter(i -> "RESOLVED".equalsIgnoreCase(i.getStatus())).count();
        long investigating = DataStore.INCIDENTS.stream().filter(i -> "UNDER INVESTIGATION".equalsIgnoreCase(i.getStatus())).count();
        int shelterCapacity = DataStore.SHELTERS.stream().mapToInt(s -> s.getAvailable()).sum();
        int volunteersAvailable = (int) DataStore.VOLUNTEERS.stream().filter(v -> v.isAvailable()).count();
        long teamsAvailable = DataStore.TEAMS.stream().filter(t -> t.isAvailable()).count();
        int totalResources = DataStore.RESOURCES.stream().mapToInt(r -> r.getQuantity()).sum();
        System.out.printf("Incidents                 : %d (active %d, investigating %d, resolved %d)%n", DataStore.INCIDENTS.size(), active, investigating, resolved);
        System.out.printf("Rescue teams available    : %d / %d%n", teamsAvailable, DataStore.TEAMS.size());
        System.out.printf("Volunteers available      : %d / %d%n", volunteersAvailable, DataStore.VOLUNTEERS.size());
        System.out.printf("Shelters                  : %d%n", DataStore.SHELTERS.size());
        System.out.printf("Available shelter capacity: %d people%n", shelterCapacity);
        System.out.printf("Resource units on hand    : %d across %d types%n", totalResources, DataStore.RESOURCES.size());
        System.out.printf("Communication records     : %d%n", DataStore.COMMUNICATIONS.size());
        System.out.printf("Field observations        : %d%n", DataStore.OBSERVATIONS.size());
        System.out.println("\nAlgorithm engine: ready");
        System.out.println("All menu modules use the same in-memory data for this session.");
        System.out.println("\nPress Enter to return to the main menu.");
        if (scanner.hasNextLine()) scanner.nextLine();
    }

}
