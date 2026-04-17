package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class EmailVerificationResendForm {

    @NotBlank(message = "{emailVerification.validation.email.notEmpty}")
    @Email(message = "{emailVerification.validation.email.valid}")
    @Size(max = 255, message = "{emailVerification.validation.email.size}")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }
}
