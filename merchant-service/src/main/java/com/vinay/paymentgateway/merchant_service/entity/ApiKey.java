package com.vinay.paymentgateway.merchant_service.entity;


import com.vinay.paymentgateway.common_lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import com.vinay.paymentgateway.common_lib.enums.Environment;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="apikey",
        indexes = {
        @Index(name = "idx_api_key_merchant_env" , columnList = "merchant_id , environment , enabled")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional = false)
    @JoinColumn(name="merchant_id",nullable = false)
    private Merchant merchant;
    @Column(nullable = false,length = 50,unique = true)
    private String keyId;
    @Column(nullable = false,length = 200)
    private String keySecretHash;

    @Column(length = 200)
    private String previousKeySecretHash;




    //    private String webhookSecretHash;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 10)

    private Environment environment;
    @Column(nullable = false)
    @Builder.Default
    private boolean enabled=true;
    private LocalDateTime lastUsedAt;
    private LocalDateTime rotatedAt;
    private LocalDateTime gracePeriodExpiresAt;

    public boolean isInGracePeriod() {
        return gracePeriodExpiresAt != null && LocalDateTime.now().isBefore(gracePeriodExpiresAt);
    }

}
