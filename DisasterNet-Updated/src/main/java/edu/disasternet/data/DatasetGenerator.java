package edu.disasternet.data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Creates reproducible, realistic linked DisasterNet datasets for scale demonstrations. */
public final class DatasetGenerator {
    private static final Random RANDOM = new Random(20260922L);
    private static final String[] LOCATIONS = {"Riverside", "Hillview", "Old Town", "Market Ward", "Greenfield", "Lakeside", "East Bank", "North Colony"};
    private static final String[] TYPES = {"Flood", "Cyclone", "Earthquake", "Fire", "Landslide", "Medical Emergency", "Road Blockage", "Missing Persons"};
    private static final String[] REPORTS = {
        "Flood water entered {location} homes; {people} residents require evacuation and clean water.",
        "Cyclone damage near {location} shelter: roof damaged, food packs and medical kits requested.",
        "Road blocked after landslide in {location}; rescue vehicle access is delayed.",
        "Medical emergency reported at {location} relief centre; injured people require oxygen support.",
        "Fire reported in {location}; volunteers are moving residents to the nearest shelter.",
        "Earthquake aftershock damaged buildings at {location}; search teams report trapped residents.",
        "Missing-person request from {location}; family last saw residents near an evacuation route.",
        "Shelter shortage in {location}; tents, food, water units and a generator are needed."
    };

    private DatasetGenerator() {}

    public static void main(String[] args) throws IOException {
        Path root = Path.of(args.length == 0 ? "data/generated" : args[0]);
        Files.createDirectories(root);
        for (int size : new int[] {100, 1_000, 10_000}) {
            Path folder = root.resolve("reports-" + size);
            Files.createDirectories(folder);
            writeDataset(folder, size);
            System.out.println("Created " + folder + " with " + size + " reports.");
        }
    }

    private static void writeDataset(Path folder, int count) throws IOException {
        List<String> reports = new ArrayList<>(count);
        List<String> incidents = new ArrayList<>(count + 2);
        List<String> observations = new ArrayList<>(count + 2);
        incidents.add("[");
        observations.add("[");
        for (int i = 1; i <= count; i++) {
            String id = String.format("INC-%05d", i);
            String location = LOCATIONS[RANDOM.nextInt(LOCATIONS.length)];
            String type = TYPES[RANDOM.nextInt(TYPES.length)];
            int severity = 35 + RANDOM.nextInt(66);
            int people = 2 + RANDOM.nextInt(79);
            String report = REPORTS[RANDOM.nextInt(REPORTS.length)]
                .replace("{location}", location).replace("{people}", String.valueOf(people));
            reports.add(id + "|" + location + "|" + report);
            incidents.add(String.format("  {\"id\":\"%s\",\"type\":\"%s\",\"severity\":%d,\"location\":\"%s\",\"peopleAffected\":%d}%s", id, type, severity, location, people, i == count ? "" : ","));
            observations.add(String.format("  {\"observationId\":\"OBS-%05d\",\"incidentId\":\"%s\",\"region\":\"%s\",\"affected\":%d,\"status\":\"%s\"}%s", i, id, location, people, severity > 80 ? "CRITICAL" : "ACTIVE", i == count ? "" : ","));
        }
        incidents.add("]"); observations.add("]");
        write(folder.resolve("reports.txt"), reports);
        write(folder.resolve("incidents.json"), incidents);
        write(folder.resolve("observations.json"), observations);
        write(folder.resolve("teams.json"), teams());
        write(folder.resolve("shelters.json"), shelters());
        write(folder.resolve("resources.json"), resources());
        write(folder.resolve("communication_records.txt"), communications(count));
    }

    private static List<String> teams() { return List.of("[", "  {\"team\":\"Alpha\",\"skills\":[\"flood rescue\",\"evacuation\"],\"capacity\":12},", "  {\"team\":\"Bravo\",\"skills\":[\"medical\",\"fire response\"],\"capacity\":10},", "  {\"team\":\"Charlie\",\"skills\":[\"shelter support\",\"food distribution\"],\"capacity\":15},", "  {\"team\":\"Delta\",\"skills\":[\"search and rescue\",\"landslide\"],\"capacity\":8}", "]"); }
    private static List<String> shelters() { return List.of("[", "  {\"id\":\"SH-01\",\"name\":\"Riverside School Shelter\",\"capacity\":350,\"occupancy\":215},", "  {\"id\":\"SH-02\",\"name\":\"Hillview Community Hall\",\"capacity\":250,\"occupancy\":170},", "  {\"id\":\"SH-03\",\"name\":\"Old Town Relief Centre\",\"capacity\":500,\"occupancy\":420}", "]"); }
    private static List<String> resources() { return List.of("[", "  {\"id\":\"SUP-BOAT-01\",\"name\":\"Rescue Boat\",\"quantity\":8,\"unitCost\":18000},", "  {\"id\":\"SUP-MED-01\",\"name\":\"Medical Kit\",\"quantity\":240,\"unitCost\":700},", "  {\"id\":\"SUP-WATER-01\",\"name\":\"Water Unit\",\"quantity\":5000,\"unitCost\":60},", "  {\"id\":\"SUP-FOOD-01\",\"name\":\"Food Pack\",\"quantity\":4000,\"unitCost\":90}", "]"); }
    private static List<String> communications(int count) { List<String> x = new ArrayList<>(); for (int i = 1; i <= Math.min(count, 500); i++) x.add(String.format("COM-%05d|%s|Dispatch acknowledged incident INC-%05d; team status update requested.", i, LOCATIONS[i % LOCATIONS.length], i)); return x; }
    private static void write(Path file, List<String> lines) throws IOException { Files.write(file, lines, StandardCharsets.UTF_8); }
}
