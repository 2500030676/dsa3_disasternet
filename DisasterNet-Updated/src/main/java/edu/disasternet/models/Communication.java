package edu.disasternet.models;

public class Communication {
    private String communicationId;
    private String sender;
    private String receiver;
    private String message;
    private String priority;
    private long timestamp;

    public Communication(String communicationId, String sender, String receiver, String message, String priority) {
        this.communicationId = communicationId;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.priority = priority;
        this.timestamp = System.currentTimeMillis();
    }

    public String getCommunicationId() { return communicationId; }
    public String getSender() { return sender; }
    public String getReceiver() { return receiver; }
    public String getMessage() { return message; }
    public String getPriority() { return priority; }
    public long getTimestamp() { return timestamp; }
}
