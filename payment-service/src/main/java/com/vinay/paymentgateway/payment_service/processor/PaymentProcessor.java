package com.vinay.paymentgateway.payment_service.processor;


import com.vinay.paymentgateway.common_lib.dto.PaymentProcessorRequest;
import com.vinay.paymentgateway.common_lib.dto.PaymentProcessorResponse;

public interface PaymentProcessor {

    PaymentProcessorResponse charge(PaymentProcessorRequest request);
}
