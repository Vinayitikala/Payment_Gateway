package com.vinay.paymentgateway.vault_service.entity;

import com.vinay.paymentgateway.common_lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="card_token")
@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CardToken extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false,length = 50,unique = true)
    private String token;


    @ManyToOne(fetch = FetchType.EAGER,optional = false)
    @JoinColumn(nullable = false,name="vault_card_id")
    private VaultCard vaultCard;

    private UUID customerId;

    @Column(nullable = false,name="merchant_id")
    private UUID merchant;

    private LocalDateTime revokedAt;

}
