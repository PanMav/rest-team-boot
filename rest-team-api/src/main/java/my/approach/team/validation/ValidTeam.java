package my.approach.team.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE, ElementType.PARAMETER})
@Constraint(validatedBy = TeamValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTeam {
    String message() default "Invalid Team details";
    Class<?>[] teams() default {};
    Class<? extends Payload>[] payload() default {};
}
