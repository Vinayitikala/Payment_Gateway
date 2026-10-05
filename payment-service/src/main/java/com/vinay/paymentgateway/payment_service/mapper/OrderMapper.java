package com.vinay.paymentgateway.payment_service.mapper;


import com.vinay.paymentgateway.payment_service.Entity.OrderRecord;
import com.vinay.paymentgateway.payment_service.dto.response.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {


    OrderResponse toResponse(OrderRecord orderRecord);

}
