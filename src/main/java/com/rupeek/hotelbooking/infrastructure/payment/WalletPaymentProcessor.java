package com.rupeek.hotelbooking.infrastructure.payment;

import com.rupeek.hotelbooking.domain.payment.PaymentGateway;
import com.rupeek.hotelbooking.domain.payment.PaymentMethod;
import com.rupeek.hotelbooking.domain.payment.WalletInstrument;
import org.springframework.stereotype.Component;

@Component
public class WalletPaymentProcessor extends GatewayPaymentProcessor<WalletInstrument> {

    public WalletPaymentProcessor(PaymentGateway gateway) {
        super(gateway, WalletInstrument.class);
    }

    @Override
    public PaymentMethod method() {
        return PaymentMethod.WALLET;
    }

    @Override
    protected String tokenize(WalletInstrument wallet) {
        return "wallet_" + wallet.provider() + "_" + wallet.walletId();
    }
}
