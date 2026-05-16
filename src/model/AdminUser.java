package model;

// admin user has all regular permissions plus moderation powers
public class AdminUser extends User {

    public AdminUser(String username) {
        super(username);
    }

    @Override
    public String getRole() {
        return "Admin";
    }

    @Override
    public boolean canKickUsers() {
        return true;
    }
}