package com.vinay.paymentgateway.merchant_service.mapper;


import com.vinay.paymentgateway.merchant_service.dto.response.WebhookConfigResponse;
import com.vinay.paymentgateway.merchant_service.entity.MerchantWebhookConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
//import org.springframework.web.bind.annotation.Mapping;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)

public interface WebhookConfigMapper {
    @Mapping(target = "webhookSecret", source = "rawSecret")
    WebhookConfigResponse toResponse(MerchantWebhookConfig merchantWebhookConfig, String rawSecret);

}
