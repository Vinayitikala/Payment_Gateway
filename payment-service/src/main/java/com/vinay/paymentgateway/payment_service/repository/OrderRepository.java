package com.vinay.paymentgateway.payment_service.repository;

import com.vinay.paymentgateway.payment_service.Entity.OrderRecord;
import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderRecord, UUID> {

    boolean existsByMerchantIdAndReceipt(UUID merchantId, @Size(max=100) String receipt);

   Optional<OrderRecord> findByIdAndMerchantId(UUID merchantId, UUID orderId);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OrderRecord o where o.id = :uuid and o.merchantId =:merchanId")
    Optional<OrderRecord> findByIdAndMerchantIdForUpdate(@NotNull(message = "order id is required") UUID uuid, UUID merchantId);
}
