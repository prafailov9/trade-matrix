package com.ntros.dataservice.position;

import com.ntros.PositionRepository;
import com.ntros.exception.InsufficientAssetsException;
import com.ntros.exception.PositionNotFoundException;
import com.ntros.model.Position;
import com.ntros.model.account.Account;
import com.ntros.model.product.Product;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
@Transactional
public class PositionDataService implements PositionService {

    private final Executor executor;

    private final PositionRepository positionRepository;


    @Autowired
    public PositionDataService(Executor executor, PositionRepository positionRepository) {
        this.executor = executor;
        this.positionRepository = positionRepository;
    }


    @Override
    public CompletableFuture<Position> getPositionByAccountNumberAndProductIsin(String accountNumber, String isin) {
        return CompletableFuture
                .supplyAsync(() -> positionRepository.findOneByAccountNumberProductIsin(accountNumber, isin)
                                .orElseThrow(() -> new PositionNotFoundException(
                                        String.format("Could not find position for AN=%s, isin:%s", accountNumber, isin))),
                        executor);
    }

    @Override
    public CompletableFuture<Position> getPositionByAccountAndProductIsin(Account account, String isin) {
        return CompletableFuture
                .supplyAsync(() -> positionRepository.findOneByAccountProductIsin(account, isin)
                                .orElseThrow(() -> new PositionNotFoundException(
                                        String.format("Could not find position for AN=%s, isin:%s", account, isin))),
                        executor);
    }

    @Override
    public CompletableFuture<Integer> getQuantityByAccountNumberAndProductIsin(String accountNumber, String isin) {
        return CompletableFuture.supplyAsync(() -> positionRepository.findQuantityByAccountNumberProductIsin(accountNumber, isin)
                .orElseThrow(() -> new PositionNotFoundException("could not get position quantity")));
    }

    @Override
    public CompletableFuture<Boolean> compareCurrentProductQuantity(String accountNumber, String isin, int orderQuantity) {

        return CompletableFuture.supplyAsync(() -> positionRepository.compareCurrentProductQuantity(orderQuantity, accountNumber, isin)
                .orElseThrow(() -> new PositionNotFoundException("could not get position quantity")));
    }

    @Override
    public CompletableFuture<Void> updatePosition(Account account, Product product, int matchedOrderQuantity) {
        return getPositionByAccountAndProductIsin(account, product.getIsin())
                .thenAcceptAsync(position -> {
                    if (position.getQuantity() < matchedOrderQuantity) {
                        throw new InsufficientAssetsException("Insufficient assets to transfer");
                    }

                    position.setQuantity(position.getQuantity() - matchedOrderQuantity);
                    position.setQuantity(matchedOrderQuantity);
                    log.info("Transferred {} units of Product {} from Account {}", matchedOrderQuantity, product.getProductName(), account.getAccountNumber());
                    positionRepository.save(position);
                }, executor);
    }
}
