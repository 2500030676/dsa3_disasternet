package edu.disasternet.models;

public class RescueTeam {
    private String teamId;
    private String name;
    private String specialization;
    private int members;
    private String location;
    private boolean available;
    private int capacity;

    public RescueTeam(String teamId, String name, String specialization, int members, String location, boolean available, int capacity) {
        this.teamId = teamId;
        this.name = name;
        this.specialization = specialization;
        this.members = members;
        this.location = location;
        this.available = available;
        this.capacity = capacity;
    }

    public String getTeamId() { return teamId; }
    public String getName() { return name; }
    public String getSpecialization() { return specialization; }
    public int getMembers() { return members; }
    public String getLocation() { return location; }
    public boolean isAvailable() { return available; }
    public int getCapacity() { return capacity; }

    public void setAvailable(boolean available) { this.available = available; }
    public void setLocation(String location) { this.location = location; }
}
