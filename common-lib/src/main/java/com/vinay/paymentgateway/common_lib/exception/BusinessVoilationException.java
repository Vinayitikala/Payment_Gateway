package com.vinay.paymentgateway.common_lib.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusinessVoilationException  extends RuntimeException{
    private final String errorCode;


    public BusinessVoilationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
