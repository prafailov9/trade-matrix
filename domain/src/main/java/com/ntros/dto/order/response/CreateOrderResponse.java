package com.ntros.dto.order.response;

import com.ntros.dto.order.OrderDTO;
import com.ntros.dto.order.request.CreateOrderRequest;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResponse extends OrderResponse {

    private OrderDTO orderDTO;

}
