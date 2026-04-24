package ar.edu.itba.paw.models.user;

public class Client {
    private final Long userId;
    private String name;
    private String lastName;
    private Boolean notificationsVisibilityPreferences;

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
