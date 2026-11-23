package com.rupeek.hotelbooking.domain.cancellation;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CancellationPolicyRegistry {

    private final Map<String, CancellationPolicy> policiesByCode;

    public CancellationPolicyRegistry(List<CancellationPolicy> policies) {
        this.policiesByCode = policies.stream().collect(Collectors.toUnmodifiableMap(
                CancellationPolicy::code,
                Function.identity(),
                (first, second) -> {
                    throw new IllegalStateException("duplicate cancellation policy code: " + first.code());
                }));
    }

    public boolean supports(String code) {
        return policiesByCode.containsKey(code);
    }

    public CancellationPolicy forCode(String code) {
        CancellationPolicy policy = policiesByCode.get(code);
        if (policy == null) {
            throw new IllegalStateException("no cancellation policy registered for code " + code);
        }
        return policy;
    }
}
