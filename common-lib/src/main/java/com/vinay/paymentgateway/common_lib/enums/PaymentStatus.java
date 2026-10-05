package com.vinay.paymentgateway.common_lib.enums;

public enum PaymentStatus {
    CREATED,
    AUTHORIZING,
    AUTHORIZED,
    CAPTURING,
    CAPTURED,
    FAILED,
    CANCELLED,
    REFUNDED,
    PRTIALLY_REFUNDED,
    SETTLED,
    AUTH_EXPIRED
}
