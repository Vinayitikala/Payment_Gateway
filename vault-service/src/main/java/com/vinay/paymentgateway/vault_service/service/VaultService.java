package com.vinay.paymentgateway.vault_service.service;


import com.vinay.paymentgateway.common_lib.dto.PaymentProcessorResponse;
import com.vinay.paymentgateway.common_lib.entity.Money;
import com.vinay.paymentgateway.vault_service.entity.dto.request.TokenizeRequest;
import com.vinay.paymentgateway.vault_service.entity.dto.response.TokenizeResponse;

import java.util.Map;
import java.util.UUID;

public interface VaultService {
    TokenizeResponse tokenize(TokenizeRequest request, UUID merchantId);

    PaymentProcessorResponse charge(UUID paymentId , String token, Money amount, Map<String, Object> methodDetails);
}
