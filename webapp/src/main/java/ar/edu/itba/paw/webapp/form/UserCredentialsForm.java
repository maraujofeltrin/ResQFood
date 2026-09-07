package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import ar.edu.itba.paw.webapp.validation.constraints.FieldMatch;

@FieldMatch(first = "password", second = "repeatPassword", message = "{user.password.mismatch}")
public class UserCredentialsForm {

    @NotBlank(message = "{register.validation.email.notEmpty}")
    @Email(message = "{register.validation.email.valid}")
    @Size(max = 255)
    private String email;

    @NotBlank(message = "{register.validation.phone.notEmpty}")
    @Size(max = 50, message = "{register.validation.phone.size}")
    @Pattern(regexp = "^[0-9]+$", message = "{register.validation.phone.pattern}")
    private String phone;

    @NotBlank(message = "{register.validation.password.notEmpty}")
    @Size(min = 8, max = 100, message = "{register.validation.password.size}")
    private String password;

    @NotBlank(message = "{register.validation.repeatPassword.notEmpty}")
    private String repeatPassword;

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(final String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getRepeatPassword() {
        return repeatPassword;
    }

    public void setRepeatPassword(final String repeatPassword) {
        this.repeatPassword = repeatPassword;
    }
}
