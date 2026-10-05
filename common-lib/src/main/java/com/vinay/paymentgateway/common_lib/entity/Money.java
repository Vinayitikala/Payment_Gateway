package com.vinay.paymentgateway.common_lib.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

//@AllArgsConstructor\
@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class Money {

    private int amountUnits;
    private String currency;


    public static Money of(int amountUnits,String currency){
        return new Money(amountUnits,currency);
    }

    public Money add(Money other){
        if(this.currency.equals(other.currency)){
            throw new IllegalArgumentException("cannot add money with diffrent currencies");
        }
        return new Money(this.amountUnits+other.amountUnits,this.currency);
    }
    public Money subtract(Money other){
        if(this.currency.equals(other.currency)){
            throw new IllegalArgumentException("cannot add money with diffrent currencies");
        }
        return new Money(this.amountUnits-other.amountUnits,this.currency);
    }

    private static Money inr(int amountUnits){
        return new Money(amountUnits,"INR");
    }


}
