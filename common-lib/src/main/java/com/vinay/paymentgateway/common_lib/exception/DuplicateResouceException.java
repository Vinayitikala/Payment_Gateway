package com.vinay.paymentgateway.common_lib.exception;

import lombok.Getter;

@Getter
public class DuplicateResouceException  extends RuntimeException{

    private final String errorCode;


    public DuplicateResouceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
