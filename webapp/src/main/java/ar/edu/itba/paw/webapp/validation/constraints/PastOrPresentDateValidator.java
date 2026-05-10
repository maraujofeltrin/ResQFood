package ar.edu.itba.paw.webapp.validation.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class PastOrPresentDateValidator implements ConstraintValidator<PastOrPresentDate, Object> {

    @Override
    public void initialize(final PastOrPresentDate constraintAnnotation) {
    }

    @Override
    public boolean isValid(final Object value, final ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        final String str = String.valueOf(value).trim();
        if (str.isEmpty()) {
            return true;
        }
        try {
            final LocalDate date = LocalDate.parse(str);
            final LocalDate today = LocalDate.now();
            return !date.isAfter(today);
        } catch (final Exception e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{validation.date.invalid}")
                .addConstraintViolation();
            return false;
        }
    }
}
