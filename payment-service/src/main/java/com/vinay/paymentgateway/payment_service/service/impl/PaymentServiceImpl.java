package com.vinay.paymentgateway.payment_service.service.impl;

//import com.vinay.PaymentGateway.PaymentGatewayApplication;


import com.vinay.paymentgateway.common_lib.enums.EventAggregateType;
import com.vinay.paymentgateway.common_lib.enums.OrderStatus;
import com.vinay.paymentgateway.common_lib.enums.PaymentEvent;
import com.vinay.paymentgateway.common_lib.enums.PaymentStatus;
import com.vinay.paymentgateway.common_lib.exception.ResourceNotFoundException;
import com.vinay.paymentgateway.payment_service.Entity.OrderRecord;
import com.vinay.paymentgateway.payment_service.Entity.Payment;
import com.vinay.paymentgateway.payment_service.dto.request.PaymentInitRequest;
import com.vinay.paymentgateway.payment_service.dto.request.PaymentResponse;
import com.vinay.paymentgateway.payment_service.gateway.PaymentGatewayRouter;
import com.vinay.paymentgateway.payment_service.gateway.dto.PaymentRequest;
import com.vinay.paymentgateway.payment_service.gateway.dto.PaymentResult;
import com.vinay.paymentgateway.payment_service.mapper.PaymentMapper;
import com.vinay.paymentgateway.payment_service.outbox.OutboxEventPublisher;
import com.vinay.paymentgateway.payment_service.repository.OrderRepository;
import com.vinay.paymentgateway.payment_service.repository.PaymentRepository;
import com.vinay.paymentgateway.payment_service.saga.PaymentAuthorizationRecorder;
import com.vinay.paymentgateway.payment_service.service.PaymentService;
import com.vinay.paymentgateway.payment_service.statemachine.PaymentTransitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {


    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentTransitionService paymentTransitionService;

    private final PaymentMapper paymentMapper;

    private final PaymentGatewayRouter paymentGatewayRouter;
    private final OutboxEventPublisher eventPublisher;
    private final PaymentAuthorizationRecorder paymentAuthorizationRecorder;

    @Override
    @Transactional()
    public PaymentResponse initiate(UUID merchantId, PaymentInitRequest request,String idempeotency) {

        if(idempeotency!=null){
            var existing=paymentAuthorizationRecorder.findExistingAttempt(merchantId , idempeotency);
            if(existing.isPresent()){
                log.info("Idempotency replay for paymentId :{}" , existing.get().id());
                return existing.get();

            }
        }

        Payment payment=paymentAuthorizationRecorder.recordPayment(merchantId , request ,idempeotency);


        PaymentRequest paymentRequest=new PaymentRequest(
                payment.getId(),
                request.orderId(),
                merchantId,
                payment.getAmount(),
                request.method(),
                request.methodDetails()
        );

        PaymentResult result;
        try{

            result=paymentGatewayRouter.initiate(paymentRequest);

        }catch (Exception e){
                return paymentAuthorizationRecorder.compensationAuthorizationFailure(payment.getId() , "PAYMENT_GATEWAY_ROUTERUNREACHBLE" , e.getMessage());
        }



        return paymentAuthorizationRecorder.applyGatewayResult(payment.getId() , result);






    }

    @Override
    @Transactional()

    public PaymentResponse capture(UUID merchantId, UUID paymentId) {

//        Payment payment=paymentRepository.findByIdAndMerchantId(paymentId , merchantId)
//                .orElseThrow(()->new ResourceNotFoundException("Payment" , paymentId));

        Payment payment=paymentRepository.findByIdAndMerchantIdForUpadate(paymentId , merchantId)
                .orElseThrow(()->new ResourceNotFoundException("Payment" , paymentId));

//        payment.setStatus(PaymentStatus.CAPTURING); //
        paymentTransitionService.apply(payment ,PaymentEvent.CAPTURE_REQUEST );
        PaymentResult paymentResult=paymentGatewayRouter.capture(payment.getMethod() , paymentId);

        if(paymentResult instanceof PaymentResult.Success success){

            paymentTransitionService.apply(payment , PaymentEvent.CAPTURE_SUCCESS);
            payment.setCapturedAt(LocalDateTime.now());
            log.info("payment captured paymentId :{} " , paymentId);

        }else if(paymentResult instanceof PaymentResult.Failure failure){
            paymentTransitionService.apply(payment,PaymentEvent.CAPTURE_FAIL);
            payment.setErrorCode(failure.errorCode());
            payment.setErrorDescription(failure.errorDescription());

            log.warn("payemnt capture failed paymentId : {}",paymentId);

        } else if(paymentResult instanceof PaymentResult.Success success){
            log.warn("Invalid State");
            return null;
        }

        payment = paymentRepository.save(payment);

        eventPublisher.publish(EventAggregateType.PAYMENT , payment.getId() , "PAYMENT_STATUS_CHANGED",
                Map.of(
                        "orderId",payment.getOrder().getId().toString(),
                        "paymentId",payment.getId().toString(),
                        "merchantId" , merchantId.toString(),
                        "paymentStatus" , payment.getStatus().name(),
                        "amountUnits" , payment.getAmount().getAmountUnits(),
                        "amountCurrency" , payment.getAmount().getCurrency(),
                        "paymentMethod" , payment.getMethod()
                )
        );

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public void resolveAuthorization(UUID paymentId, boolean approove, String bankRef, String errrCode, String errorDescription) {


//        Payment payment =paymentRepository.findById(paymentId)
//                .orElseThrow(()-> new ResourceNotFoundException("Payment ",paymentId));

        Payment payment =paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(()-> new ResourceNotFoundException("Payment ",paymentId));

        if(payment.getStatus()!=PaymentStatus.AUTHORIZING){
            log.warn("payment not in authorizing state payment id : {} status : {}" , paymentId , payment.getStatus());
            return;
        }

        OrderRecord orderRecord = payment.getOrder();

        if(approove){
            paymentTransitionService.apply(payment , PaymentEvent.AUTHORIZE_SUCCESS);
            payment.setBankReference(bankRef);
            payment.setAuthorizedAt(LocalDateTime.now());


            // auto capture
            paymentTransitionService.apply(payment,PaymentEvent.CAPTURE_REQUEST);
            PaymentResult captredResult = paymentGatewayRouter.capture(payment.getMethod() , paymentId);

            if(captredResult instanceof  PaymentResult.Success success){
                paymentTransitionService.apply(payment,PaymentEvent.CAPTURE_SUCCESS);
                payment.setCapturedAt(LocalDateTime.now());
                orderRecord.setOrderStatus(OrderStatus.PAID);

            }else if(captredResult instanceof PaymentResult.Failure failure){
                paymentTransitionService.apply(payment , PaymentEvent.CAPTURE_FAIL);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorDescription());

            }else{
                paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
                payment.setErrorDescription(errrCode);
                payment.setErrorCode(errorDescription);
            }

            paymentRepository.save(payment);

            eventPublisher.publish(EventAggregateType.PAYMENT , payment.getId() , "PAYMENT_STATUS_CHANGED",
                    Map.of(
                            "orderId",payment.getOrder().getId().toString(),
                            "paymentId",payment.getId().toString(),
                            "merchantId" , payment.getMerchantId().toString(),
                            "paymentStatus" , payment.getStatus().name(),
                            "amountUnits" , payment.getAmount().getAmountUnits(),
                            "amountCurrency" , payment.getAmount().getCurrency(),
                            "paymentMethod" , payment.getMethod()
                    )
            );
            orderRepository.save(orderRecord);
        }


    }
}
