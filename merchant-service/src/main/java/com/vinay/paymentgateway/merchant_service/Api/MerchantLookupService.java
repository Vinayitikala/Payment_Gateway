package com.vinay.paymentgateway.merchant_service.Api;


import com.vinay.paymentgateway.common_lib.dto.SettlementBankDetails;
import com.vinay.paymentgateway.common_lib.dto.WebhookTarget;

import java.util.List;
import java.util.UUID;

public interface MerchantLookupService {
    List<WebhookTarget> getActiveConfigsForEvent(UUID merchantId , String eventType);


    List<UUID> listActiveMerchantIds();

    SettlementBankDetails getSettlementBankDetails(UUID merchantId);
}
