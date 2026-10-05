package com.vinay.paymentgateway.operations_service.settlement;


import com.vinay.paymentgateway.common_lib.entity.Money;
import com.vinay.paymentgateway.common_lib.util.RandomizerUtil;
import com.vinay.paymentgateway.operations_service.settlement.dto.BankTranferResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
@Service
@Slf4j
@RequiredArgsConstructor
public class BankTransferProcessorImpl implements BankTrasferProcessor{



    @Override
    public BankTranferResult initiate(UUID settlementId, UUID merchantId, Money amount, String bankAccount, String ifsc) {

        // call the bank Api

        String registrationRef="TXN_"+ RandomizerUtil.randomBase64(12);


        log.debug("BAnk transfer call complted for settlement id :{} , registratioref :{}" , settlementId , registrationRef);
        return new BankTranferResult(registrationRef);
    }
}
