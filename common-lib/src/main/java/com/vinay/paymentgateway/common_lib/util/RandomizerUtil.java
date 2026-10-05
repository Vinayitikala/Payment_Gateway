package com.vinay.paymentgateway.common_lib.util;

import java.security.SecureRandom;
import java.util.Base64;

public class RandomizerUtil {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String randomBase64(int length) {
//        UUID.randomUUID().toString().replace("-","");

        byte[] buf = new byte[length / 2];

        SECURE_RANDOM.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}

