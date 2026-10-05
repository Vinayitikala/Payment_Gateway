package com.vinay.paymentgateway.operations_service.settlement;


import com.vinay.paymentgateway.common_lib.enums.SettlementStatus;
import com.vinay.paymentgateway.operations_service.entity.Settlement;
import com.vinay.paymentgateway.operations_service.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BankSettlementCallbackSimulator {
    private final SettlementRepository settlementRepository;
    private final SettlementTransactionExecutor settlementTransactionExecutor;

    @Scheduled(fixedDelay = 5000)
    @SchedulerLock(name = "operations-service-settlement-simulator", lockAtMostFor = "10s", lockAtLeastFor = "1s")

    public void processCallbacks(){

        List<Settlement> settlements=settlementRepository.findByStatus(SettlementStatus.TRANSFER_PENDING);

        if(settlements.isEmpty()){
            return;
        }

        for(Settlement settlement : settlements){
            simulatorCallback(settlement);
        }

    }

    private void simulatorCallback(Settlement settlement){

        log.info("initiating settlemnnt call baclk for settlement ID :{}" , settlement.getId() );
        settlementTransactionExecutor.ressloveTransfer(settlement.getId(),null, null);
    }
}
