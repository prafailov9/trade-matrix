package com.ntros.cache;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class LockingUtil {

    public static <T> T runSafe(ReentrantLock lock, Supplier<T> supplier) {
        lock.lock();
        try {
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }

    public static void runSafe(ReentrantLock lock, Runnable codeBlock) {
        lock.lock();
        try {
            codeBlock.run(); // Execute the passed lambda
        } finally {
            lock.unlock();
        }
    }
}
