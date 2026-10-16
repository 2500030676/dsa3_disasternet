package edu.disasternet.models;

public class Observation {
    private String observationId;
    private String location;
    private String type;
    private String description;
    private int affectedPeople;
    private long timestamp;

    public Observation(String observationId, String location, String type, String description, int affectedPeople) {
        this.observationId = observationId;
        this.location = location;
        this.type = type;
        this.description = description;
        this.affectedPeople = affectedPeople;
        this.timestamp = System.currentTimeMillis();
    }

    public String getObservationId() { return observationId; }
    public String getLocation() { return location; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public int getAffectedPeople() { return affectedPeople; }
    public long getTimestamp() { return timestamp; }
}
