package com.vinay.paymentgateway.merchant_service.controller;


import com.vinay.paymentgateway.common_lib.context.MerchantContext;
import com.vinay.paymentgateway.merchant_service.dto.request.CreateApiKeyRequest;
import com.vinay.paymentgateway.merchant_service.dto.response.ApiKeyCreateResponse;
import com.vinay.paymentgateway.merchant_service.dto.response.ApiKeyResponse;
import com.vinay.paymentgateway.merchant_service.service.ApikeyServce;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("v1/merchants/api-keys")
@Slf4j
public class ApiKeyContoller {


    public final ApikeyServce apikeyServce;
    public final MerchantContext merchantContext;

    @PostMapping
    public ResponseEntity<ApiKeyCreateResponse> create(@Valid @RequestBody CreateApiKeyRequest request){
        log.info("In the api controller");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(apikeyServce.create(merchantContext.getMerchantId() , request));

    }

    @GetMapping

    public ResponseEntity<List<ApiKeyResponse>> list(){

        return ResponseEntity.ok(apikeyServce.listByMerchant(merchantContext.getMerchantId()));

    }


    @DeleteMapping("/keyId")
    public ResponseEntity<Void> revoke( @PathVariable UUID keyId){
        apikeyServce.revoke(merchantContext.getMerchantId(),keyId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{keyId}/rotate")
    public ResponseEntity<ApiKeyCreateResponse> rotate( @PathVariable UUID keyId){
        return ResponseEntity.ok(apikeyServce.rotateKeyId(merchantContext.getMerchantId(),keyId));
    }
}
