package com.vinay.paymentgateway.common_lib.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        String errorCode,
        String errorDescription,
        LocalDateTime timeStamp,
        List<FieldError> fieldErrors
) {
    public record FieldError(String fient , String message){ }
    public static  ErrorResponse of(String errorCode , String errorDescription ){
        return new ErrorResponse(errorCode,errorDescription,LocalDateTime.now(),null);
    }

    public static  ErrorResponse of(String errorCode , String errorDescription , List<FieldError> fieldErrors ){
        return new ErrorResponse(errorCode,errorDescription,LocalDateTime.now(),fieldErrors);
    }

}
