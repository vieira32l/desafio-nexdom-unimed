package com.nexdom.beneficiarios.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CPFValidator implements ConstraintValidator<CPF, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        String digits = value.replaceAll("\\D", "");

        if (digits.length() != 11 || allDigitsEqual(digits)) {
            return false;
        }

        int firstCheckDigit = calculateCheckDigit(digits.substring(0, 9));
        int secondCheckDigit = calculateCheckDigit(digits.substring(0, 9) + firstCheckDigit);

        return digits.charAt(9) - '0' == firstCheckDigit
                && digits.charAt(10) - '0' == secondCheckDigit;
    }

    private boolean allDigitsEqual(String digits) {
        return digits.chars().distinct().count() == 1;
    }

    private int calculateCheckDigit(String base) {
        int weight = base.length() + 1;
        int sum = 0;
        for (int i = 0; i < base.length(); i++) {
            sum += (base.charAt(i) - '0') * weight--;
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
