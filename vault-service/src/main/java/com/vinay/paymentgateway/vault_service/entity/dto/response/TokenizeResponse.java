package com.vinay.paymentgateway.vault_service.entity.dto.response;


import com.vinay.paymentgateway.common_lib.enums.CardBrand;

public record TokenizeResponse(
        String token,
        String lastFour,
        CardBrand brand,
        Integer expiryMonth,
        Integer expiryYear
) {
}
