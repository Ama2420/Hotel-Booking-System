package com.rupeek.hotelbooking.domain.payment;

import com.rupeek.hotelbooking.domain.common.BusinessRuleViolationException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PaymentProcessorRegistry {

    private final Map<PaymentMethod, PaymentProcessor> processorsByMethod;

    public PaymentProcessorRegistry(List<PaymentProcessor> processors) {
        this.processorsByMethod = processors.stream().collect(Collectors.toUnmodifiableMap(
                PaymentProcessor::method,
                Function.identity(),
                (first, second) -> {
                    throw new IllegalStateException("duplicate payment processor for " + first.method());
                }));
    }

    public PaymentProcessor processorFor(PaymentMethod method) {
        PaymentProcessor processor = processorsByMethod.get(method);
        if (processor == null) {
            throw new BusinessRuleViolationException("Payment method " + method + " is not supported");
        }
        return processor;
    }
}
