package com.vinay.paymentgateway.merchant_service.dto.request;


import com.vinay.paymentgateway.common_lib.enums.Environment;

public record CreateApiKeyRequest(
        Environment environment
) {
}
