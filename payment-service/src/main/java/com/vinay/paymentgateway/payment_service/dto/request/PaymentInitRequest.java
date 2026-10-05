package com.vinay.paymentgateway.payment_service.dto.request;

import com.vinay.paymentgateway.common_lib.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record PaymentInitRequest(
        @NotNull(message = "order id is required")

        UUID orderId,
        @NotNull(message = "Payment method is Required")
        PaymentMethod method,

        Map<String , Object> methodDetails
) {
}
