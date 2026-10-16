package edu.disasternet;

import edu.disasternet.cli.MainMenu;
import edu.disasternet.simulations.DisasterSimulation;
import edu.disasternet.algorithms.stringalg.StringAlgorithms;
import java.util.*;

/**
 * DisasterNet CLI entry point for emergency-response simulation.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            new MainMenu().start();
            return;
        }

        String cmd = args[0];

        if ("demo".equalsIgnoreCase(cmd)) {
            System.out.println(DisasterSimulation.run());
        } else if ("input".equalsIgnoreCase(cmd) || "interactive".equalsIgnoreCase(cmd)) {
            DisasterSimulation.IncidentDetails incident = readIncident(new Scanner(System.in));
            System.out.println(DisasterSimulation.run(incident));
        } else if ("search".equalsIgnoreCase(cmd)) {
            String q = args.length > 1 ? args[1] : "medical";
            String text = java.nio.file.Files.readString(
                java.nio.file.Path.of("data/reports.txt")
            ).toLowerCase();

            System.out.println("KMP matches: " +
                StringAlgorithms.kmp(text, q.toLowerCase()));
        } else {
            System.out.println("Usage: demo | input | search <phrase>");
        }
    }

    static DisasterSimulation.IncidentDetails readIncident(Scanner scanner) {
        String disasterType = readRequired(scanner, "Disaster type", "Flood");
        String location = readRequired(scanner, "Affected location", "Coastal Ward 7, Chennai");
        String severity = readRequired(scanner, "Severity (Low/Moderate/High/Critical)", "High");
        int peopleAffected = readPositiveInt(scanner, "People affected", 1200);
        String description = readOptional(scanner, "Brief description", "No additional details provided.");

        return new DisasterSimulation.IncidentDetails(disasterType, location, severity, peopleAffected, description);
    }

    private static String readRequired(Scanner scanner, String prompt, String defaultValue) {
        while (true) {
            System.out.print(prompt + " [" + defaultValue + "]: ");
            String value = scanner.nextLine();
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }
            String trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                return trimmed;
            }
        }
    }

    private static String readOptional(Scanner scanner, String prompt, String defaultValue) {
        System.out.print(prompt + " [" + defaultValue + "]: ");
        String value = scanner.nextLine();
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }

    private static int readPositiveInt(Scanner scanner, String prompt, int defaultValue) {
        while (true) {
            System.out.print(prompt + " [" + defaultValue + "]: ");
            String value = scanner.nextLine();
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }
            try {
                int parsed = Integer.parseInt(value.trim());
                if (parsed > 0) {
                    return parsed;
                }
                System.out.println("Please enter a positive number.");
            } catch (NumberFormatException ignored) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }
}
