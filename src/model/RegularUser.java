package model;

// A standard user that can chat and send files but can not kick others
public class RegularUser extends User {

    public RegularUser(String username) {
        super(username);
    }

    @Override
    public String getRole() {
        return "User";
    }

    @Override
    public boolean canKickUsers() {
        return false;
    }
}