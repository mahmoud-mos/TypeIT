package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Abstract base class for all message types
// Serializable allows Message objects to be sent over sockets as byte streams
public abstract class Message implements Serializable {

    private final String senderUsername;
    private final LocalDateTime timestamp;

    public Message(String senderUsername) {
        this.senderUsername = senderUsername;
        this.timestamp = LocalDateTime.now();
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public String getFormattedTime() {
        return timestamp.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    // Each subclass formats itself differently for display
    public abstract String format();
}