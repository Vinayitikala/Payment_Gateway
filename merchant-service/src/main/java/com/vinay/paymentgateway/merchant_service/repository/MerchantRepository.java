package com.vinay.paymentgateway.merchant_service.repository;


import com.vinay.paymentgateway.common_lib.enums.MerchantStatus;
import com.vinay.paymentgateway.merchant_service.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
//@Repository

//you van ingnoe
public interface MerchantRepository extends JpaRepository<Merchant , UUID> {
    boolean existsByEmail( String email);

    List<Merchant> findByStatus(MerchantStatus merchantStatus);
//    @Email @NotNull(message = "email is Required")
}
