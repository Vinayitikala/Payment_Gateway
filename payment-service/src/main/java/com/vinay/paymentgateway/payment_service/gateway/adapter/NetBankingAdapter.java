package com.vinay.paymentgateway.payment_service.gateway.adapter;

import com.vinay.paymentgateway.common_lib.dto.PaymentProcessorResponse;
import com.vinay.paymentgateway.common_lib.enums.PaymentMethod;
import com.vinay.paymentgateway.common_lib.dto.PaymentProcessorRequest;
import com.vinay.paymentgateway.payment_service.gateway.PaymentAdapter;
import com.vinay.paymentgateway.payment_service.gateway.dto.PaymentRequest;
import com.vinay.paymentgateway.payment_service.gateway.dto.PaymentResult;
import com.vinay.paymentgateway.payment_service.processor.PaymentProcessorRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component("NETBANKING")
@Slf4j
@RequiredArgsConstructor

public class NetBankingAdapter implements PaymentAdapter {

    private final PaymentProcessorRouter paymentProcessorRouter;



    @Override
    public PaymentResult initiate(PaymentRequest request){

        log.info("Intiate Payment with NetBanking, paymentId : {}",request.paymentId());
        try {
            PaymentProcessorRequest paymentProcessorRequest = PaymentProcessorRequest.nonCard(
                    request.paymentId(),
                    PaymentMethod.NETBANKING,
                    request.amount(),
                    request.methodDetails()
            );


            PaymentProcessorResponse paymentProcessorResponse = paymentProcessorRouter.charge(paymentProcessorRequest);


            return switch (paymentProcessorResponse) {
                case PaymentProcessorResponse.Failure failure ->
                        new PaymentResult.Failure(failure.errorCode(), failure.errorDescription());
                case PaymentProcessorResponse.Pending pending ->
                        new PaymentResult.Pending(pending.processorReference());
                case PaymentProcessorResponse.Success success -> new PaymentResult.Success(success.bankReference());


            };
        } catch (Exception e){
            log.warn("Net Banking failed paymentId {}", request.paymentId());
            return new PaymentResult.Failure("Netbanking faied " , e.getMessage());
        }
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return new PaymentResult.Success("NBK_REF");
    }
}
