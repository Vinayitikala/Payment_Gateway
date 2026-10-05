package com.vinay.paymentgateway.operations_service.client;

import com.vinay.paymentgateway.common_lib.dto.PaymentSettlementView;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name ="payment-service" ,path= "/internal/payments" )
public interface PaymentServiceClient {

    @GetMapping("/unsettled-capture")
    List<PaymentSettlementView> findUnsettledCaptured(@RequestBody UUID merchantId);

    @PostMapping("/mark-settled")
    void markSettled(@RequestBody List<UUID> paymentIds);

}
