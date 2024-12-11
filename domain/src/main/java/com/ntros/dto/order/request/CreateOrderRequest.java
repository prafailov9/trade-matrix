package com.ntros.dto.order.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest extends OrderRequest {

    @NotNull
    @Min(value = 1, message = "price of order cannot be less than 1.")
    protected BigDecimal price;
    @NotNull
    @Min(value = 1, message = "quantity cannot be less than 1.")
    private int quantity;
    private int filledQuantity;
    private int remainingQuantity;

    @NotNull(message = "tx_type cannot be null.")
    private String transactionType;

}
