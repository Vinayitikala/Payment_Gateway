package com.vinay.paymentgateway.merchant_service.service.impl;


import com.vinay.paymentgateway.common_lib.cache.ApiKeyCache;
import com.vinay.paymentgateway.common_lib.exception.ResourceNotFoundException;
import com.vinay.paymentgateway.common_lib.util.RandomizerUtil;
//import com.vinay.paymentgateway.merchant_service.cache.ApiKeyCache;
import com.vinay.paymentgateway.merchant_service.dto.request.CreateApiKeyRequest;
import com.vinay.paymentgateway.merchant_service.dto.response.ApiKeyCreateResponse;
import com.vinay.paymentgateway.merchant_service.dto.response.ApiKeyResponse;
import com.vinay.paymentgateway.merchant_service.entity.ApiKey;
import com.vinay.paymentgateway.merchant_service.entity.Merchant;
import com.vinay.paymentgateway.merchant_service.mapper.ApiKeyMapper;
import com.vinay.paymentgateway.merchant_service.repository.ApiKeyRepository;
import com.vinay.paymentgateway.merchant_service.repository.MerchantRepository;
import com.vinay.paymentgateway.merchant_service.service.ApikeyServce;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ApiKeyServiceImpl implements ApikeyServce {
    private final MerchantRepository merchantRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyMapper apiKeyMapper;
    private final ApiKeyCache apiKeyCache;

    private final BCryptPasswordEncoder BCRYPT=new BCryptPasswordEncoder();

    @Override
    @Transactional
    public ApiKeyCreateResponse create(UUID merchantId, CreateApiKeyRequest request) {

//        log.info
        Merchant merchant =merchantRepository.findById(merchantId)
                .orElseThrow(()->new ResourceNotFoundException("merchant " , merchantId));

        String keyId= "rzp_"+request.environment().name().toLowerCase()+"_"+ RandomizerUtil.randomBase64(24);
        String rawSecret = RandomizerUtil.randomBase64(40); // TODO

        ApiKey apiKey = ApiKey.builder()
                .merchant(merchant)
                .keyId(keyId)
                .keySecretHash(BCRYPT.encode(rawSecret))
                .environment(request.environment())

                .build();

        apiKey = apiKeyRepository.save(apiKey);
       return new ApiKeyCreateResponse(apiKey.getId() , keyId , rawSecret , request.environment());
    }

    @Override
    public List<ApiKeyResponse> listByMerchant(UUID merchantId) {
        return apiKeyMapper.toResponse(apiKeyRepository.findByMerchant_Id(merchantId));



    }

    @Override
    @Transactional
    public void revoke(UUID merchantId, UUID keyId) {
        ApiKey key=apiKeyRepository.findById(keyId).filter(k-> k.getMerchant().getId().equals(merchantId))
                .orElseThrow(()-> new ResourceNotFoundException("ApiKey" , keyId));

        key.setEnabled(false);
        apiKeyCache.evict(key.getKeyId());

        apiKeyRepository.save(key);
    }

    @Override
    @Transactional
    public ApiKeyCreateResponse rotateKeyId(UUID merchantId, UUID keyId) {
        ApiKey apiKey=apiKeyRepository.findById(keyId).filter(k-> k.getMerchant().getId().equals(merchantId))
                .orElseThrow(()-> new ResourceNotFoundException("ApiKey" , keyId));




        if(!apiKey.isEnabled()) throw new RuntimeException("Cannot rotate a disabled Key");


        String newRawSecret = RandomizerUtil.randomBase64(40);// TODO




        apiKey.setPreviousKeySecretHash(apiKey.getKeySecretHash());

        apiKey.setKeySecretHash(BCRYPT.encode(newRawSecret));

        apiKey.setRotatedAt(LocalDateTime.now());

        apiKey.setGracePeriodExpiresAt(LocalDateTime.now().plusHours(24));

        apiKey=apiKeyRepository.save(apiKey);

        apiKeyCache.evict(apiKey.getKeyId())
        ;



        return new ApiKeyCreateResponse(apiKey.getId() ,
                apiKey.getKeyId() ,
                newRawSecret ,
                apiKey.getEnvironment());

    }
}
