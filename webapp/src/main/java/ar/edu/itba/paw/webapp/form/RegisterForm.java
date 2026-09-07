package ar.edu.itba.paw.webapp.form;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class RegisterForm {

    /** Validated in {@code RegisterFormValidator} (role value + not empty). */
    private String role;

    @Valid
    @NotNull(message = "{register.validation.credentials.notNull}")
    private UserCredentialsForm credentials;

    private ClientProfileForm clientProfile;

    private CommerceProfileForm commerceProfile;

    public RegisterForm() {
        this.credentials = new UserCredentialsForm();
        this.clientProfile = new ClientProfileForm();
        this.commerceProfile = new CommerceProfileForm();
    }

    public String getRole() {
        return role;
    }

    public void setRole(final String role) {
        this.role = role;
    }

    public UserCredentialsForm getCredentials() {
        return credentials;
    }

    public void setCredentials(final UserCredentialsForm credentials) {
        this.credentials = credentials;
    }

    public ClientProfileForm getClientProfile() {
        return clientProfile;
    }

    public void setClientProfile(final ClientProfileForm clientProfile) {
        this.clientProfile = clientProfile;
    }

    public CommerceProfileForm getCommerceProfile() {
        return commerceProfile;
    }

    public void setCommerceProfile(final CommerceProfileForm commerceProfile) {
        this.commerceProfile = commerceProfile;
    }
}
