package com.vinay.paymentgateway.vault_service.controllor;

import com.vinay.paymentgateway.common_lib.context.MerchantContext;
import com.vinay.paymentgateway.vault_service.entity.dto.request.TokenizeRequest;
import com.vinay.paymentgateway.vault_service.entity.dto.response.TokenizeResponse;
import com.vinay.paymentgateway.vault_service.service.VaultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/vault")
public class VaultController {

    private final VaultService vaultService;
    private final MerchantContext merchantContext;


    @PostMapping("/tokenize")
    public ResponseEntity<TokenizeResponse> tokenize(@RequestBody @Valid TokenizeRequest request){

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vaultService.tokenize(request , merchantContext.getMerchantId()));

    }





}
