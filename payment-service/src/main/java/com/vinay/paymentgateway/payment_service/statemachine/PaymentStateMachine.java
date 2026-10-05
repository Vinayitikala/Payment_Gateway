package com.vinay.paymentgateway.payment_service.statemachine;
import com.vinay.paymentgateway.common_lib.enums.PaymentEvent;
import com.vinay.paymentgateway.common_lib.enums.PaymentStatus;
import com.vinay.paymentgateway.common_lib.exception.InvalidStateTransistionException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentStateMachine {


    private record Transition(PaymentStatus from , PaymentEvent event){}

    private static final Map<Transition , PaymentStatus> TRANSITION=Map.ofEntries(
      Map.entry(new Transition(PaymentStatus.CREATED , PaymentEvent.AUTHORIZE_ATTEMPT) , PaymentStatus.AUTHORIZING),
            Map.entry(new Transition(PaymentStatus.AUTHORIZING , PaymentEvent.AUTHORIZE_SUCCESS) , PaymentStatus.AUTHORIZED),
            Map.entry(new Transition(PaymentStatus.AUTHORIZING , PaymentEvent.AUTHORIZE_FAIL) , PaymentStatus.FAILED),
            Map.entry(new Transition(PaymentStatus.AUTHORIZED , PaymentEvent.CAPTURE_REQUEST) , PaymentStatus.CAPTURING),
            Map.entry(new Transition(PaymentStatus.CAPTURING , PaymentEvent.CAPTURE_SUCCESS) , PaymentStatus.CAPTURED),
            Map.entry(new Transition(PaymentStatus.CAPTURING , PaymentEvent.CAPTURE_FAIL) , PaymentStatus.AUTHORIZED),
            Map.entry(new Transition(PaymentStatus.CAPTURED , PaymentEvent.REFUND_INIT) , PaymentStatus.PRTIALLY_REFUNDED),
            Map.entry(new Transition(PaymentStatus.SETTLED , PaymentEvent.REFUND_INIT) , PaymentStatus.PRTIALLY_REFUNDED),
            Map.entry(new Transition(PaymentStatus.SETTLED , PaymentEvent.REFUND_COMPLETE) , PaymentStatus.REFUNDED),
            Map.entry(new Transition(PaymentStatus.CAPTURED , PaymentEvent.SETTLE) , PaymentStatus.SETTLED),
            Map.entry(new Transition(PaymentStatus.PRTIALLY_REFUNDED , PaymentEvent.REFUND_COMPLETE) , PaymentStatus.REFUNDED),
            Map.entry(new Transition(PaymentStatus.CREATED , PaymentEvent.CANCEL) , PaymentStatus.CANCELLED),
            Map.entry(new Transition(PaymentStatus.AUTHORIZING , PaymentEvent.CANCEL) , PaymentStatus.CANCELLED),
            Map.entry(new Transition(PaymentStatus.AUTHORIZED , PaymentEvent.CAPTURE_TIMEOUT) , PaymentStatus.AUTH_EXPIRED)







            );


    public PaymentStatus transistion(PaymentStatus current , PaymentEvent event){
        PaymentStatus next=TRANSITION.get(new Transition(current , event));

        if(next==null){
            throw new InvalidStateTransistionException(current.name() , event.name());
        }
        return  next;
    }

}
