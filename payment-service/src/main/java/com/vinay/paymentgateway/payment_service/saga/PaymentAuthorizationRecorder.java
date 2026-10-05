package com.vinay.paymentgateway.payment_service.saga;

import com.vinay.paymentgateway.common_lib.enums.EventAggregateType;
import com.vinay.paymentgateway.common_lib.enums.OrderStatus;
import com.vinay.paymentgateway.common_lib.enums.PaymentEvent;
import com.vinay.paymentgateway.common_lib.enums.PaymentStatus;
import com.vinay.paymentgateway.common_lib.exception.BusinessVoilationException;
import com.vinay.paymentgateway.common_lib.exception.ResourceNotFoundException;
import com.vinay.paymentgateway.payment_service.Entity.OrderRecord;
import com.vinay.paymentgateway.payment_service.Entity.Payment;
import com.vinay.paymentgateway.payment_service.dto.request.PaymentInitRequest;
import com.vinay.paymentgateway.payment_service.dto.request.PaymentResponse;
import com.vinay.paymentgateway.payment_service.gateway.dto.PaymentResult;
import com.vinay.paymentgateway.payment_service.mapper.PaymentMapper;
import com.vinay.paymentgateway.payment_service.outbox.OutboxEventPublisher;
import com.vinay.paymentgateway.payment_service.repository.OrderRepository;
import com.vinay.paymentgateway.payment_service.repository.PaymentRepository;
import com.vinay.paymentgateway.payment_service.repository.PaymentTransitionLogRepository;
import com.vinay.paymentgateway.payment_service.statemachine.PaymentTransitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor

public class PaymentAuthorizationRecorder {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentTransitionLogRepository paymentTransitionLogRepository;
    private final PaymentTransitionService paymentTransitionService;
    private final OutboxEventPublisher eventPublisher;
    private final PaymentMapper paymentMapper;
    @Transactional
    public Payment recordPayment(UUID merchantId , PaymentInitRequest request, String idempeotency){
        OrderRecord order=orderRepository.findByIdAndMerchantId(request.orderId() , merchantId)
                .orElseThrow(()->new ResourceNotFoundException("order" , request.orderId()));



//        OrderRecord order=orderRepository.findByIdAndMerchantIdForUpdate(request.orderId() , merchantId)
//                .orElseThrow(()->new ResourceNotFoundException("order" , request.orderId()));


        if(order.getOrderStatus() != OrderStatus.CREATED && order.getOrderStatus()!= OrderStatus.ATTEMPTED){
            throw new BusinessVoilationException("Order_Not PAyABLE" , "Order cannot accept payment in status "+order.getOrderStatus());
        }

        order.setOrderStatus(OrderStatus.ATTEMPTED);
        order.setAttempts(order.getAttempts()+1);

        Payment payment =Payment.builder()
                .order(order)
                .merchantId(merchantId)
                .amount(order.getAmount())
                .status(PaymentStatus.CREATED)
                .method(request.method())
                .idempotencyKey(idempeotency!=null ?idempeotency:UUID.randomUUID().toString())
                .methodDetailes(request.methodDetails())
                .build();
        payment=paymentRepository.save(payment);
        paymentTransitionService.apply(payment , PaymentEvent.AUTHORIZE_ATTEMPT);


        return payment;
    }

    @Transactional(readOnly = true)
    public Optional<PaymentResponse> findExistingAttempt(UUID merchantId, String idempotencyKey) {
        return paymentRepository.findByMerchantIdAndIdempotencyKey(merchantId, idempotencyKey)
                .map(paymentMapper::toResponse);
    }

    @Transactional
    public PaymentResponse compensationAuthorizationFailure(UUID paymentId,
                                                            String  errorCode , String errorDescription){
        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
        payment.setErrorCode(errorCode);
        payment.setErrorDescription(errorDescription);
        payment = paymentRepository.save(payment);

        publishStatusEvent(payment, "PAYMENT_AUTHORIZATION_COMPENSATED");
        return paymentMapper.toResponse(payment);
    }
    @Transactional
    public PaymentResponse applyGatewayResult(UUID paymentId , PaymentResult result  ){
        log.info("Applying Gateway result for paymentId: {}", paymentId);
        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        switch (result) {
            case PaymentResult.Pending pending -> payment.setProcessorReference(pending.registrationRef());
            case PaymentResult.Failure failure -> {
                paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorDescription());
            }
            case PaymentResult.Success success ->
                    log.warn("Invalid state: initiate() gateway call returned Success directly, paymentId={}", paymentId);
        }

        payment = paymentRepository.save(payment);
        publishStatusEvent(payment, "PAYMENT_CREATED");
        log.info("Successfully applied Gateway result for paymentId: {}", paymentId);
        return paymentMapper.toResponse(payment);
    }

    private void publishStatusEvent(Payment payment, String eventType) {
        eventPublisher.publish(EventAggregateType.PAYMENT, payment.getId(), eventType,
                Map.of("orderId", payment.getOrder().getId().toString(),
                        "paymentId", payment.getId().toString(),
                        "merchantId", payment.getMerchantId().toString(),
                        "paymentStatus", payment.getStatus().name(),
                        "amountUnits", payment.getAmount().getAmountUnits(),
                        "amountCurrency", payment.getAmount().getCurrency(),
                        "paymentMethod", payment.getMethod()
                ));
    }
}
