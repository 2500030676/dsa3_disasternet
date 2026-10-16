package edu.disasternet.cli;

import edu.disasternet.utils.ConsoleColors;
import edu.disasternet.data.DataStore;

import java.util.Scanner;

public class MainMenu {
    private final IncidentMenu incidentMenu = new IncidentMenu();
    private final TeamMenu teamMenu = new TeamMenu();
    private final ShelterMenu shelterMenu = new ShelterMenu();
    private final ResourceMenu resourceMenu = new ResourceMenu();
    private final CommunicationMenu communicationMenu = new CommunicationMenu();
    private final ObservationMenu observationMenu = new ObservationMenu();
    private final AlgorithmLaboratoryMenu algorithmLaboratoryMenu = new AlgorithmLaboratoryMenu();

    public void start() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            printHeader();
            System.out.println("1. Incident Reports");
            System.out.println("2. Rescue Teams & Volunteers");
            System.out.println("3. Shelters & Relief Centers");
            System.out.println("4. Resource Management");
            System.out.println("5. Communication Records");
            System.out.println("6. Field Observations & Analytics");
            System.out.println("7. Algorithm Laboratory");
            System.out.println("8. System Statistics");
            System.out.println("9. Exit");
            System.out.println(ConsoleColors.color("------------------------------------------------------------", ConsoleColors.BLUE));
            System.out.print("Enter your choice: ");
            if (!scanner.hasNextLine()) return;
            String option = scanner.nextLine().trim();
            switch (option) {
                case "1" -> incidentMenu.show(scanner);
                case "2" -> teamMenu.show(scanner);
                case "3" -> shelterMenu.show(scanner);
                case "4" -> resourceMenu.show(scanner);
                case "5" -> communicationMenu.show(scanner);
                case "6" -> observationMenu.show(scanner);
                case "7" -> algorithmLaboratoryMenu.show(scanner);
                case "8" -> SystemStatisticsMenu.show(scanner);
                case "9" -> {
                    System.out.println(ConsoleColors.color("[GREEN] DisasterNet session closed.", ConsoleColors.GREEN));
                    return;
                }
                default -> System.out.println(ConsoleColors.color("[WARNING] Invalid menu option. Please try again.", ConsoleColors.YELLOW));
            }
        }
    }

    private void printHeader() {
        System.out.println();
        System.out.println(ConsoleColors.color("============================================================", ConsoleColors.BLUE));
        System.out.println(ConsoleColors.color("              DISASTERNET", ConsoleColors.BOLD + ConsoleColors.CYAN));
        System.out.println(ConsoleColors.color("       EMERGENCY RESPONSE CONTROL CENTER", ConsoleColors.CYAN));
        System.out.println(ConsoleColors.color("============================================================", ConsoleColors.BLUE));
        System.out.println(ConsoleColors.color("SYSTEM STATUS : ONLINE", ConsoleColors.GREEN));
        System.out.println(ConsoleColors.color("MODE          : TERMINAL", ConsoleColors.CYAN));
        System.out.println(ConsoleColors.color("ALGORITHM ENGINE : ACTIVE", ConsoleColors.YELLOW));
        long activeIncidents = DataStore.INCIDENTS.stream().filter(i -> "ACTIVE".equalsIgnoreCase(i.getStatus())).count();
        long availableTeams = DataStore.TEAMS.stream().filter(t -> t.isAvailable()).count();
        int shelterPlaces = DataStore.SHELTERS.stream().mapToInt(s -> s.getAvailable()).sum();
        int stockUnits = DataStore.RESOURCES.stream().mapToInt(r -> r.getQuantity()).sum();
        System.out.println(ConsoleColors.color("[CRITICAL] " + activeIncidents + " Active Incidents", ConsoleColors.RED));
        System.out.println(ConsoleColors.color("[INFO] " + availableTeams + " Rescue Teams Available | " + shelterPlaces + " Shelter Places | " + stockUnits + " Resource Units", ConsoleColors.CYAN));
        System.out.println(ConsoleColors.color("------------------------------------------------------------", ConsoleColors.BLUE));
    }
}
