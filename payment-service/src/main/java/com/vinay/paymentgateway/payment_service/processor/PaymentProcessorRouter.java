package com.vinay.paymentgateway.payment_service.processor;


import com.vinay.paymentgateway.common_lib.dto.PaymentProcessorResponse;
import com.vinay.paymentgateway.common_lib.enums.PaymentMethod;
import com.vinay.paymentgateway.common_lib.dto.PaymentProcessorRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
@Component
@RequiredArgsConstructor
public class PaymentProcessorRouter {
    private final Map<PaymentMethod, PaymentProcessor> paymentProcessors;


    public PaymentProcessorResponse charge(PaymentProcessorRequest request){

        PaymentProcessor processor=paymentProcessors.get(request.method());

        if(processor==null){
            throw new IllegalArgumentException(("no payment processor register for the megthod  : "+request.method()));

        }

        return processor.charge(request);




    }
}
