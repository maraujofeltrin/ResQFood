package ar.edu.itba.paw.webapp.assembler;

import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.webapp.form.RegisterForm;
import ar.edu.itba.paw.webapp.form.UserCredentialsForm;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class RegisterFormAssembler {

    public User toUser(final RegisterForm form, final Locale locale) {
        final UserCredentialsForm credentials = form.getCredentials();
        final User.Role role = resolveRole(form);
        final String name = role == User.Role.CLIENT
                ? form.getClientProfile().getFirstName() + " " + form.getClientProfile().getLastName()
                : form.getCommerceProfile().getCommercialName();
        return new User(null, credentials.getEmail(), credentials.getPassword(), name, credentials.getPhone(), role,
                false, locale);
    }

    public Client toClientProfile(final RegisterForm form) {
        if (resolveRole(form) != User.Role.CLIENT) {
            return null;
        }
        return new Client(
                (Long) null,
                form.getClientProfile().getFirstName(),
                form.getClientProfile().getLastName(),
                form.getClientProfile().getNotificationsVisibilityPreferences());
    }

    public Commerce toCommerceProfile(final RegisterForm form) {
        if (resolveRole(form) != User.Role.COMMERCE) {
            return null;
        }
        return new Commerce(
                (Long) null,
                form.getCommerceProfile().getCommercialName(),
                form.getCommerceProfile().getCategory(),
                form.getCommerceProfile().getStreet(),
                form.getCommerceProfile().getStreetNumber(),
                form.getCommerceProfile().getCity(),
                form.getCommerceProfile().getProvince(),
                form.getCommerceProfile().getPostalCode(),
                form.getCommerceProfile().getOpeningTime(),
                form.getCommerceProfile().getClosingTime());
    }

    private User.Role resolveRole(final RegisterForm form) {
        return User.Role.valueOf(form.getRole().trim().toUpperCase());
    }
}
