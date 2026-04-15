package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class UserForm {

    @NotBlank(message = "{register.validation.name.notEmpty}")
    @Size(max = 100, message = "{register.validation.name.size}")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "{register.validation.name.pattern}")
    private String name;

    @NotBlank(message = "{register.validation.email.notEmpty}")
    @Email(message = "{register.validation.email.valid}")
    @Size(max = 255)
    private String email;

    @NotBlank(message = "{register.validation.password.notEmpty}")
    @Size(min = 8, max = 100, message = "{register.validation.password.size}")
    private String password;

    @NotBlank(message = "{register.validation.repeatPassword.notEmpty}")
    private String repeatPassword;

    @NotNull(message = "{register.validation.role.notNull}")
    private ar.edu.itba.paw.models.User.Role role;

    public ar.edu.itba.paw.models.User.Role getRole() {
        return role;
    }

    public void setRole(final ar.edu.itba.paw.models.User.Role role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
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