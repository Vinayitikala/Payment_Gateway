package com.vinay.paymentgateway.common_lib.ratelimit;

public interface RateLimiter {

    RateLimitResult check(String key , int maxRequestsAllowed , long windowSeconds );



}
