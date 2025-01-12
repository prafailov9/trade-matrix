package com.ntros.handler;

import com.ntros.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

final public class RestExceptionHandlerRegistry {

    private static final Set<RestExceptionHandler> HANDLERS;

    // register all handlers
    static {
        HANDLERS = Set.of(GenericExceptionHandler.of(WalletCreateFailedException.class, HttpStatus.INTERNAL_SERVER_ERROR, Throwable::getMessage),
                GenericExceptionHandler.of(WalletCreateFailedException.class, HttpStatus.INTERNAL_SERVER_ERROR, Throwable::getMessage),
                GenericExceptionHandler.of(WalletNotFoundException.class, HttpStatus.NOT_FOUND, Throwable::getMessage),
                GenericExceptionHandler.of(WalletNotFoundForAccountException.class, HttpStatus.NOT_FOUND, Throwable::getMessage),
                GenericExceptionHandler.of(WalletNotFoundForCurrencyAndAccountException.class, HttpStatus.NOT_FOUND, Throwable::getMessage),
                GenericExceptionHandler.of(WalletDeleteFailedException.class, HttpStatus.INTERNAL_SERVER_ERROR, Throwable::getMessage),
                GenericExceptionHandler.of(AccountConstraintFailureException.class, HttpStatus.INTERNAL_SERVER_ERROR, Throwable::getMessage),
                GenericExceptionHandler.of(AccountNotFoundException.class, HttpStatus.NOT_FOUND, Throwable::getMessage),
                GenericExceptionHandler.of(CurrencyNotFoundException.class, HttpStatus.NOT_FOUND, Throwable::getMessage),
                GenericExceptionHandler.of(FailedToActivateAllCurrenciesException.class, HttpStatus.INTERNAL_SERVER_ERROR, Throwable::getMessage),
                GenericExceptionHandler.of(CurrencyInactiveStateException.class, HttpStatus.BAD_REQUEST, Throwable::getMessage),
                GenericExceptionHandler.of(AddressNotFoundException.class, HttpStatus.NOT_FOUND, Throwable::getMessage),
                GenericExceptionHandler.of(AddressHashingFailedException.class, HttpStatus.INTERNAL_SERVER_ERROR, Throwable::getMessage),
                GenericExceptionHandler.of(AddressConstraintFailureException.class, HttpStatus.INTERNAL_SERVER_ERROR, Throwable::getMessage),
                GenericExceptionHandler.of(TransferProcessingFailedException.class, HttpStatus.INTERNAL_SERVER_ERROR, Throwable::getMessage),
                GenericExceptionHandler.of(DataConstraintViolationException.class, HttpStatus.INTERNAL_SERVER_ERROR, Throwable::getMessage),
                GenericExceptionHandler.of(WalletUpdateFailedException.class, HttpStatus.INTERNAL_SERVER_ERROR, Throwable::getMessage),
                GenericExceptionHandler.of(InvalidTransferRequestException.class, HttpStatus.BAD_REQUEST, Throwable::getMessage),
                GenericExceptionHandler.of(WalletNotFoundForANException.class, HttpStatus.NOT_FOUND, Throwable::getMessage),
                GenericExceptionHandler.of(NoMainWalletException.class, HttpStatus.INTERNAL_SERVER_ERROR, Throwable::getMessage),
                GenericExceptionHandler.of(InsufficientFundsException.class, HttpStatus.BAD_REQUEST, Throwable::getMessage),
                GenericExceptionHandler.of(CurrencyNotSupportedException.class, HttpStatus.BAD_REQUEST, Throwable::getMessage),
                GenericExceptionHandler.of(ExchangeRateNotFoundForPairException.class, HttpStatus.NOT_FOUND, Throwable::getMessage),
                GenericExceptionHandler.of(InvalidDecimalAmountException.class, HttpStatus.INTERNAL_SERVER_ERROR, Throwable::getMessage),
                GenericExceptionHandler.of(CardNotFoundException.class, HttpStatus.NOT_FOUND, Throwable::getMessage),
                GenericExceptionHandler.of(CannotRefreshCardException.class, HttpStatus.INTERNAL_SERVER_ERROR, Throwable::getMessage),
                GenericExceptionHandler.of(CancelOrderFailedException.class, HttpStatus.INTERNAL_SERVER_ERROR, Throwable::getMessage),
                GenericExceptionHandler.of(CreateOrderFailedException.class, HttpStatus.INTERNAL_SERVER_ERROR, Throwable::getMessage),
                GenericExceptionHandler.of(InsufficientAssetsException.class, HttpStatus.BAD_REQUEST, Throwable::getMessage),
                GenericExceptionHandler.of(MarketPriceNotFoundException.class, HttpStatus.NOT_FOUND, Throwable::getMessage),
                GenericExceptionHandler.of(OrderConstraintViolationException.class, HttpStatus.INTERNAL_SERVER_ERROR, Throwable::getMessage),
                GenericExceptionHandler.of(OrderStatusCreateFailedException.class, HttpStatus.INTERNAL_SERVER_ERROR, Throwable::getMessage),
                GenericExceptionHandler.of(OrderStatusNotFoundException.class, HttpStatus.NOT_FOUND, Throwable::getMessage),
                GenericExceptionHandler.of(OrderStatusUpdateFailedException.class, HttpStatus.INTERNAL_SERVER_ERROR, Throwable::getMessage),
                GenericExceptionHandler.of(OrderTypeNotFoundException.class, HttpStatus.NOT_FOUND, Throwable::getMessage),
                GenericExceptionHandler.of(OrderNotFoundException.class, HttpStatus.NOT_FOUND, Throwable::getMessage),
                GenericExceptionHandler.of(OrderProcessingException.class, HttpStatus.INTERNAL_SERVER_ERROR, Throwable::getMessage),
                GenericExceptionHandler.of(PositionNotFoundException.class, HttpStatus.NOT_FOUND, Throwable::getMessage),
                GenericExceptionHandler.of(FailedOrdersDeleteException.class, HttpStatus.INTERNAL_SERVER_ERROR, Throwable::getMessage),
                GenericExceptionHandler.of(ProductNotFoundException.class, HttpStatus.NOT_FOUND, Throwable::getMessage),
                GenericExceptionHandler.of(RetryLimitExceededException.class, HttpStatus.INTERNAL_SERVER_ERROR, Throwable::getMessage),
                GenericExceptionHandler.of(NotFoundException.class, HttpStatus.NOT_FOUND, Throwable::getMessage),
                GenericExceptionHandler.of(TransferFundsAndAssetsException.class, HttpStatus.BAD_REQUEST, Throwable::getMessage),
                GenericExceptionHandler.of(FailedJsonPayloadProcessingException.class, HttpStatus.INTERNAL_SERVER_ERROR, Throwable::getMessage)
        );

    }

    private RestExceptionHandlerRegistry() {

    }

    public static ResponseEntity<?> handleException(Throwable ex) {
        Optional<RestExceptionHandler> restExceptionHandler = HANDLERS.stream()
                .filter(handler -> handler.supports(ex.getClass()))
                .findFirst();
        return restExceptionHandler.isPresent() ? restExceptionHandler.get().handle(ex) : defaultErrorResponse(ex);
    }

    private static ResponseEntity<?> defaultErrorResponse(Throwable ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
    }

}
