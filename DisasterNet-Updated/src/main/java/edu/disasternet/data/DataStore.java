package edu.disasternet.data;

import edu.disasternet.models.*;
import java.util.*;

/** Shared in-memory emergency data for all menus during one application run. */
public final class DataStore {
    public static final List<Incident> INCIDENTS = new ArrayList<>();
    public static final List<RescueTeam> TEAMS = new ArrayList<>();
    public static final List<Volunteer> VOLUNTEERS = new ArrayList<>();
    public static final List<Shelter> SHELTERS = new ArrayList<>();
    public static final List<Resource> RESOURCES = new ArrayList<>();
    public static final List<Communication> COMMUNICATIONS = new ArrayList<>();
    public static final List<Observation> OBSERVATIONS = new ArrayList<>();

    static {
        String[] locations = {"Vijayawada", "Guntur", "Krishna", "Eluru", "Amaravati", "Rajahmundry", "Visakhapatnam"};
        String[] types = {"Flood", "Cyclone", "Fire", "Landslide", "Earthquake", "Medical Emergency"};
        String[] descriptions = {
            "Heavy flood water entered residential areas and families need evacuation.",
            "Cyclone winds damaged roofs; injured residents request medical support.",
            "Fire spread near a warehouse; responders need a clear access road.",
            "Landslide debris blocked the road and isolated nearby households.",
            "Earthquake damage reported; search teams are checking collapsed buildings.",
            "Medical team requested at the shelter for first aid and oxygen."
        };
        for (int i = 0; i < 20; i++) {
            INCIDENTS.add(new Incident("INC-%03d".formatted(101 + i), locations[i % locations.length], types[i % types.length], descriptions[i % descriptions.length],
                i % 5 == 0 ? "CRITICAL" : i % 3 == 0 ? "HIGH" : "MEDIUM", 45 + (i * 37) % 420, i % 4 == 0 ? "UNDER INVESTIGATION" : "ACTIVE"));
        }
        String[] teamNames = {"Delta Flood Rescue", "Guntur Medical Unit", "Coastal Fire Crew", "Krishna Search Team", "Amaravati Evacuation Unit", "Eluru Logistics", "Vizag Urban Rescue", "River Patrol Alpha", "Cyclone Response Bravo", "District First Aid"};
        String[] skills = {"Flood Rescue", "Medical Support", "Fire Rescue", "Search and Rescue", "Evacuation", "Logistics", "Urban Rescue", "Water Rescue", "Cyclone Response", "First Aid"};
        for (int i = 0; i < 10; i++) TEAMS.add(new RescueTeam("RT-%02d".formatted(i + 1), teamNames[i], skills[i], 6 + i, locations[i % locations.length], true, 60 + i * 10));
        String[] volunteerSkills = {"First Aid", "Logistics", "Driving", "Translation", "Food Distribution"};
        for (int i = 0; i < 20; i++) VOLUNTEERS.add(new Volunteer("VOL-%03d".formatted(i + 1), "Volunteer %02d".formatted(i + 1), volunteerSkills[i % volunteerSkills.length], locations[i % locations.length], true));
        String[] shelterNames = {"Riverbank Relief Camp", "Central Community Hall", "Coastal Safe Shelter", "District School Shelter", "Medical Relief Centre", "Highway Evacuation Camp", "Municipal Relief Hall", "East Ward Shelter", "Cyclone Relief Centre", "University Emergency Hall"};
        for (int i = 0; i < 10; i++) {
            int capacity = 250 + i * 55;
            SHELTERS.add(new Shelter("SH-%02d".formatted(i + 1), shelterNames[i], locations[i % locations.length], capacity, 50 + i * 13, i % 3 == 0, 150 + i * 20, 120 + i * 18));
        }
        String[] supplies = {"Medical Kit", "Drinking Water", "Food Packet", "Family Tent", "Blanket", "Oxygen Cylinder", "First Aid Pack", "Water Purifier", "Emergency Radio", "Rescue Rope", "Life Jacket", "Generator", "Battery Pack", "Sanitation Kit", "Baby Care Kit", "Flashlight", "Tarpaulin", "Fuel Canister", "ORS Packet", "Protective Mask"};
        for (int i = 0; i < supplies.length; i++) RESOURCES.add(new Resource("RES-%03d".formatted(i + 1), supplies[i], 80 + i * 17, 20 + (i % 6) * 10, 1 + i % 8));
        for (int i = 0; i < 30; i++) COMMUNICATIONS.add(new Communication("COM-%03d".formatted(i + 1), "Field Team " + (i % 6 + 1), "Control Room", descriptions[i % descriptions.length], i % 5 == 0 ? "CRITICAL" : i % 2 == 0 ? "HIGH" : "MEDIUM"));
        for (int i = 0; i < 50; i++) OBSERVATIONS.add(new Observation("OBS-%03d".formatted(i + 1), locations[i % locations.length], types[i % types.length], descriptions[i % descriptions.length], 5 + (i * 19) % 180));
    }

    private DataStore() {}
}
