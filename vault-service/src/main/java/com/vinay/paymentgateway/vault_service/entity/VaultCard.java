package com.vinay.paymentgateway.vault_service.entity;


import com.vinay.paymentgateway.common_lib.entity.BaseEntity;
import com.vinay.paymentgateway.common_lib.enums.CardBrand;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="valut_card")
@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VaultCard  extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false,length = 4)
    private String lastFour;



    @Column(nullable = false,length = 6)
    private String bin;


    @Column(nullable = false)
    private byte[] encryptePan;

    @Column(nullable = false)
    private byte[] encrytpedDek;




    @Column(nullable = false)

    @Enumerated(EnumType.STRING)
    private CardBrand brand;

    @Column(nullable = false)


    private String expiryMonth;


    @Column(nullable = false)

    private String expiryYear;


    @Column(nullable = false)

    private String cardHolderName;
    private LocalDateTime deletedAt;

}
