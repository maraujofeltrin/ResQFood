package ar.edu.itba.paw.models.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "clients")
public class Client {
    @Id
    @Column(name = "user_id")
    private Long userId;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, name = "last_name")
    private String lastName;
    @Column(nullable = false, name = "notifications_visibility_preferences")
    private Boolean notificationsVisibilityPreferences;

    protected Client() {}

    public Client(Long userId, String name, String lastName, Boolean notificationsVisibilityPreferences) {
        this.userId = userId;
        this.name = name;
        this.lastName = lastName;
        this.notificationsVisibilityPreferences = notificationsVisibilityPreferences;
    }

    public Long getUserId() {
        return userId;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setNotificationsVisibilityPreferences(Boolean notificationsVisibilityPreferences) {
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
        return "Client [userId=" + userId + ", name=" + name + ", lastName=" + lastName
                + ", notificationsVisibilityPreferences=" + notificationsVisibilityPreferences + "]";
    }
}
