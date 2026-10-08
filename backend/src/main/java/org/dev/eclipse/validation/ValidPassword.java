package org.dev.eclipse.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordConstraintValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    String message() default "Şifre en az 6 karakter olmalı, en az 1 büyük harf, 1 sayı ve 1 özel karakter içermelidir.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
