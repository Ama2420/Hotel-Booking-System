package com.rupeek.hotelbooking.domain.common;

/**
 * Base type for business-rule failures. Deliberately carries no transport concerns (e.g. HTTP status);
 * the API layer decides how each subtype is presented.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
