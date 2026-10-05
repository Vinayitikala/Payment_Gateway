package com.vinay.paymentgateway.operations_service.webhook;


import com.vinay.paymentgateway.common_lib.enums.WebhookEventStatus;
import com.vinay.paymentgateway.operations_service.entity.DlqEvent;
import com.vinay.paymentgateway.operations_service.entity.WebhookEvent;
import com.vinay.paymentgateway.operations_service.repository.DlqEventRepository;
import com.vinay.paymentgateway.operations_service.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookDlqRecorder {

    private final WebhookEventRepository webhookEventRepository;
    private final DlqEventRepository dlqEventRepository;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAfterAttemptsExhausted(WebhookEvent webhookEvent , String finalEroor){
        log.debug("Recoding the , webhook event {}"  , webhookEvent.getId());

        webhookEvent.setStatus(WebhookEventStatus.DEAD);
        webhookEventRepository.save(webhookEvent);

        DlqEvent dlqEvent=DlqEvent.builder()
                .webhookEvent(webhookEvent)
                .merchantId(webhookEvent.getMerchantId())
                .finalError(finalEroor)
                .payload(webhookEvent.getPayload())
                .build();

        dlqEventRepository.save(dlqEvent);


    }

    public void recorConsumerFailed(ConsumerRecord<String, Map<String, Object>> record, String message) {

        Map<String , Object> envelope=record.value();
        UUID merchantId=null;

        try{
            Map<String, Object> data = (Map<String, Object>) envelope.get("data");
            Object merchantIdRaw = data != null ? data.get("merchantId") : null;
            if (merchantIdRaw != null) {
                merchantId = UUID.fromString(merchantIdRaw.toString());
            }

        }catch (Exception e){

        }

        log.debug("Recoding the dlq consumer failed with merchantID : {} " , merchantId);


        log.debug("Recording the dlq because consumer failed event with merchantId: {}", merchantId);
        DlqEvent dlqEvent = DlqEvent.builder()
                .webhookEvent(null)
                .merchantId(merchantId)
                .finalError(message)
                .payload(envelope != null ? envelope: Map.of())
                .build();
        dlqEventRepository.save(dlqEvent);
    }
}
