package com.vinay.paymentgateway.operations_service.webhook;


import com.vinay.paymentgateway.common_lib.enums.WebhookEventStatus;
import com.vinay.paymentgateway.operations_service.entity.WebhookEvent;
import com.vinay.paymentgateway.operations_service.repository.WebhookEventRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebhookDeliveryScheduler {
    private final WebhookRetryQueue retryQueue;
    private final WebhookEventRepository webhookEventRepository;

    private ExecutorService virtualThreadExecter;
    @PostConstruct
    void init(){
        virtualThreadExecter= Executors.newVirtualThreadPerTaskExecutor();

    }

    @PreDestroy
    void shutdown(){
        virtualThreadExecter.shutdown();
    }

    private final WebhookDeliverExecuter deliverExecuter;
    @Value("${app.webhook.delivery.poll-batch-size:100}")
    private int batchSize;


    @Scheduled(fixedDelay = 1000)
    @SchedulerLock(name = "operations-service-webhook-delivery-poll-deliver", lockAtMostFor = "10s", lockAtLeastFor = "1s")

    public void pollAndDeliver(){
        Set<UUID> due=retryQueue.pollDue(batchSize);

                for(UUID webhookEvent : due){
                    // eecuter .deliver webhookeventId
                    virtualThreadExecter.submit(()->{
                            deliverExecuter.deliver(webhookEvent);
                    });


                }


    }
    @Scheduled(fixedDelay = 10000)
    @SchedulerLock(name = "operations-service-webhook-delivery-reconcile-from-db", lockAtMostFor = "10s", lockAtLeastFor = "1s")

    public void reconcileFromDatabase(){
        LocalDateTime now=LocalDateTime.now();
        List<WebhookEvent> due=webhookEventRepository.findByStatusAndNextRetryAtBefore(WebhookEventStatus.PENDING , now);

        for(WebhookEvent event : due){
            retryQueue.enqueueIfAbsent(event.getId() , event.getNextRetryAt());
        }
    }
}
