package com.vinay.paymentgateway.payment_service.controller;


import com.vinay.paymentgateway.common_lib.context.MerchantContext;
import com.vinay.paymentgateway.payment_service.dto.request.PaymentInitRequest;
import com.vinay.paymentgateway.payment_service.dto.request.PaymentResponse;
import com.vinay.paymentgateway.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final MerchantContext merchantContext;



    @PostMapping
    public ResponseEntity<PaymentResponse> initiate(@RequestBody @Valid PaymentInitRequest request , @RequestHeader(value="X-Idempotency-Key" , required = false)String idempotencyKey){



        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.initiate(merchantContext.getMerchantId() , request , idempotencyKey));


    }

    @PostMapping("/{paymentId}/capture")
    public ResponseEntity<PaymentResponse> initiate(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.capture(merchantContext.getMerchantId(), paymentId));

    }

}
