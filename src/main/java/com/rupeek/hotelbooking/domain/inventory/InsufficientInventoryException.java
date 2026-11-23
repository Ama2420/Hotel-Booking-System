package com.rupeek.hotelbooking.domain.inventory;

import com.rupeek.hotelbooking.domain.common.DomainException;

public class InsufficientInventoryException extends DomainException {

    public InsufficientInventoryException(String roomTypeId, int requested, int available) {
        super(String.format("Room type '%s' has %d unit(s) available for the requested dates; %d requested",
                roomTypeId, Math.max(available, 0), requested));
    }
}
