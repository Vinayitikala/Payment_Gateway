package com.vinay.paymentgateway.payment_service.mapper;


import com.vinay.paymentgateway.payment_service.Entity.Payment;
import com.vinay.paymentgateway.payment_service.dto.request.PaymentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {
    @Mapping(target = "orderId" , source="order.id")
    PaymentResponse toResponse(Payment payment);


    @Mapping(target = "orderId" , source="order.id")
    List<PaymentResponse> toResponsibleList(List<Payment>  paymentList);
}
