package com.vinay.paymentgateway.merchant_service.dto.request;

import com.vinay.paymentgateway.common_lib.enums.BusinessType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MerchantSignUpRequest(
        @NotNull(message = "Name Should be provided")
        @Size(max=50 , message = "Name sholr not exceed more that 50  charecters")
        String name,

        @Email
        @NotNull(message = "email is Required")
        String email,
        @NotNull(message = "Password is Required")
        @Size(min=8 , message = "password shold bbe 8 char5ecters")
        String password,
        @Size(max=50 , message = "Business sholud be not more than 50 charecters")
        String businessName,

        BusinessType businessType

) {
}
