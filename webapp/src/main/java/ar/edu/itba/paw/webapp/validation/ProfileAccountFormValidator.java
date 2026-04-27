package ar.edu.itba.paw.webapp.validation;

import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.webapp.form.ProfileAccountForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Locale;

@Component
public class ProfileAccountFormValidator {

    private final ImageMultipartValidator imageMultipartValidator;
    private final MessageSource messageSource;

    @Autowired
    public ProfileAccountFormValidator(final ImageMultipartValidator imageMultipartValidator,
            final MessageSource messageSource) {
        this.imageMultipartValidator = imageMultipartValidator;
        this.messageSource = messageSource;
    }

    public void validate(final ProfileAccountForm form, final Errors errors, final User.Role role) {
        final Locale locale = LocaleContextHolder.getLocale();
        if (role == User.Role.COMMERCE) {
            validateCommerceFields(form, errors, locale);
            if (form.getPhoto() != null && !form.getPhoto().isEmpty()) {
                imageMultipartValidator.validate(form.getPhoto(), errors, "photo", locale, false);
            }
        } else {
            imageMultipartValidator.validate(form.getPhoto(), errors, "photo", locale, true);
        }
    }

    private void validateCommerceFields(final ProfileAccountForm form, final Errors errors, final Locale locale) {
        if (form.getCategory() == null || form.getCategory().isBlank()) {
            errors.rejectValue("category", "error.category.required",
                    messageSource.getMessage("profile.commerce.validation.category.required", null, locale));
        } else {
            try {
                Commerce.Category.valueOf(form.getCategory().trim());
            } catch (final IllegalArgumentException ex) {
                errors.rejectValue("category", "error.category.invalid",
                        messageSource.getMessage("profile.commerce.validation.category.invalid", null, locale));
            }
        }
        if (form.getStreet() == null || form.getStreet().isBlank()) {
            errors.rejectValue("street", "error.street.required",
                    messageSource.getMessage("profile.commerce.validation.street.required", null, locale));
        }
        if (form.getCity() == null || form.getCity().isBlank()) {
            errors.rejectValue("city", "error.city.required",
                    messageSource.getMessage("profile.commerce.validation.city.required", null, locale));
        }
        if (form.getOpeningTime() == null || form.getOpeningTime().isBlank()) {
            errors.rejectValue("openingTime", "error.openingTime.required",
                    messageSource.getMessage("profile.commerce.validation.hours.required", null, locale));
        }
        if (form.getClosingTime() == null || form.getClosingTime().isBlank()) {
            errors.rejectValue("closingTime", "error.closingTime.required",
                    messageSource.getMessage("profile.commerce.validation.hours.required", null, locale));
        }
        if (form.getStreetNumber() != null && !form.getStreetNumber().isBlank()) {
            try {
                Integer.parseInt(form.getStreetNumber().trim());
            } catch (final NumberFormatException ex) {
                errors.rejectValue("streetNumber", "error.streetNumber.invalid",
                        messageSource.getMessage("profile.commerce.validation.streetNumber.invalid", null, locale));
            }
        }
    }
}
