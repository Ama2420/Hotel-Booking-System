package com.rupeek.hotelbooking.infrastructure.payment;

import com.rupeek.hotelbooking.domain.common.Money;
import com.rupeek.hotelbooking.domain.payment.ChargeRequest;
import com.rupeek.hotelbooking.domain.payment.GatewayResult;
import com.rupeek.hotelbooking.domain.payment.PaymentGateway;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Deterministic stand-in for a real payment provider. Charges are approved unless the instrument token signals
 * a decline: a card ending in 0002, or a UPI handle / wallet id containing "fail". Refunds always succeed.
 */
@Component
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public GatewayResult charge(ChargeRequest request) {
        String reference = "mock_ch_" + UUID.randomUUID();
        return shouldDecline(request.instrumentToken())
                ? GatewayResult.declined(reference, "Declined by issuer (simulated)")
                : GatewayResult.approved(reference);
    }

    @Override
    public GatewayResult refund(String chargeReference, Money amount) {
        return GatewayResult.approved("mock_rf_" + UUID.randomUUID());
    }

    private static boolean shouldDecline(String instrumentToken) {
        return instrumentToken.endsWith("0002") || instrumentToken.toLowerCase(Locale.ROOT).contains("fail");
    }
}
