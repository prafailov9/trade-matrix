package com.ntros.validation;

import com.ntros.dto.order.request.OrderRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class OrderTypeSupportedValidator implements ConstraintValidator<OrderTypeSupported, OrderRequest> {
    private static final List<String> SUPPORTED_ORDER_TYPES = List.of("MARKET", "LIMIT", "STOP", "FOK");

    @Override
    public boolean isValid(OrderRequest orderRequest, ConstraintValidatorContext context) {
        return SUPPORTED_ORDER_TYPES.stream().anyMatch(orderType -> orderType.equalsIgnoreCase(orderRequest.getOrderType()));
    }
}
