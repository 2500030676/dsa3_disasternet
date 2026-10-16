package edu.disasternet.cli;

import edu.disasternet.models.Shelter;
import edu.disasternet.data.DataStore;
import edu.disasternet.utils.ConsoleColors;
import edu.disasternet.utils.ConsoleInput;

import java.util.*;

public class ShelterMenu {
    private final List<Shelter> shelters = DataStore.SHELTERS;

    public ShelterMenu() {
    }

    public void show(Scanner scanner) {
        while (true) {
            System.out.println();
            System.out.println(ConsoleColors.color("============================================================", ConsoleColors.BLUE));
            System.out.println(ConsoleColors.color("                 SHELTERS & RELIEF CENTERS", ConsoleColors.CYAN));
            System.out.println(ConsoleColors.color("============================================================", ConsoleColors.BLUE));
            System.out.println("1. Add Shelter");
            System.out.println("2. View Shelters");
            System.out.println("3. Search Shelter");
            System.out.println("4. Allocate People");
            System.out.println("5. Capacity Analysis");
            System.out.println("6. Back");
            System.out.print("Enter your choice: ");
            if (!scanner.hasNextLine()) return;
            String option = scanner.nextLine().trim();
            switch (option) {
                case "1" -> addShelter(scanner);
                case "2" -> viewShelters();
                case "3" -> searchShelter(scanner);
                case "4" -> allocatePeople(scanner);
                case "5" -> capacityAnalysis();
                case "6" -> { return; }
                default -> System.out.println(ConsoleColors.color("[WARNING] Invalid menu option.", ConsoleColors.YELLOW));
            }
        }
    }

    private void addShelter(Scanner scanner) {
        System.out.print("Shelter ID: ");
        String id = scanner.nextLine().trim();
        if (id.isBlank() || shelters.stream().anyMatch(s -> s.getShelterId().equalsIgnoreCase(id))) { System.out.println(ConsoleColors.color("[WARNING] ID must be non-empty and unique.", ConsoleColors.YELLOW)); return; }
        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Location: ");
        String location = scanner.nextLine();
        if (name.isBlank() || location.isBlank()) { System.out.println(ConsoleColors.color("[WARNING] Name and location are required.", ConsoleColors.YELLOW)); return; }
        int capacity = ConsoleInput.readInt(scanner, "Capacity: ", 1);
        shelters.add(new Shelter(id, name, location, capacity, 0, true, 100, 80));
        System.out.println(ConsoleColors.color("[GREEN] Shelter added.", ConsoleColors.GREEN));
    }

    private void viewShelters() {
        System.out.println(ConsoleColors.color("\n=== SHELTER GRID ===", ConsoleColors.CYAN));
        System.out.printf("%-10s %-20s %-12s %-10s %-10s%n", "ID", "NAME", "LOCATION", "CAPACITY", "AVAILABLE");
        for (Shelter shelter : shelters) {
            System.out.printf("%-10s %-20s %-12s %-10d %-10d%n", shelter.getShelterId(), shelter.getName(), shelter.getLocation(), shelter.getCapacity(), shelter.getAvailable());
        }
    }

    private void searchShelter(Scanner scanner) {
        System.out.print("Enter shelter ID: ");
        String shelterId = scanner.nextLine();
        for (Shelter shelter : shelters) {
            if (shelter.getShelterId().equalsIgnoreCase(shelterId)) {
                System.out.println("Found shelter: " + shelter.getName() + " in " + shelter.getLocation());
                return;
            }
        }
        System.out.println(ConsoleColors.color("[WARNING] Shelter not found.", ConsoleColors.YELLOW));
    }

    private void allocatePeople(Scanner scanner) {
        System.out.print("Enter shelter ID: ");
        String shelterId = scanner.nextLine();
        int count = ConsoleInput.readInt(scanner, "Number of people to allocate: ", 1);
        for (Shelter shelter : shelters) {
            if (shelter.getShelterId().equalsIgnoreCase(shelterId)) {
                int newOccupancy = shelter.getOccupied() + count;
                if (newOccupancy > shelter.getCapacity()) {
                    System.out.println(ConsoleColors.color("[WARNING] Capacity exceeded.", ConsoleColors.YELLOW));
                    return;
                }
                shelter.setOccupied(newOccupancy);
                System.out.println(ConsoleColors.color("[GREEN] Allocation successful.", ConsoleColors.GREEN));
                return;
            }
        }
        System.out.println(ConsoleColors.color("[WARNING] Shelter not found.", ConsoleColors.YELLOW));
    }

    private void capacityAnalysis() {
        System.out.println(ConsoleColors.color("\n=== SHELTER CAPACITY ANALYSIS ===", ConsoleColors.CYAN));
        for (Shelter shelter : shelters) {
            System.out.println(shelter.getShelterId() + " | Occupied: " + shelter.getOccupied() + " | Available: " + shelter.getAvailable() + " | Status: " + (shelter.getAvailable() <= 50 ? "NEAR FULL" : "AVAILABLE"));
        }
    }
}
