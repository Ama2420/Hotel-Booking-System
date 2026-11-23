package com.rupeek.hotelbooking.domain.payment;

import static com.rupeek.hotelbooking.domain.common.Validation.requireNonBlank;

public record WalletInstrument(String provider, String walletId) implements PaymentInstrument {

    public WalletInstrument {
        provider = requireNonBlank(provider, "provider");
        walletId = requireNonBlank(walletId, "walletId");
    }

    @Override
    public PaymentMethod method() {
        return PaymentMethod.WALLET;
    }
}
