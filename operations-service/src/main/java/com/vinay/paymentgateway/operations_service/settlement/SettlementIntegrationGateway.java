package com.vinay.paymentgateway.operations_service.settlement;

import com.vinay.paymentgateway.common_lib.dto.PaymentSettlementView;
import com.vinay.paymentgateway.common_lib.dto.SettlementBankDetails;
import com.vinay.paymentgateway.operations_service.client.MerchantServiceClient;
import com.vinay.paymentgateway.operations_service.client.PaymentServiceClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SettlementIntegrationGateway {

    private final PaymentServiceClient paymentServiceClient;
    private final MerchantServiceClient merchantServiceClient;
    @CircuitBreaker(name="payment-service")
    @Retry(name="payment-service")
    public List<PaymentSettlementView> findUnsettledCaptured(@RequestBody UUID merchantId){
        return paymentServiceClient.findUnsettledCaptured(merchantId);
    }



    @CircuitBreaker(name = "payment-service")
    @Retry(name = "payment-service")
    public void markSettled(List<UUID> paymentIds) {
        paymentServiceClient.markSettled(paymentIds);
    }

    @CircuitBreaker(name = "merchant-service")
    @Retry(name = "merchant-service")
    public SettlementBankDetails getSettlementBankDetails(UUID merchantId) {
        return merchantServiceClient.getSettlementBankDetails(merchantId);
    }

}
