package com.ntros.dto.order.request;

import com.ntros.validation.OrderTypeSupported;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@OrderTypeSupported
public abstract class OrderRequest {
    private String orderType; // buy or sell

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


}
