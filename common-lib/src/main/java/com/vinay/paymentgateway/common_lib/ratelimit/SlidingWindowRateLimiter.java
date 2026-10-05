package com.vinay.paymentgateway.common_lib.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.UUID;

@RequiredArgsConstructor
public class SlidingWindowRateLimiter implements  RateLimiter{

    private final StringRedisTemplate redis;


    @Override
    public RateLimitResult check(String key, int maxRequestsAllowed, long windowSeconds) {
        long nowMs=System.currentTimeMillis();

        long florMs=nowMs-windowSeconds*1000;

        String redisKey = "ratelimit:sliding:"+key;

        var zset=redis.opsForZSet();

        zset.removeRangeByScore(redisKey , Double.NEGATIVE_INFINITY , florMs);

        Long count=zset.zCard(redisKey);

        long current=count!=null?count:0;

        if(current >=maxRequestsAllowed){

            var oldest=zset.rangeWithScores(redisKey , 0 ,0);
            int retryAfter =1;

            if((oldest!=null) && !oldest.isEmpty()){
                Double oldestScore=oldest.iterator().next().getScore();
                if(oldestScore!=null){
                    long windowExpiresMS=oldestScore.longValue()+windowSeconds*1000;
                    retryAfter=(int)Math.ceil((windowExpiresMS-nowMs)/1000.0);
                }
            }






            return RateLimitResult.denied(retryAfter);
        }

        zset.add(redisKey , UUID.randomUUID().toString() , nowMs);
        redis.expire(redisKey , Duration.ofSeconds(windowSeconds+1));

        return RateLimitResult.allowed((int)(maxRequestsAllowed-current-1));




    }
}
