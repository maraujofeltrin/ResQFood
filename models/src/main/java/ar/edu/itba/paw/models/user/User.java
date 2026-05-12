package ar.edu.itba.paw.models.user;

import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    public enum Role {
        CLIENT,
        COMMERCE
    }

    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String phone;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    @Column(nullable = false)
    private boolean verified;
    
    @Convert(converter = LocaleConverter.class)
    @Column(nullable = false)
    private Locale locale;
    
    /** FK opcional a {@code images}; null si el usuario usa solo el avatar por defecto. */
    @Column(name = "profile_image_id")
    private Long profileImageId;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_id_seq")
    @SequenceGenerator(sequenceName = "users_id_seq", name = "users_id_seq", allocationSize = 1)
    private Long id;

    protected User() {
        // Just for Hibernate
    }

    public User(final Long id, final String email, final String password, final String name) {
        this(id, email, password, name, null, null, false, Locale.forLanguageTag("es"));
    }

    public User(final Long id, final String email, final String password, final String name, final String phone,
            final Role role, final boolean verified) {
        this(id, email, password, name, phone, role, verified, Locale.forLanguageTag("es"));
    }

    public User(final Long id, final String email, final String password, final String name, final String phone,
            final Role role, final boolean verified, final Locale locale) {
        this(id, email, password, name, phone, role, verified, locale, null);
    }

    public User(final Long id, final String email, final String password, final String name, final String phone,
            final Role role, final boolean verified, final Locale locale, final Long profileImageId) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.role = role;
        this.verified = verified;
        this.locale = locale;
        this.profileImageId = profileImageId;
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

    public Long getProfileImageId() {
        return profileImageId;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", email=" + email + ", password=" + password + ", name=" + name
                + ", phone=" + phone + ", role=" + role + ", verified=" + verified + ", locale=" + locale
                + ", profileImageId=" + profileImageId + "]";
    }
}
