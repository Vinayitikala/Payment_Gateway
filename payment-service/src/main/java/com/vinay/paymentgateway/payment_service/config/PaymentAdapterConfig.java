package com.vinay.paymentgateway.payment_service.config;

import com.vinay.paymentgateway.common_lib.enums.PaymentMethod;
import com.vinay.paymentgateway.payment_service.gateway.PaymentAdapter;
import com.vinay.paymentgateway.payment_service.gateway.adapter.CardPaymentAdapter;
import com.vinay.paymentgateway.payment_service.gateway.adapter.NetBankingAdapter;
import com.vinay.paymentgateway.payment_service.gateway.adapter.UpiPaymentAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
@RequiredArgsConstructor
@Configuration
public class PaymentAdapterConfig {


    private final NetBankingAdapter netBankingAdapter;
    private final UpiPaymentAdapter upiPaymentAdapter;
    private final CardPaymentAdapter cardPaymentAdapter;

    @Bean
    public Map<PaymentMethod, PaymentAdapter>  paymentAdapterMap(){
        return Map.of(
                PaymentMethod.CARD,cardPaymentAdapter,
                PaymentMethod.NETBANKING , netBankingAdapter,
                PaymentMethod.UPI , upiPaymentAdapter
        );
    }
}
