package com.vinay.paymentgateway.payment_service.client;

import com.vinay.paymentgateway.common_lib.dto.FindOrCreateCustomerRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name="merchant-client" , path="/internal/customer" )
public interface CustomerServiceClient {
    @PostMapping("/find-or-create")
    UUID findOrCreate(@RequestBody FindOrCreateCustomerRequest request);


}
