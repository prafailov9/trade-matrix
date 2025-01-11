package com.ntros.processor.order.notification;

import com.ntros.model.order.Order;

public interface Notifier<T> {
    void notify(T obj, String callbackUrl);

}
