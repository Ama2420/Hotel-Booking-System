package com.rupeek.hotelbooking.domain.payment;

public record GatewayResult(boolean successful, String reference, String failureReason) {

    public static GatewayResult approved(String reference) {
        return new GatewayResult(true, reference, null);
    }

    public static GatewayResult declined(String reference, String reason) {
        return new GatewayResult(false, reference, reason);
    }
}
