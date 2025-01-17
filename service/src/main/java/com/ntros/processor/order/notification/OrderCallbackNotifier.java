package com.ntros.processor.order.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntros.dto.order.response.CreateOrderResponse;
import com.ntros.exception.FailedJsonPayloadProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.lang.String.format;

@Component
@Slf4j
public class OrderCallbackNotifier implements CallbackNotifier<CreateOrderResponse> {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public OrderCallbackNotifier(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void notifyCallback(CreateOrderResponse orderResponse, String callbackUrl) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(callbackUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(buildCallbackPayload(orderResponse)))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        log.info("Callback notification sent successfully for order {}", orderResponse.getName());
                    } else {
                        log.error("Failed to notify callback for order {}. Response code: {}",
                                orderResponse.getName(), response.statusCode());
                    }
                }).exceptionally(ex -> {
                    log.error("Error while sending callback notification for order {}", orderResponse.getName(), ex);
                    return null;
                });
    }

    private String buildCallbackPayload(CreateOrderResponse orderResponse) {
        try {
            return objectMapper.writeValueAsString(orderResponse);
        } catch (JsonProcessingException ex) {
            String err = format("Could not convert order response [%s] to json %s", orderResponse, ex.getMessage());
            log.error(err, ex);
            throw FailedJsonPayloadProcessingException.with(err);
        }
    }

}
