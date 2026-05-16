package model;

import java.io.Serializable;

// Abstract base class for all user types
public abstract class User implements Serializable {

    private final String username;
    private boolean online;

    public User(String username) {
        this.username = username;
        this.online = true;
    }

    public String getUsername() {
        return username;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    // Each user type defines what they're allowed to do
    public abstract String getRole();

    public abstract boolean canKickUsers();
}