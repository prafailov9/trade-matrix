package com.ntros.processor.order.notification;

public interface CallbackNotifier<T> {
    void notifyCallback(T obj, String callbackUrl);

}
