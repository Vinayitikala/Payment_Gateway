package com.vinay.paymentgateway.merchant_service.service;

import com.vinay.paymentgateway.merchant_service.dto.request.LoginRequest;
import com.vinay.paymentgateway.merchant_service.dto.request.MerchantSignUpRequest;
import com.vinay.paymentgateway.merchant_service.dto.response.LoginResponse;
import com.vinay.paymentgateway.merchant_service.dto.response.MerchantResponse;

public interface AuthService {
    MerchantResponse signup(MerchantSignUpRequest request);

    LoginResponse login(LoginRequest request);
}
