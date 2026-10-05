package com.vinay.paymentgateway.vault_service.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Year;

public class ExpiryYearVaidator implements ConstraintValidator<ExpiryYear, Integer > {

    @Override
    public boolean isValid(Integer InputYear, ConstraintValidatorContext context) {

        if(InputYear==null){
            return false;
        }
        int currentYear=Year.now().getValue();

        return currentYear<=InputYear;
    }
}
