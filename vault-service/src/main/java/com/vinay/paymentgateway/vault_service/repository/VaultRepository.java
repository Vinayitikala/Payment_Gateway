package com.vinay.paymentgateway.vault_service.repository;

import com.vinay.paymentgateway.vault_service.entity.VaultCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VaultRepository extends JpaRepository<VaultCard, UUID> {
}
