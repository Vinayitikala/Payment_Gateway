package com.vinay.paymentgateway.merchant_service.service.impl;


import com.vinay.paymentgateway.common_lib.enums.MerchantStatus;
import com.vinay.paymentgateway.common_lib.enums.UserRole;
import com.vinay.paymentgateway.common_lib.exception.BusinessVoilationException;
import com.vinay.paymentgateway.common_lib.exception.DuplicateResouceException;
import com.vinay.paymentgateway.common_lib.exception.ResourceNotFoundException;
import com.vinay.paymentgateway.merchant_service.dto.request.LoginRequest;
import com.vinay.paymentgateway.merchant_service.dto.request.MerchantSignUpRequest;
import com.vinay.paymentgateway.merchant_service.dto.response.LoginResponse;
import com.vinay.paymentgateway.merchant_service.dto.response.MerchantResponse;
import com.vinay.paymentgateway.merchant_service.entity.AppUser;
import com.vinay.paymentgateway.merchant_service.entity.Merchant;
import com.vinay.paymentgateway.merchant_service.mapper.MerchantMapper;
import com.vinay.paymentgateway.merchant_service.repository.AppUserRepository;
import com.vinay.paymentgateway.merchant_service.repository.MerchantRepository;
import com.vinay.paymentgateway.merchant_service.security.JwtUtil;
import com.vinay.paymentgateway.merchant_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantMapper merchantMapper;

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    @Override
    @Transactional
    public MerchantResponse signup(MerchantSignUpRequest request) {
        if(merchantRepository.existsByEmail(request.email())){
            throw new DuplicateResouceException("Duplicate Merchant Email","Merchant wit email exists : " +request.email());
        }

        Merchant merchant=merchantMapper.toEntityFromSignUpRequest(request);
        merchant.setStatus(MerchantStatus.PENDING_KYC);
        merchant=merchantRepository.save(merchant);



        AppUser appUser=AppUser.builder()
                .email(request.email())
                .merchant(merchant)
                .passwordHash(passwordEncoder.encode(request.password())) // TODO encrpt pasword
                .role(UserRole.OWNER)
                .build();

        appUserRepository.save(appUser);

        return merchantMapper.toResponse(merchant);

    }

    @Override
    public LoginResponse login(LoginRequest request) {


        //verify the pasword



        log.info(request.email() + " befre find by");

        AppUser appUser=appUserRepository.findByEmail(request.email())
                .orElseThrow(()->new ResourceNotFoundException("User mail not fount " , request.email()));

        if(!passwordEncoder.matches(request.password() , appUser.getPasswordHash())){
            log.info("INAVID CREDENTIALS");
            throw new BusinessVoilationException("Inavlid credentials "," invalid email or password");
        }

        String token=jwtUtil.generateAccessToken(request.email() , appUser.getMerchant().getId() ,appUser.getRole().toString());

        return new LoginResponse(token);

    }
}
