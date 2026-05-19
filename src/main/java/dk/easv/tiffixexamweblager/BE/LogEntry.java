package dk.easv.tiffixexamweblager.BE;

import java.time.LocalDateTime;

public class LogEntry {

    private final int id;
    private final String username;
    private final String type;
    private final String message;
    private final LocalDateTime createdAt;

    public LogEntry(int id, String username, String type,
                    String message, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.type = type;
        this.message = message;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}