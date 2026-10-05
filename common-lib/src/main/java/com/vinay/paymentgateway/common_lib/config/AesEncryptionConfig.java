package com.vinay.paymentgateway.common_lib.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;


public class AesEncryptionConfig {



    @Bean
    public BytesEncryptor masterKeyEncrypter(String masterKey){
        byte[] masterKeyBytes= Base64.getDecoder().decode(masterKey);

        SecretKeySpec masterDecKey=new SecretKeySpec(masterKeyBytes,"AES");
        return new AesBytesEncryptor(masterDecKey,KeyGenerators.secureRandom(12),
                AesBytesEncryptor.CipherAlgorithm.GCM);

    }
}
