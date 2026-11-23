package com.rupeek.hotelbooking.domain.common;

public class BusinessRuleViolationException extends DomainException {

    public BusinessRuleViolationException(String message) {
        super(message);
    }
}
