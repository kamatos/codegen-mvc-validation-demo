package com.kamatos.codegenvalidationdemo.validation.constraint;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class EmailConstraintValidator implements ConstraintValidator<ValidEmail, String> {
    public static final Pattern NAME_LASTNAME_PATTERN =
            Pattern.compile("^[A-Za-z]+\\.[A-Za-z]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        // If you want @NotBlank to handle null/blank, return true here and rely on @NotBlank separately.
        if (value == null || value.isBlank()) return true;

        boolean ok = true;
        ctx.disableDefaultConstraintViolation();

        // Rule 1: must match name.lastname@domain
        if (!NAME_LASTNAME_PATTERN.matcher(value).matches()) {
            ctx.buildConstraintViolationWithTemplate("email.format.name_lastname_required")
                    .addConstraintViolation();
            ok = false;
        }

        // Rule 2: domain must NOT be test.com
        String domain = extractDomain(value);
        if ("test.com".equalsIgnoreCase(domain)) {
            ctx.buildConstraintViolationWithTemplate("email.domain.blocked")
                    .addConstraintViolation();
            ok = false;
        }

        return ok;
    }

    private static String extractDomain(String email) {
        int at = email.lastIndexOf('@');
        return at >= 0 ? email.substring(at + 1) : "";
    }
}
