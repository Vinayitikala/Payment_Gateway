package com.vinay.paymentgateway.merchant_service.service;


import com.vinay.paymentgateway.merchant_service.dto.request.CreateApiKeyRequest;
import com.vinay.paymentgateway.merchant_service.dto.response.ApiKeyCreateResponse;
import com.vinay.paymentgateway.merchant_service.dto.response.ApiKeyResponse;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

//@Service

public interface ApikeyServce {
    ApiKeyCreateResponse create(UUID merchantId, @Valid CreateApiKeyRequest request);

    List<ApiKeyResponse> listByMerchant(UUID merchantId);

    void revoke(UUID merchantId, UUID keyId);

    ApiKeyCreateResponse rotateKeyId(UUID merchantId, UUID keyId);
}
