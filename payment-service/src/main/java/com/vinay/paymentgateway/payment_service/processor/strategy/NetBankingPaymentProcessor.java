package com.vinay.paymentgateway.payment_service.processor.strategy;


import com.vinay.paymentgateway.common_lib.dto.PaymentProcessorResponse;
import com.vinay.paymentgateway.common_lib.util.RandomizerUtil;
import com.vinay.paymentgateway.common_lib.dto.PaymentProcessorRequest;
import com.vinay.paymentgateway.payment_service.processor.PaymentProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NetBankingPaymentProcessor implements PaymentProcessor {


    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {

        final String BANK_CODE_FAIL="BANK_CODE_FAIL";


        String bankCode = request.methodDetails() !=null?
                request.methodDetails().get("bank").toString() :null;

        if(BANK_CODE_FAIL.equals(bankCode)){
            return new PaymentProcessorResponse.Failure("BANK_REJECTED" ,
                    "BANK REJECTED the TRANSACTION REGISTRATION");
        }

        String processorRef = "NBK_PROCESSOR"+ RandomizerUtil.randomBase64(16);


        String redirectRef="http://REDIRECT_BANK.com/"+processorRef;


        return new PaymentProcessorResponse.Pending(processorRef );
    }
}
