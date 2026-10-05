package com.vinay.paymentgateway.merchant_service.mapper;


import com.vinay.paymentgateway.merchant_service.dto.response.ApiKeyCreateResponse;
import com.vinay.paymentgateway.merchant_service.dto.response.ApiKeyResponse;
import com.vinay.paymentgateway.merchant_service.entity.ApiKey;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)

public interface ApiKeyMapper {

    ApiKeyCreateResponse toCreateResponse(ApiKey apikey);

    List<ApiKeyResponse> toResponse(List<ApiKey> apiKeysList);
}
