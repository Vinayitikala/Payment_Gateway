package com.vinay.paymentgateway.operations_service.settlement;


import com.vinay.paymentgateway.common_lib.dto.PaymentSettlementView;
import com.vinay.paymentgateway.common_lib.dto.SettlementBankDetails;
import com.vinay.paymentgateway.common_lib.entity.Money;
import com.vinay.paymentgateway.common_lib.enums.EventAggregateType;
import com.vinay.paymentgateway.common_lib.enums.SettlementStatus;
import com.vinay.paymentgateway.common_lib.exception.ResourceNotFoundException;
import com.vinay.paymentgateway.operations_service.entity.Settlement;
import com.vinay.paymentgateway.operations_service.entity.SettlementPayment;
import com.vinay.paymentgateway.operations_service.entity.SettlementPaymentId;
import com.vinay.paymentgateway.operations_service.outbox.OutboxEventPublisher;
import com.vinay.paymentgateway.operations_service.repository.SettlementPaymentRepository;
import com.vinay.paymentgateway.operations_service.repository.SettlementRepository;
import com.vinay.paymentgateway.operations_service.settlement.dto.BankTranferResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementTransactionExecutor {

    private final SettlementRepository settlementRepository;
    private final SettlementPaymentRepository settlementPaymentRepository;
    private final BankTrasferProcessor  bankTrasferProcessor;

    private final SettlementIntegrationGateway settlementIntegrationGateway;

    //toDo publishr inside it own db
    private final OutboxEventPublisher outboxEventPublisher;

    private static final double FEE_RATE=0.02;
    private static final double GST_RATE=0.10;

    @Transactional
    public void processForMerchant(UUID merchantId , LocalDate settlementDate){


            List<PaymentSettlementView> unsettledPayments = settlementIntegrationGateway.findUnsettledCaptured(merchantId);
            if (unsettledPayments.isEmpty()) {
                return;
            }

            log.info(" processing {} unsettlement payment for mervhnat id {} on  {} date" , unsettledPayments , merchantId , settlementDate);

            Integer grossAmount = unsettledPayments.stream()
                    .map(PaymentSettlementView::amountUnits)
                    .reduce(Integer::sum)
                    .orElse(0);

            Money gross=Money.of(grossAmount, unsettledPayments.getFirst().currency());

            int fee = Math.toIntExact(Math.round(gross.getAmountUnits() * FEE_RATE));
            int gst = Math.toIntExact(Math.round(fee * GST_RATE));

            Money feeAmount = Money.of(fee, gross.getCurrency());

            Money gstAmount = Money.of(gst, gross.getCurrency());

            Money netAmount = gross.subtract(feeAmount).subtract(gstAmount);

            Settlement settlement = Settlement.builder()
                    .merchantId(merchantId)
                    .grossAmount(gross)
                    .feeAmount(feeAmount)
                    .netAmount(netAmount)
                    .status(SettlementStatus.INTIATED)
                    .build();

            settlementRepository.save(settlement);
        try {
            List<SettlementPayment> links = new ArrayList<>();

            for (PaymentSettlementView p : unsettledPayments) {
                links.add(SettlementPayment.builder()
                        .id(new SettlementPaymentId(settlement.getId(), p.paymentId()))
                        .settlement(settlement)
                        .build());

            }

            settlementPaymentRepository.saveAll(links);


            SettlementBankDetails settlementBankDetails = settlementIntegrationGateway.getSettlementBankDetails(merchantId);

            //callthe bank transfer service to tranfer ner amount to merhnat bank
            BankTranferResult bankTranferResult = bankTrasferProcessor.initiate(settlement.getId(), merchantId,
                    netAmount, settlementBankDetails.accountNumber(),
                    settlementBankDetails.ifsc());

            settlement.setStatus(SettlementStatus.TRANSFER_PENDING);
            settlement.setBankReference(bankTranferResult.registrationRef());


            settlementRepository.save(settlement);
        }catch (Exception e){
            log.warn("settlementfailed settlement id :{} on {} date" , settlement.getId() ,settlementDate, e);

            settlement.setStatus(SettlementStatus.FAILED);
            settlementRepository.save(settlement);
        }

    }


    public void ressloveTransfer(UUID settlementId ,  String errorCode , String errorDescription){
        Settlement settlement=settlementRepository.findById(settlementId).orElseThrow(
                ()->new ResourceNotFoundException("Settlement not for tis settlement id {}" , settlementId));
        if(settlement.getStatus() != SettlementStatus.TRANSFER_PENDING){
            log.info("settlement ressoloved fot id :{}" , settlement.getId());

            if(errorCode==null){
                settlement.setStatus(SettlementStatus.PROCESSED);
                settlement.setProcessedAt(LocalDateTime.now());
                settlementRepository.save(settlement);


                List<SettlementPayment> settlementPaymentList =settlementPaymentRepository.findBySettlement(settlement);

                List<UUID> paymentIds= settlementPaymentList.stream()
                                .map(SettlementPayment::getId)
                                        .map(SettlementPaymentId::getPaymentId)
                                                .toList();

                settlementIntegrationGateway.markSettled(paymentIds);


                log.info(" Settlement processed duccessfully  settlementId :{}" , settlement.getId());
                outboxEventPublisher.publish(EventAggregateType.SETTLEMENT ,settlementId ,
                        "SETTLEMENT_PROCESSED" , Map.of(
                                "settlementId" , settlement,
                                "merchantId" , settlement.getMerchantId(),
                                "status" , settlement.getStatus().name(),
                                "settlementAount" , settlement.getNetAmount().getAmountUnits(),
                                "settlementCurrency" , settlement.getNetAmount().getCurrency()
                        ) );
            }{
                settlement.setStatus(SettlementStatus.FAILED);
                settlement.setFailureReason(errorCode +" : "+errorDescription);
                settlementRepository.save(settlement);
                log.warn(" Settlement failed sucessfully  settlementId :{}" , settlement.getId());

                outboxEventPublisher.publish(EventAggregateType.SETTLEMENT ,settlementId ,
                        "SETTLEMENT_FAILED" , Map.of(
                                "settlementId" , settlement,
                                "merchantId" , settlement.getMerchantId(),
                                "status" , settlement.getStatus().name(),
                                "settlementAount" , settlement.getNetAmount().getAmountUnits(),
                                "settlementCurrency" , settlement.getNetAmount().getCurrency()
                        ) );
            }
        }


    }
}
