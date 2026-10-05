package com.vinay.paymentgateway.common_lib.exception;

import lombok.Getter;

@Getter
public class InvalidStateTransistionException extends RuntimeException{

    private final String fromState;
    private final String event;

    public InvalidStateTransistionException(String  fromState , String  event){
        super("Invalid travsition from " + fromState + " to " + event);
        this.fromState=fromState;
        this.event =event;
    }
}
