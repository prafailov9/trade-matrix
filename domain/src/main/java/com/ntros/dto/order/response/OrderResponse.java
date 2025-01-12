package com.ntros.dto.order.response;

import lombok.Data;

@Data
public abstract class OrderResponse {

    protected String name;
    protected Status status;
    protected String message;

}
