package com.vinay.paymentgateway.operations_service.settlement;



import com.vinay.paymentgateway.common_lib.entity.Money;
import com.vinay.paymentgateway.operations_service.settlement.dto.BankTranferResult;

import java.util.UUID;

public interface BankTrasferProcessor {
    BankTranferResult initiate(UUID settlementId , UUID merchantId , Money amount ,
                               String bankAccount , String ifsc);
}
