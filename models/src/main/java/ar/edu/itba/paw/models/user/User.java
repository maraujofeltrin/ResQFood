package ar.edu.itba.paw.models.user;

import ar.edu.itba.paw.models.image.Image;

import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
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
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_image_id")
    private Image profileImage;

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
        this(id, email, password, name, phone, role, verified, locale, (Image) null);
    }

    public User(final Long id, final String email, final String password, final String name, final String phone,
            final Role role, final boolean verified, final Locale locale, final Image profileImage) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.role = role;
        this.verified = verified;
        this.locale = locale;
        this.profileImage = profileImage;
    }

    public User(final Long id, final String email, final String password, final String name, final String phone,
            final Role role, final boolean verified, final Locale locale, final Long profileImageId) {
        this(id, email, password, name, phone, role, verified, locale,
                profileImageId != null ? new Image(profileImageId, new byte[0], "image/png") : null);
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

    public Image getProfileImage() {
        return profileImage;
    }

    public Long getProfileImageId() {
        return profileImage != null ? profileImage.getId() : null;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", email=" + email + ", password=" + password + ", name=" + name
                + ", phone=" + phone + ", role=" + role + ", verified=" + verified + ", locale=" + locale
                + ", profileImageId=" + getProfileImageId() + "]";
    }
}
