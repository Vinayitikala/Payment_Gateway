package com.vinay.paymentgateway.common_lib.config;


import com.vinay.paymentgateway.common_lib.enums.EventAggregateType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;


@Setter
@Getter
@ConfigurationProperties(prefix = "app.kafka")
public class KafkaProperties {

    private Map<String  , String > topics=new HashMap<>();

    public String topicFor(EventAggregateType aggregateType){
        String topic=topics.get(aggregateType.name().toLowerCase());
        if(topic==null){
            throw new IllegalStateException("No kafka topic is configred for aggregationtype : " + aggregateType);
        }


        return topic;



    }

}
