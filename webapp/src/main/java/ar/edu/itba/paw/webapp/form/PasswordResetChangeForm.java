package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import ar.edu.itba.paw.webapp.validation.constraints.FieldMatch;


@FieldMatch(first = "newPassword", second = "confirmPassword", message = "{passwordReset.validation.passwords.mismatch}")
public class PasswordResetChangeForm {

    private String token;

    @NotBlank(message = "{passwordReset.validation.password.notEmpty}")
    @Size(min = 8, max = 100, message = "{passwordReset.validation.password.size}")
    private String newPassword;

    @NotBlank(message = "{passwordReset.validation.confirmPassword.notEmpty}")
    private String confirmPassword;

    public String getNewPassword() {
        return newPassword;
    }

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public void setNewPassword(final String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(final String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    // Password equality validated by @FieldMatch on the bean
}
