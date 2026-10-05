package com.vinay.paymentgateway.api_gateway_service.security;

public class GatewayAuthenticationException extends RuntimeException {
    public GatewayAuthenticationException(String invalidOrExpiredToken) {
        super(invalidOrExpiredToken);
    }
}
