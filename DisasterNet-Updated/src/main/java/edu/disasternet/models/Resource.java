package edu.disasternet.models;

public class Resource {
    private String resourceId;
    private String name;
    private int quantity;
    private int value;
    private int weight;

    public Resource(String resourceId, String name, int quantity, int value, int weight) {
        this.resourceId = resourceId;
        this.name = name;
        this.quantity = quantity;
        this.value = value;
        this.weight = weight;
    }

    public String getResourceId() { return resourceId; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public int getValue() { return value; }
    public int getWeight() { return weight; }

    public void setQuantity(int quantity) { this.quantity = quantity; }
}
