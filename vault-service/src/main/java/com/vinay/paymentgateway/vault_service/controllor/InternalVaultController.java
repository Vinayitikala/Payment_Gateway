package com.vinay.paymentgateway.vault_service.controllor;

import com.vinay.paymentgateway.common_lib.dto.PaymentProcessorResponse;
import com.vinay.paymentgateway.common_lib.dto.VaultChargeRequest;
import com.vinay.paymentgateway.vault_service.service.VaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/vault")
public class InternalVaultController {

    private final VaultService vaultService;

    @PostMapping("/charge")
    public PaymentProcessorResponse charge(@RequestBody VaultChargeRequest request) {
        return vaultService.charge(request.paymentId(), request.token(), request.amount(), request.methodDetails());
    }
}
