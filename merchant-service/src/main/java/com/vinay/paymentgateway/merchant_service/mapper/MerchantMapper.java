package com.vinay.paymentgateway.merchant_service.mapper;


import com.vinay.paymentgateway.merchant_service.dto.request.MerchantSignUpRequest;
import com.vinay.paymentgateway.merchant_service.dto.response.MerchantResponse;
import com.vinay.paymentgateway.merchant_service.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)

public interface MerchantMapper {

    Merchant toEntityFromSignUpRequest(MerchantSignUpRequest request);

    MerchantResponse toResponse(Merchant merchant);
}
