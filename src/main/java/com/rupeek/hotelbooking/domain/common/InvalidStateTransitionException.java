package com.rupeek.hotelbooking.domain.common;

public class InvalidStateTransitionException extends DomainException {

    public InvalidStateTransitionException(String entity, String id, Enum<?> from, Enum<?> to) {
        super(String.format("%s '%s' cannot move from %s to %s", entity, id, from, to));
    }
}
