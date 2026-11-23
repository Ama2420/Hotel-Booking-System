package com.rupeek.hotelbooking.infrastructure.concurrency;

import com.rupeek.hotelbooking.application.LockManager;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

/**
 * One reentrant lock per key. Locks are never evicted, which mirrors the in-memory stores (they also only grow);
 * a persistent deployment would use row locks or a distributed lock instead.
 */
@Component
public class InMemoryLockManager implements LockManager {

    private final ConcurrentMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    @Override
    public <T> T withLock(String key, Supplier<T> action) {
        ReentrantLock lock = locks.computeIfAbsent(key, ignored -> new ReentrantLock());
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }
}
