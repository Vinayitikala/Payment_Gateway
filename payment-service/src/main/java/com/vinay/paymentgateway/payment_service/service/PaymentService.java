package com.vinay.paymentgateway.payment_service.service;


import com.vinay.paymentgateway.payment_service.dto.request.PaymentInitRequest;
import com.vinay.paymentgateway.payment_service.dto.request.PaymentResponse;

import java.util.UUID;

public interface PaymentService {

    PaymentResponse initiate(UUID merchantId , PaymentInitRequest request , String idempotency);

    PaymentResponse capture(UUID merchantId, UUID paymentId);

    void resolveAuthorization(UUID paymentId, boolean approove, String bankRef, String errrCode, String errorDescription);
}

