package com.vinay.paymentgateway.common_lib.idempotency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Optional;


@RequiredArgsConstructor
@Slf4j
public class RedisIdempotencyStore implements IdempotencyStore {


    private final StringRedisTemplate redis;
    private static final String PREFIX="idempotency";


    @Override
    public boolean setIfAbsent(String key, Duration ttl) {
        try{

            Boolean set=redis.opsForValue().setIfAbsent(PREFIX+key , IN_PROGRESS , ttl);
            return  Boolean.TRUE.equals(set);

        }catch (DataAccessException e){
//            throw new RuntimeException(e);
            log.warn("IDEMPOTENCy store unavailable failing open for key = {}" , key , e);
            return true;
        }
    }

    @Override
    public void store(String key, String value, Duration ttl) {
        try{
            redis.opsForValue().set(PREFIX+key , value , ttl);


        }catch (DataAccessException e){
            log.warn("failed to perssist , failing to oprn for key = {} " , key ,e);


        }
    }

    @Override
    public Optional<String> get(String key) {

        try{
            return Optional.of(redis.opsForValue().get(PREFIX+key));

        }catch (DataAccessException e){
            log.warn("failed to perssist , failing to oprn for key = {} " , key ,e);
            return Optional.empty();
        }
    }

    @Override
    public void delete(String key) {
        try{
            redis.delete(PREFIX+key);
        }catch (DataAccessException e){
            log.warn(" failing to clear idempotency  for key = {} " , key ,e);
        }
    }
}
