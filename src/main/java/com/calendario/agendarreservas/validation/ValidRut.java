package com.calendario.agendarreservas.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = RutConstraintValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRut {
    String message() default "RUT chileno inválido. Formato esperado: 12345678-9 o 12345678-K";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
