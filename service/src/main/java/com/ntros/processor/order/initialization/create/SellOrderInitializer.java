package com.ntros.processor.order.initialization.create;

import com.ntros.dto.order.request.CreateOrderRequest;
import com.ntros.exception.InvalidArgumentException;
import com.ntros.service.position.PositionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.lang.String.format;

@Service("sell")
@Slf4j
public class SellOrderInitializer extends AbstractCreateOrderInitializer {


    private final PositionService positionService;

    @Autowired
    public SellOrderInitializer(PositionService positionService) {
        this.positionService = positionService;
    }

    @Override
    protected void validateOrderRequest(CreateOrderRequest request) {
        int positionQuantity = positionService.getQuantityByAccountNumberAndProductIsin(request.getAccountNumber(), request.getProductIsin());
        if (positionQuantity < request.getQuantity()) {
            throw InvalidArgumentException.with(
                    format("Not enough assets to sell. Requested sells=%s, position quantity=%s",
                            request.getQuantity(), positionQuantity));
        }
    }

}
