package com.ntros.service.position;

import com.ntros.position.PositionRepository;
import com.ntros.exception.InsufficientAssetsException;
import com.ntros.exception.PositionNotFoundException;
import com.ntros.model.Position;
import com.ntros.model.account.Account;
import com.ntros.model.order.Side;
import com.ntros.model.product.Product;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;

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
    public Position createPosition(Position position) {
        try {
            return positionRepository.save(position);
        } catch (DataIntegrityViolationException ex) {
            log.error("Failed to create pos: {}", position);
            throw new RuntimeException("Failed to create position");
        }
    }

    @Override
    public CompletableFuture<List<Position>> getAllPositionsAsync() {
        return supplyAsync(positionRepository::findAll);
    }

    @Override
    public CompletableFuture<Position> getPositionByAccountAndProductAsync(Account account, Product product) {
        return supplyAsync(() -> getPositionByAccountAndProduct(account, product));
    }

    @Override
    public Position getPositionByAccountAndProduct(Account account, Product product) {
        return positionRepository.findOneByAccountProduct(account, product)
                .orElseThrow(() ->
                        new PositionNotFoundException(String.format("Position not found for account: %s, product: %s",
                                account.getAccountNumber(),
                                product.getIsin())));
    }

    @Override
    public CompletableFuture<Position> getPositionByAccountNumberAndProductIsinAsync(String accountNumber, String isin) {
        return supplyAsync(() -> positionRepository.findOneByAccountNumberProductIsin(accountNumber, isin)
                        .orElseThrow(() -> new PositionNotFoundException(
                                String.format("Could not find position for AN=%s, isin:%s", accountNumber, isin))),
                executor);
    }

    @Override
    public CompletableFuture<Position> getPositionByAccountAndProductIsinAsync(Account account, String isin) {
        return supplyAsync(() -> positionRepository.findOneByAccountProductIsin(account, isin)
                        .orElseThrow(() -> new PositionNotFoundException(
                                String.format("Could not find position for AN=%s, isin:%s", account, isin))),
                executor);
    }

    @Override
    public CompletableFuture<Integer> getQuantityByAccountNumberAndProductIsinAsync(String accountNumber, String isin) {
        return supplyAsync(() -> getQuantityByAccountNumberAndProductIsin(accountNumber, isin));
    }

    @Override
    public int getQuantityByAccountNumberAndProductIsin(String accountNumber, String isin) {
        return positionRepository.findQuantityByAccountNumberProductIsin(accountNumber, isin)
                .orElseThrow(() ->
                        new PositionNotFoundException(
                                String.format("Could not get position quantity for AN:%s and ISIN:%s", accountNumber, isin)));
    }

    @Override
    public CompletableFuture<Boolean> compareCurrentProductQuantityAsync(String accountNumber, String isin, int orderQuantity) {

        return supplyAsync(() -> positionRepository.compareCurrentProductQuantity(orderQuantity, accountNumber, isin)
                .orElseThrow(() -> new PositionNotFoundException("could not get position quantity")));
    }

    @Override
    public CompletableFuture<Void> updatePositionAsync(Account account, Product product, int matchedOrderQuantity, Side side) {
        return runAsync(() -> updatePosition(account, product, matchedOrderQuantity, side), executor);
    }

    @Override
    public void updatePosition(Account account, Product product, int matchedOrderQuantity, Side side) {
        Position position = getPositionByAccountAndProduct(account, product);
        if (side.equals(Side.BUY)) {
            position.setQuantity(position.getQuantity() + matchedOrderQuantity);
        } else {
            if (position.getQuantity() < matchedOrderQuantity) {
                log.info("Position: {} for account: {} on product: {} has Insufficient assets. Assets to match:{}",
                        position, account, product, matchedOrderQuantity);
                throw new InsufficientAssetsException("Insufficient assets to transfer");
            }
            position.setQuantity(position.getQuantity() - matchedOrderQuantity);

        }
        log.info("Transferred {} units of Product {} from Account {}",
                matchedOrderQuantity, product, account);
        positionRepository.save(position);
    }
}
