package com.ntros.dataservice.position;

import com.ntros.model.Position;
import com.ntros.model.account.Account;
import com.ntros.model.product.Product;

import java.util.concurrent.CompletableFuture;

public interface PositionService {

    CompletableFuture<Position> getPositionByAccountNumberAndProductIsin(String accountNumber, String isin);

    CompletableFuture<Position> getPositionByAccountAndProductIsin(Account account, String isin);

    CompletableFuture<Integer> getQuantityByAccountNumberAndProductIsin(String accountNumber, String isin);

    CompletableFuture<Boolean> compareCurrentProductQuantity(String accountNumber, String isin, int orderQuantity);

    CompletableFuture<Void> updatePosition(Account account, Product product, int matchedOrderQuantity);

}
