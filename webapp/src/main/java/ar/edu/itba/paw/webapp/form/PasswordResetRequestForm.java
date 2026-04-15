package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class PasswordResetRequestForm {

    @NotBlank(message = "{passwordReset.validation.email.notEmpty}")
    @Email(message = "{passwordReset.validation.email.valid}")
    @Size(max = 255)
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }
}
