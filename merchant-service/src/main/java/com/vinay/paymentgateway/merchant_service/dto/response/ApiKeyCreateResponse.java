package com.vinay.paymentgateway.merchant_service.dto.response;


import com.vinay.paymentgateway.common_lib.enums.Environment;

import java.util.UUID;

public record ApiKeyCreateResponse(
        UUID id,
        String KeyId,
        String KeySecret,
        Environment environment
) {



}
