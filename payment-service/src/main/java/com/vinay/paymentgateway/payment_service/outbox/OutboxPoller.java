package com.vinay.paymentgateway.payment_service.outbox;


import com.vinay.paymentgateway.common_lib.config.KafkaProperties;
import com.vinay.paymentgateway.common_lib.enums.OutboxStatus;
import com.vinay.paymentgateway.payment_service.Entity.OutboxEvent;
import com.vinay.paymentgateway.payment_service.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPoller {
//    private final CardTokenRepository cardTokenRepository;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String  , Object> kafkaTemplate;
    private final KafkaProperties kafkaProperties;
    private final OutboxResultHandler outboxResultHandler;

    @Scheduled(fixedDelay = 5000)
    @SchedulerLock(name = "payment-service-outbox-poller", lockAtMostFor = "1m", lockAtLeastFor = "1s")
    public void poll(){



            List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

            for (OutboxEvent event : pendingEvents) {
                try {
                String topic = kafkaProperties.topicFor(event.getAggregateType());
                String key = extractMerchantId(event.getPayload());

                Map<String, Object> envelope = Map.of(
                        "eventType", event.getEventType(),
                        "aggregateType", event.getAggregateType().name(),
                        "aggreagateId", event.getAggregateId().toString(),
                        "data", event.getPayload()


                );

                kafkaTemplate.send(topic, key, envelope)
                        .get(5, TimeUnit.SECONDS);

                outboxResultHandler.hanleEventPublished(event);

            }catch (Exception e){

                    log.error("Outbox event failer eventId :{} , attmepsts : {]" , event.getId() , event.getAttempts());
                    outboxResultHandler.handleEventFailure(event , e.getMessage());
                    throw new RuntimeException(e);
                }
        }


    }



    private String extractMerchantId(Map<String ,Object> payload){
        Object value=payload.get("merchantId");
        return value!=null ? value.toString() : "unknown";

    }

}
