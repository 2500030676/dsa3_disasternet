package edu.disasternet.models;

public class Volunteer {
    private String volunteerId;
    private String name;
    private String skill;
    private String location;
    private boolean available;

    public Volunteer(String volunteerId, String name, String skill, String location, boolean available) {
        this.volunteerId = volunteerId;
        this.name = name;
        this.skill = skill;
        this.location = location;
        this.available = available;
    }

    public String getVolunteerId() { return volunteerId; }
    public String getName() { return name; }
    public String getSkill() { return skill; }
    public String getLocation() { return location; }
    public boolean isAvailable() { return available; }
}
