package edu.disasternet.models;

public class Incident {
    private final String id;
    private String location;
    private String disasterType;
    private String description;
    private String severity;
    private int affectedPeople;
    private String status;
    private long timestamp;

    public Incident(String id, String location, String disasterType, String description, String severity, int affectedPeople, String status) {
        this.id = id;
        this.location = location;
        this.disasterType = disasterType;
        this.description = description;
        this.severity = severity;
        this.affectedPeople = affectedPeople;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public String getLocation() { return location; }
    public String getDisasterType() { return disasterType; }
    public String getDescription() { return description; }
    public String getSeverity() { return severity; }
    public int getAffectedPeople() { return affectedPeople; }
    public String getStatus() { return status; }
    public long getTimestamp() { return timestamp; }

    public void setLocation(String location) { this.location = location; }
    public void setDisasterType(String disasterType) { this.disasterType = disasterType; }
    public void setDescription(String description) { this.description = description; }
    public void setSeverity(String severity) { this.severity = severity; }
    public void setAffectedPeople(int affectedPeople) { this.affectedPeople = affectedPeople; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return id + " | " + location + " | " + disasterType + " | " + severity + " | " + affectedPeople + " | " + status;
    }
}
