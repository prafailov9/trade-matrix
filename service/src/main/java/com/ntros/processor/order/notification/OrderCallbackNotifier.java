package com.ntros.processor.order.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntros.dto.order.request.OrderRequest;
import com.ntros.dto.order.response.CreateOrderResponse;
import com.ntros.dto.order.response.OrderResponse;
import com.ntros.exception.FailedJsonPayloadProcessingException;
import com.ntros.model.order.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.lang.String.format;
import static java.net.http.HttpClient.newHttpClient;

@Service
@Slf4j
public class OrderNotifier implements Notifier<CreateOrderResponse> {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public OrderNotifier(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void notify(CreateOrderResponse orderResponse, String callbackUrl) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(callbackUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(buildCallbackPayload(orderResponse)))
                .build();

        String orderIdentifier = format("%s_%s_%s", orderResponse.getOrderDTO().getAccountNumber(),
                orderResponse.getOrderDTO().getProductIsin(), orderResponse.getOrderDTO().getMarketCode());

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        log.info("Callback notification sent successfully for order {}", orderIdentifier);
                    } else {
                        log.error("Failed to notify callback for order {}. Response code: {}", orderIdentifier, response.statusCode());
                    }
                }).exceptionally(ex -> {
                    log.error("Error while sending callback notification for order {}", orderIdentifier, ex);
                    return null;
                });
    }

    private String buildCallbackPayload(CreateOrderResponse orderResponse) {
        try {
            return objectMapper.writeValueAsString(orderResponse);
        } catch (JsonProcessingException ex) {
            String err = String.format("Could not convert order response [%s] to json %s", orderResponse, ex.getMessage());
            log.error(err, ex);
            throw new FailedJsonPayloadProcessingException(err);
        }
    }

}
