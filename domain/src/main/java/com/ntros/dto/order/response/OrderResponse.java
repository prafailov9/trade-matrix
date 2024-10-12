package com.ntros.dto.order.response;

import lombok.Data;

@Data
public abstract class OrderResponse {
    protected Status status;
    protected String message;

}
