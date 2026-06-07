package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class ClientProfileForm {

    @NotBlank(message = "{register.validation.clientName.notEmpty}")
    @Size(max = 100, message = "{register.validation.clientName.size}")
    @Pattern(regexp = "^[\\p{L}]+(?:\\s+[\\p{L}]+)*$", message = "{register.validation.clientName.pattern}")
    private String firstName;

    @NotBlank(message = "{register.validation.clientLastName.notEmpty}")
    @Size(max = 100, message = "{register.validation.clientLastName.size}")
    @Pattern(regexp = "^[\\p{L}]+(?:\\s+[\\p{L}]+)*$", message = "{register.validation.clientLastName.pattern}")
    private String lastName;

    private Boolean notificationsVisibilityPreferences;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public Boolean getNotificationsVisibilityPreferences() {
        return notificationsVisibilityPreferences;
    }

    public void setNotificationsVisibilityPreferences(final Boolean notificationsVisibilityPreferences) {
        this.notificationsVisibilityPreferences = notificationsVisibilityPreferences;
    }
}
