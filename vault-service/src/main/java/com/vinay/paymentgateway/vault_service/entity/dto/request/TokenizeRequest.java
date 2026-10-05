package com.vinay.paymentgateway.vault_service.entity.dto.request;

import com.vinay.paymentgateway.vault_service.validation.ExpiryYear;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.LuhnCheck;

import java.util.UUID;

public record TokenizeRequest(
        @NotNull(message = "pan isRequired")
        @LuhnCheck(message = "Invalid card number")
        @Pattern(regexp = "^[0-9]{13,19}$" , message = "pan length is invalid")
        String pan,
        @NotNull(message = "CVV is Required")
        @Pattern(regexp = "^[0-9]{3,4}$" , message = " CVV length is inavlid")
        String cvv,

        @NotNull(message = "Expity month is Required")
        @Min(value = 1,message = "expiry must be between i to 12")
        @Max(value = 12,message = "expiry must be between i to 12")


        Integer expiryMonth,

        @NotNull(message = "Expity year is Required")
        @ExpiryYear
        Integer expiryYear,

        UUID customerId,

        @Size(min=3 , message = "card holder name shouuld be min 2 characters")
        String cardHolderName
) {


}
