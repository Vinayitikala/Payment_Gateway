package com.vinay.paymentgateway.api_gateway_service.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthHandler {

    private final JwtVerifier jwtVerifier;

    public Map<String, String> authenticate(String token) {
        Claims claims;
        try {
            claims = jwtVerifier.verify(token);
        } catch (Exception e) {
            throw new GatewayAuthenticationException("Invalid or expired token");
        }
        String merchantId=jwtVerifier.extractMerchantId(claims);
        String role=jwtVerifier.extractRole(claims);
        log.info("JWT merchantId = {}", merchantId);
        log.info("JWT role = {}", role);
        log.info("========== MERCHANT CONTEXT ==========");
        log.info("X-Merchant-Id header = {}", merchantId);

        return Map.of(
                "X-Merchant-Id", jwtVerifier.extractMerchantId(claims),
                "X-User-Role", jwtVerifier.extractRole(claims)
        );
    }
}
