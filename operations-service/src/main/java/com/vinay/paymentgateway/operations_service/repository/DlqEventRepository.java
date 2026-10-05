package com.vinay.paymentgateway.operations_service.repository;

import com.vinay.paymentgateway.operations_service.entity.DlqEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DlqEventRepository extends JpaRepository<DlqEvent, UUID> {
}
