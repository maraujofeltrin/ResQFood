package ar.edu.itba.paw.models;

public class User {
    private final String email;
    private final String password;
    private final String name;

    private final Long id;
    
    public User(Long id, String email, String password, String name) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }
    public String getName() {
        return name;
    }
    public Long getId() {
        return id;
    }
    @Override
    public String toString() {
        return "User [id=" + id + ", email=" + email + ", password=" + password + ", name=" + name + "]";
    }
}
