package com.ntros.dto.order;

import com.ntros.validation.OrderTypeSupported;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@RequiredArgsConstructor
@OrderTypeSupported
public class OrderDTO {

    private String orderType; // buy or sell
    private String currentStatus;

    @Pattern(regexp = "\\d+", message = "AN must be a number.")
    @Size(min = 8, max = 12, message = "Invalid AN: must be 8 - 13 digits.")
    protected String accountNumber;

    @NotNull(message = "currency code cannot be null.")
    @NotBlank(message = "currency code cannot be empty.")
    protected String currencyCode;

    // @Pattern(regexp = "^[a-zA-Z0-9]+$\n", message = "ISIN must be a alphanumeric string.")
    @Size(min = 8, max = 12, message = "Invalid AN: must be 8 - 12 digits.")
    protected String productIsin;

    @NotNull(message = "market code cannot be null.")
    protected String marketCode;


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
