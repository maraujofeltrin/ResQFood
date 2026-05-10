package ar.edu.itba.paw.webapp.validation.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = PastOrPresentDateValidator.class)
@Target({FIELD})
@Retention(RUNTIME)
public @interface PastOrPresentDate {
    String message() default "{validation.date.pastorpresent}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
