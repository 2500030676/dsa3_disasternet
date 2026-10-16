package edu.disasternet.models;

public class Shelter {
    private String shelterId;
    private String name;
    private String location;
    private int capacity;
    private int occupied;
    private boolean medicalFacility;
    private int foodAvailable;
    private int waterAvailable;

    public Shelter(String shelterId, String name, String location, int capacity, int occupied, boolean medicalFacility, int foodAvailable, int waterAvailable) {
        this.shelterId = shelterId;
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.occupied = occupied;
        this.medicalFacility = medicalFacility;
        this.foodAvailable = foodAvailable;
        this.waterAvailable = waterAvailable;
    }

    public String getShelterId() { return shelterId; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public int getCapacity() { return capacity; }
    public int getOccupied() { return occupied; }
    public int getAvailable() { return capacity - occupied; }
    public boolean isMedicalFacility() { return medicalFacility; }
    public int getFoodAvailable() { return foodAvailable; }
    public int getWaterAvailable() { return waterAvailable; }

    public void setOccupied(int occupied) { this.occupied = occupied; }
    public void setFoodAvailable(int foodAvailable) { this.foodAvailable = foodAvailable; }
    public void setWaterAvailable(int waterAvailable) { this.waterAvailable = waterAvailable; }
}
