package ar.edu.itba.paw.webapp.validation.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.BeanWrapperImpl;

public class LessOrEqualValidator implements ConstraintValidator<LessOrEqual, Object> {

    private String firstFieldName;
    private String secondFieldName;
    private String message;

    @Override
    public void initialize(final LessOrEqual constraintAnnotation) {
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

        Double firstVal;
        Double secondVal;
        try {
            if (firstObj instanceof Number) {
                firstVal = ((Number) firstObj).doubleValue();
            } else {
                firstVal = Double.valueOf(String.valueOf(firstObj));
            }
            if (secondObj instanceof Number) {
                secondVal = ((Number) secondObj).doubleValue();
            } else {
                secondVal = Double.valueOf(String.valueOf(secondObj));
            }
        } catch (final Exception e) {
            return true;
        }

        final boolean valid = firstVal <= secondVal;

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode(firstFieldName)
                    .addConstraintViolation();
        }

        return valid;
    }
}
