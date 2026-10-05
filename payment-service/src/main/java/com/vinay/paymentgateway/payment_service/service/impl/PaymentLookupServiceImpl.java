package com.vinay.paymentgateway.payment_service.service.impl;


import com.vinay.paymentgateway.common_lib.dto.PaymentSettlementView;
import com.vinay.paymentgateway.common_lib.enums.PaymentStatus;
import com.vinay.paymentgateway.payment_service.Entity.Payment;
import com.vinay.paymentgateway.payment_service.api.PaymentLookupService;
import com.vinay.paymentgateway.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentLookupServiceImpl implements PaymentLookupService {


    private final PaymentRepository paymentRepository;


//    @Override
//    public List<PaymentSettlementView> findUnsettlementCapturedPayments(UUID merchantId) {
//        List<Payment> paymentList= paymentRepository.findByMerchantIdAndStatusForUpdate(merchantId , PaymentStatus.CAPTURED);
//
//        return paymentList.stream()
//                .map(p -> new PaymentSettlementView(
//                        p.getId(),
//                        p.getAmount().getAmountUnits(),
//                        0, // TODO: replace with actual refund values from RefundRepository
//                        p.getAmount().getCurrency()))
//                .toList();
//    }


//

    @Override
    public List<PaymentSettlementView> findUnsettledCapturedPayments(UUID merchantId) {
        List<Payment> paymentList= paymentRepository.findByMerchantIdAndStatusForUpdate(merchantId , PaymentStatus.CAPTURED);

        return paymentList.stream()
                .map(p -> new PaymentSettlementView(
                        p.getId(),
                        p.getAmount().getAmountUnits(),
                        0, // TODO: replace with actual refund values from RefundRepository
                        p.getAmount().getCurrency()))
                .toList();
    }

    @Override
    public void markSettled(List<UUID> paymentList) {
        LocalDateTime now = LocalDateTime.now();
        List<Payment> payments = paymentRepository.findAllById(paymentList);
        for (Payment payment : payments) {
            payment.setStatus(PaymentStatus.SETTLED);
            payment.setSettledAt(now);
        }
        paymentRepository.saveAll(payments);
    }
}
