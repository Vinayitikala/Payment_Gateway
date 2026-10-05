package com.vinay.paymentgateway.payment_service.service.impl;


import com.vinay.paymentgateway.common_lib.dto.FindOrCreateCustomerRequest;
import com.vinay.paymentgateway.common_lib.enums.EventAggregateType;
import com.vinay.paymentgateway.common_lib.enums.OrderStatus;
import com.vinay.paymentgateway.common_lib.exception.BusinessVoilationException;
import com.vinay.paymentgateway.common_lib.exception.DuplicateResouceException;
import com.vinay.paymentgateway.common_lib.exception.ResourceNotFoundException;
import com.vinay.paymentgateway.payment_service.Entity.OrderRecord;
import com.vinay.paymentgateway.payment_service.Entity.Payment;
import com.vinay.paymentgateway.payment_service.client.CustomerServiceClient;
import com.vinay.paymentgateway.payment_service.dto.request.CreateOrderRequest;
import com.vinay.paymentgateway.payment_service.dto.request.PaymentResponse;
import com.vinay.paymentgateway.payment_service.dto.response.OrderResponse;
import com.vinay.paymentgateway.payment_service.mapper.OrderMapper;
import com.vinay.paymentgateway.payment_service.mapper.PaymentMapper;
import com.vinay.paymentgateway.payment_service.outbox.OutboxEventPublisher;
import com.vinay.paymentgateway.payment_service.repository.OrderRepository;
import com.vinay.paymentgateway.payment_service.repository.PaymentRepository;
import com.vinay.paymentgateway.payment_service.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
//import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final OutboxEventPublisher eventPublisher;
    private final CustomerServiceClient customerServiceClient;

    private final OrderMapper orderMapper;
    @Value("${payment.order.default-order-expiry-minutes : 30}")
    private int defaultOrderExpiryMinutes;
    @Override
    @Transactional
    @CircuitBreaker(name="merchant-service")
    @Retry(name="merchant-service")
    public OrderResponse create(UUID merchantId, CreateOrderRequest request) {
        if(request.receipt()!=null &&  orderRepository.existsByMerchantIdAndReceipt(merchantId,request.receipt())){
            throw new DuplicateResouceException("ORDER_RECEIPT_DUPLICATE" , "Ordeer with recepi alredy exists "+request.receipt());
        }
        UUID customerId=null;
        if(request.customer() !=null){
            customerId=customerServiceClient.findOrCreate(new FindOrCreateCustomerRequest(merchantId ,
                    request.customer().email(),
                    request.customer().name(),
                    request.customer().phone()));
        }

        OrderRecord order=OrderRecord.builder()
                .receipt(request.receipt())
                .amount(request.amount())
                .notes(request.notes())
                .merchantId(merchantId)
                .customerId(customerId)
                .orderStatus(OrderStatus.CREATED)
                .expiresAt(request.expiresAt()!=null ? request.expiresAt():
                        LocalDateTime.now().plusMinutes(defaultOrderExpiryMinutes))
                .build();
        order=orderRepository.save(order);




// TODO PUBLISH KAFKA EVENT ABOUNT order

        eventPublisher.publish(EventAggregateType.ORDER , order.getId() , "ORDER_CREATED",
                Map.of(
                        "orderId",order.getId(),
                        "merchantId" , merchantId.toString(),
                        "OrderStatus" , order.getOrderStatus().name(),
                        "amountUnits" , order.getAmount().getAmountUnits(),
                        "amountCurrency" , order.getAmount().getCurrency()
                        )
                );
        return orderMapper.toResponse(order);



    }

    @Override
    public OrderResponse getById(UUID merchantId, UUID orderId) {

        OrderRecord order= orderRepository.findByIdAndMerchantId(merchantId,orderId)
                .orElseThrow(()->new ResourceNotFoundException("order" , orderId));

    return orderMapper.toResponse(order);

    }

    @Override
    @Transactional
    public OrderResponse cancel(UUID merchantId, UUID orderId) {
        OrderRecord order= orderRepository.findByIdAndMerchantId(merchantId,orderId)
                .orElseThrow(()->new ResourceNotFoundException("order" , orderId));

        if(order.getOrderStatus() == OrderStatus.PAID || order.getOrderStatus() == OrderStatus.PAID){
                throw new BusinessVoilationException("Order cannort cancel " , "cannot cancel the order with status  "+order.getOrderStatus().name());
        }

        order.setOrderStatus(OrderStatus.CANCELLED);

        order = orderRepository.save(order);
        eventPublisher.publish(EventAggregateType.ORDER , order.getId() , "ORDER_CANCELLED",
                Map.of(
                        "orderId",order.getId(),
                        "merchantId" , merchantId.toString(),
                        "OrderStatus" , order.getOrderStatus().name(),
                        "amountUnits" , order.getAmount().getAmountUnits(),
                        "amountCurrency" , order.getAmount().getCurrency()
                )
        );

        return orderMapper.toResponse(order);


    }

    @Override
    public List<PaymentResponse> listPayments(UUID merchantId, UUID orderId) {
        orderRepository.findByIdAndMerchantId(merchantId,orderId)
                .orElseThrow(()->new ResourceNotFoundException("order" , orderId));


        List<Payment> paymentList=paymentRepository.findByOrder_Id(orderId);

//        return paymentList.stream().map(
//                payment -> paymentMapper.toResponse(payment)
//        ).collect(Collectors.toList());

        return paymentMapper.toResponsibleList(paymentList);


//        return List.of();
    }
}
