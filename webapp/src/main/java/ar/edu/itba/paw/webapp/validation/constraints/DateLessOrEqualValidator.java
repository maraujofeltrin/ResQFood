package ar.edu.itba.paw.webapp.validation.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;
import java.time.LocalDate;

public class DateLessOrEqualValidator implements ConstraintValidator<DateLessOrEqual, Object> {

    private String firstFieldName;
    private String secondFieldName;
    private String message;

    @Override
    public void initialize(final DateLessOrEqual constraintAnnotation) {
        firstFieldName = constraintAnnotation.first();
        secondFieldName = constraintAnnotation.second();
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(final Object value, final ConstraintValidatorContext context) {
        final Object firstObj = new BeanWrapperImpl(value).getPropertyValue(firstFieldName);
        final Object secondObj = new BeanWrapperImpl(value).getPropertyValue(secondFieldName);

        if (firstObj == null || secondObj == null) {
            return true;
        }

        try {
            final LocalDate firstDate = LocalDate.parse(String.valueOf(firstObj).trim());
            final LocalDate secondDate = LocalDate.parse(String.valueOf(secondObj).trim());
            final boolean valid = !firstDate.isAfter(secondDate);
            if (!valid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message)
                        .addPropertyNode(firstFieldName)
                        .addConstraintViolation();
            }
            return valid;
        } catch (final Exception e) {
            return true;
        }
    }
}
