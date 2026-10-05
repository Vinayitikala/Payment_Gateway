package com.vinay.paymentgateway.operations_service.webhook;


import com.vinay.paymentgateway.common_lib.dto.WebhookTarget;
import com.vinay.paymentgateway.common_lib.enums.WebhookEventStatus;
import com.vinay.paymentgateway.common_lib.util.SignerUtil;
import com.vinay.paymentgateway.operations_service.client.MerchantServiceClient;
import com.vinay.paymentgateway.operations_service.entity.WebhookEvent;
import com.vinay.paymentgateway.operations_service.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.CannotCreateTransactionException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookKafkaConsumer {




    private final ObjectMapper objectMapper;
    private final SignerUtil signerUtil;
    private final WebhookEventRepository webhookEventRepository;
    private final WebhookDlqRecorder dlqRecorder;
    private final WebhookRetryQueue webhookRetryQueue;
    private final MerchantServiceClient merchantServiceClient;


    @KafkaListener(topics = {
            "${app.kafka.topics.payments:payments.events}",
            "${app.kafka.topics.orders:orders.events}",
            "${app.kafka.topics.refunds:refunds.events}",
            "${app.kafka.topics.settlements:settlements.events}"
    })
    public void onWebhookEvent(ConsumerRecord <String  , Map<String  , Object> >record, Acknowledgment ack){
        try {
            Map<String, Object> envelope = record.value();
            Map<String, Object> data = (Map<String, Object>) envelope.get("data");
            String eventType = (String) envelope.get("eventType");

            Object merchantIdRaw = data.get("merchantId");
            if (merchantIdRaw == null) {
                log.warn("no merchantId fount skiping event : {}", eventType);
                ack.acknowledge();
                return;
            }

            UUID merchantId = UUID.fromString(merchantIdRaw.toString());

            List<WebhookTarget> targets = merchantServiceClient.getActiveConfigsForEvent(merchantId, eventType);
            if (targets.isEmpty()) {
                log.debug("No webhook target was found skiping");
                ack.acknowledge();
                return;
            }

            Map<String, Object> signatureDate =
                    Map.of(
                            "event", eventType,
                            "payload", data

                    );
            String signatureJson = objectMapper.writeValueAsString(signatureDate);

            for (WebhookTarget target : targets) {
                String signature = signerUtil.sign(signatureJson, target.webhookSecret());

                WebhookEvent webhookEvent = WebhookEvent.builder()
                        .merchantId(merchantId)
                        .eventType(eventType)
                        .payload(data)
                        .targetUrl(target.targetUrl())
                        .signature(signature)
                        .status(WebhookEventStatus.PENDING)
                        .nextRetryAt(LocalDateTime.now())
                        .build();

                webhookEvent = webhookEventRepository.save(webhookEvent);

                //redis queue enqueue webhookEventgetId

                webhookRetryQueue.enqueue(webhookEvent.getId(), webhookEvent.getNextRetryAt());
                log.info("Created a webhook event with id: {}", webhookEvent.getId());

            }

            ack.acknowledge();
        }catch (DataAccessException | CannotCreateTransactionException dbDown){
            log.error("Webhook consmer failed todue then the doDown could not process the reord offSet : {} " , record
                    .offset() , dbDown);
            // TODO check Exception for acknoledge
        }catch (Exception logicError){
            log.error("Webhook consmer failed todue then the logic error could not process the reord offSet : {} " , record.offset() , logicError);

            dlqRecorder.recorConsumerFailed(record , logicError.getMessage());

            ack.acknowledge();
        }


    }

}
