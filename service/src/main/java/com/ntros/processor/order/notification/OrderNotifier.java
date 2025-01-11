package com.ntros.processor.order.notification;

import com.ntros.model.order.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.lang.String.format;
import static java.net.http.HttpClient.newHttpClient;

@Service
@Slf4j
public class OrderNotifier implements Notifier<Order> {

    private final HttpClient client = newHttpClient();

    @Override
    public void notify(Order order, String callbackUrl) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(callbackUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(buildCallbackPayload(order)))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        log.info("Callback notification sent successfully for order {}", order.getOrderId());
                    } else {
                        log.error("Failed to notify callback for order {}. Response code: {}", order.getOrderId(), response.statusCode());
                    }
                }).exceptionally(ex -> {
                    log.error("Error while sending callback notification for order {}", order.getOrderId(), ex);
                    return null;
                });
    }

    private String buildCallbackPayload(Order order) {
        return format("{ \"orderId\": \"%s\", \"status\": \"%s\", \"filledQuantity\": \"%s\", \"remainingQuantity\": \"%s\" }",
                order.getOrderId(), "success", order.getFilledQuantity(), order.getRemainingQuantity());
    }

}
