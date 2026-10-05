package com.vinay.paymentgateway.payment_service.processor.strategy;


import com.vinay.paymentgateway.common_lib.dto.PaymentProcessorRequest;
import com.vinay.paymentgateway.common_lib.dto.PaymentProcessorResponse;
import com.vinay.paymentgateway.common_lib.util.RandomizerUtil;
import com.vinay.paymentgateway.payment_service.processor.PaymentProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UpiPaymentProcessor implements PaymentProcessor {


    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {

        final String VPA_CODE_FAIL="fail@okaxis";


        String bankCode = request.methodDetails() !=null?
                request.methodDetails().get("vpa").toString() :null;

        if(VPA_CODE_FAIL.equals(bankCode)){
            return new PaymentProcessorResponse.Failure("UPI_REJECTED" ,
                    "BANK REJECTED the TRANSACTION REGISTRATION");
        }

        String processorRef = "UPI_PROCESSOR"+ RandomizerUtil.randomBase64(16);


        String bankRef="BANK_REF"+processorRef;


        return new PaymentProcessorResponse.Pending(processorRef );
    }
}
