package ar.edu.itba.paw.webapp.validation;

import org.springframework.validation.Errors;

import javax.validation.ConstraintViolation;
import java.util.Set;

/**
 * Maps Bean Validation output onto Spring {@link Errors} (nested path support).
 */
public final class BeanValidationUtils {

    private BeanValidationUtils() {
    }

    public static <T> void rejectViolations(final String fieldPrefix,
            final Set<ConstraintViolation<T>> violations, final Errors errors) {
        for (final ConstraintViolation<T> violation : violations) {
            final String property = violation.getPropertyPath() == null ? "" : violation.getPropertyPath().toString();
            final String field = property.isEmpty() ? fieldPrefix : fieldPrefix + "." + property;
            errors.rejectValue(field, "invalid", violation.getMessage());
        }
    }
}
