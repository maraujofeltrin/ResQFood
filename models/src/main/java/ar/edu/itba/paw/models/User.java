package ar.edu.itba.paw.models;

public class User {
    public enum Role {
        CLIENT,
        COMMERCE
    }

    private String email;
    private String password;
    private String name;
    private String phone;
    private Role role;

    private final Long id;
    
    public User(Long id, String email, String password, String name) {
        this(id, email, password, name, null, null);
    }

    public User(Long id, String email, String password, String name, String phone, Role role) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.role = role;
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

    public String getPhone() {
        return phone;
    }

    public Role getRole() {
        return role;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", email=" + email + ", password=" + password + ", name=" + name
                + ", phone=" + phone + ", role=" + role + "]";
    }
}
