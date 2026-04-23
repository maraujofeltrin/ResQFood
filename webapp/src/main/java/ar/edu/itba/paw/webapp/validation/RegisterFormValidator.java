package ar.edu.itba.paw.webapp.validation;

import ar.edu.itba.paw.webapp.form.ClientProfileForm;
import ar.edu.itba.paw.webapp.form.CommerceProfileForm;
import ar.edu.itba.paw.webapp.form.RegisterForm;
import ar.edu.itba.paw.webapp.form.UserCredentialsForm;
import ar.edu.itba.paw.models.user.User;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Objects;
import java.util.Optional;

/**
 * Complements Bean Validation: password match, role enum, and nested profile validation.
 */
@Component
public class RegisterFormValidator implements Validator {

    private final javax.validation.Validator beanValidator;

    public RegisterFormValidator(final javax.validation.Validator beanValidator) {
        this.beanValidator = beanValidator;
    }

    @Override
    public boolean supports(@NonNull final Class<?> clazz) {
        return RegisterForm.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull final Object target, @NonNull final Errors errors) {
        final RegisterForm form = (RegisterForm) target;
        final UserCredentialsForm credentials = form.getCredentials();
        if (credentials != null
                && !Objects.equals(credentials.getPassword(), credentials.getRepeatPassword())) {
            errors.rejectValue("credentials.repeatPassword", "user.password.mismatch");
        }

        final Optional<User.Role> parsedRole = parseRole(form.getRole());
        if (parsedRole.isEmpty()) {
            errors.rejectValue("role", "register.validation.role.notNull");
            return;
        }
        final User.Role role = parsedRole.get();
        if (role == User.Role.CLIENT) {
            final ClientProfileForm profile = form.getClientProfile();
            if (profile != null) {
                BeanValidationUtils.rejectViolations("clientProfile", beanValidator.validate(profile), errors);
            }
        } else if (role == User.Role.COMMERCE) {
            final CommerceProfileForm profile = form.getCommerceProfile();
            if (profile != null) {
                BeanValidationUtils.rejectViolations("commerceProfile", beanValidator.validate(profile), errors);
            }
        }
    }

    private static Optional<User.Role> parseRole(final String role) {
        if (role == null || role.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(User.Role.valueOf(role.trim().toUpperCase()));
        } catch (final IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
