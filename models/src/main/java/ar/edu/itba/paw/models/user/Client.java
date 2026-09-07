package ar.edu.itba.paw.models.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "clients")
public class Client {
    @Id
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false, name = "last_name")
    private String lastName;
    @Column(nullable = false, name = "notifications_visibility_preferences")
    private Boolean notificationsVisibilityPreferences;

    protected Client() {}

    public Client(final User user, final String name, final String lastName,
            final Boolean notificationsVisibilityPreferences) {
        this.user = user;
        this.userId = user != null ? user.getId() : null;
        this.name = name;
        this.lastName = lastName;
        this.notificationsVisibilityPreferences = notificationsVisibilityPreferences;
    }

    public Client(final Long userId, final String name, final String lastName,
            final Boolean notificationsVisibilityPreferences) {
        this(userId != null ? new User(userId, "stub@local", "p", "n", null, User.Role.CLIENT, false) : null, name,
                lastName, notificationsVisibilityPreferences);
    }

    public User getUser() {
        return user;
    }

    public Long getUserId() {
        return user != null ? user.getId() : userId;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    public Boolean getNotificationsVisibilityPreferences() {
        return notificationsVisibilityPreferences;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public void setNotificationsVisibilityPreferences(final Boolean notificationsVisibilityPreferences) {
        this.notificationsVisibilityPreferences = notificationsVisibilityPreferences;
    }

    /**
     * Returns the client's full name as "firstName lastName", trimmed.
     * If both name and lastName are blank, returns "-".
     */
    public String getFullName() {
        final String first = name == null ? "" : name.trim();
        final String last = lastName == null ? "" : lastName.trim();
        final String full = (first + " " + last).trim();
        return full.isEmpty() ? "-" : full;
    }

    @Override
    public String toString() {
        return "Client [userId=" + getUserId() + ", name=" + name + ", lastName=" + lastName
                + ", notificationsVisibilityPreferences=" + notificationsVisibilityPreferences + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Client)) return false;
        Client that = (Client) o;
        return userId != null && userId.equals(that.getUserId());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(userId);
    }
}