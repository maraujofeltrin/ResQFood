package ar.edu.itba.paw.models.user;

import java.util.Locale;

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
    private final boolean verified;
    private final Locale locale;

    private final Long id;

    public User(final Long id, final String email, final String password, final String name) {
        this(id, email, password, name, null, null, false, Locale.forLanguageTag("es"));
    }

    public User(final Long id, final String email, final String password, final String name, final String phone,
            final Role role, final boolean verified) {
        this(id, email, password, name, phone, role, verified, Locale.forLanguageTag("es"));
    }

    public User(final Long id, final String email, final String password, final String name, final String phone,
            final Role role, final boolean verified, final Locale locale) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.role = role;
        this.verified = verified;
        this.locale = locale;
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

    public boolean isVerified() {
        return verified;
    }

    public Locale getLocale() {
        return locale;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", email=" + email + ", password=" + password + ", name=" + name
                + ", phone=" + phone + ", role=" + role + ", verified=" + verified + ", locale=" + locale + "]";
    }
}
