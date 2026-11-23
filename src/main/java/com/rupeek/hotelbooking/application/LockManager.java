package com.rupeek.hotelbooking.application;

import java.util.function.Supplier;

/**
 * Mutual exclusion keyed by a business identifier (a booking, a room type's inventory).
 *
 * <p>Lock ordering, to stay deadlock-free: a booking lock may be taken before an inventory lock, never after.
 * The in-memory implementation is single-node; a multi-node deployment would back this with database row locks
 * or a distributed lock.
 */
public interface LockManager {

    <T> T withLock(String key, Supplier<T> action);

    default void runWithLock(String key, Runnable action) {
        withLock(key, () -> {
            action.run();
            return null;
        });
    }
}
