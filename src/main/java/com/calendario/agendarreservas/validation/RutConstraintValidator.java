package com.calendario.agendarreservas.validation;

import com.calendario.agendarreservas.util.RutUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RutConstraintValidator implements ConstraintValidator<ValidRut, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return true;
        return RutUtil.esValido(value);
    }
}
