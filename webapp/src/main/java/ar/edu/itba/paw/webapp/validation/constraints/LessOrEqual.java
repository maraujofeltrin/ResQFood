package ar.edu.itba.paw.webapp.validation.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = LessOrEqualValidator.class)
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
public @interface LessOrEqual {
    String message() default "{validation.lessorequal}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /** Field that must be less than or equal to {@code second()} */
    String first();

    /** Field to compare against */
    String second();

    /** Whether the comparison should be strictly less than or not */
    boolean strict() default false;


    @Target({TYPE, ANNOTATION_TYPE})
    @Retention(RUNTIME)
    @interface List {
        LessOrEqual[] value();
    }
}
