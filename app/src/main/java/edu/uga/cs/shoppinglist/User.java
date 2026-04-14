package edu.uga.cs.shoppinglist;

// POJO Object for User
public class User {
    private long id;
    public String displayName;
    public String email;
    private String password;


    public User() {
        this.id = -1;
        this.displayName = null;
        this.email = null;
        this.password = null;
    }

    public User(String displayName, String email, String password) {
        this.id = -1;
        this.displayName = displayName;
        this.email = email;
        this.password = password;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getDisplayName() { return displayName; }
    public void setDisplayName( String displayName) { this.displayName = displayName; }
    public String getEmail() { return email; }
    public void setEmail( String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword( String password) { this.password = password; }
}
