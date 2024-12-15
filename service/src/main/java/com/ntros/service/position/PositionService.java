package com.ntros.service.position;

import com.ntros.model.Position;
import com.ntros.model.account.Account;
import com.ntros.model.order.Side;
import com.ntros.model.product.Product;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PositionService {

    Position createPosition(Position position);

    CompletableFuture<List<Position>> getAllPositionsAsync();
    CompletableFuture<Position> getPositionByAccountAndProductAsync(Account account, Product product);
    Position getPositionByAccountAndProduct(Account account, Product product);
    CompletableFuture<Position> getPositionByAccountNumberAndProductIsinAsync(String accountNumber, String isin);

    CompletableFuture<Position> getPositionByAccountAndProductIsinAsync(Account account, String isin);

    CompletableFuture<Integer> getQuantityByAccountNumberAndProductIsinAsync(String accountNumber, String isin);
    int getQuantityByAccountNumberAndProductIsin(String accountNumber, String isin);

    CompletableFuture<Boolean> compareCurrentProductQuantityAsync(String accountNumber, String isin, int orderQuantity);

    CompletableFuture<Void> updatePositionAsync(Account account, Product product, int matchedOrderQuantity, Side side);
    void updatePosition(Account account, Product product, int matchedOrderQuantity, Side side);

}
