package ar.edu.itba.paw.webapp.validation;

import ar.edu.itba.paw.webapp.form.ProfilePhotoForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ProfilePhotoFormValidator implements Validator {

    private final ImageMultipartValidator imageMultipartValidator;

    @Autowired
    public ProfilePhotoFormValidator(final ImageMultipartValidator imageMultipartValidator) {
        this.imageMultipartValidator = imageMultipartValidator;
    }

    @Override
    public boolean supports(@NonNull final Class<?> clazz) {
        return ProfilePhotoForm.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull final Object target, @NonNull final Errors errors) {
        final ProfilePhotoForm form = (ProfilePhotoForm) target;
        imageMultipartValidator.validate(form.getPhoto(), errors, "photo", LocaleContextHolder.getLocale(), true);
    }
}
