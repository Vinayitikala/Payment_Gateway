package com.vinay.paymentgateway.payment_service.repository;
import com.vinay.paymentgateway.payment_service.Entity.PaymentTransitionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentTransitionLogRepository extends JpaRepository<PaymentTransitionLog, UUID> {
}
