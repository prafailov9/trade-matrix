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
        HANDLERS = Set.of(
                GenericExceptionHandler.of(NotFoundException.class, HttpStatus.NOT_FOUND, Throwable::getMessage),
                GenericExceptionHandler.of(InvalidArgumentException.class, HttpStatus.BAD_REQUEST, Throwable::getMessage),
                GenericExceptionHandler.of(DataConstraintFailureException.class, HttpStatus.INTERNAL_SERVER_ERROR, Throwable::getMessage),
                GenericExceptionHandler.of(DataAccessViolationException.class, HttpStatus.INTERNAL_SERVER_ERROR, Throwable::getMessage),
                GenericExceptionHandler.of(OrderProcessingException.class, HttpStatus.INTERNAL_SERVER_ERROR, Throwable::getMessage),
                GenericExceptionHandler.of(FailedJsonPayloadProcessingException.class, HttpStatus.INTERNAL_SERVER_ERROR, Throwable::getMessage)
                );
    }

    private RestExceptionHandlerRegistry() {

    }

    public static ResponseEntity<?> handleException(Throwable ex) {
        Optional<RestExceptionHandler> restExceptionHandler = HANDLERS.stream()
                .filter(handler -> handler.supports(ex.getClass()))
                .findFirst();
        return restExceptionHandler.isPresent()
                ? restExceptionHandler.get().handle(ex)
                : defaultErrorResponse(ex);
    }

    private static ResponseEntity<?> defaultErrorResponse(Throwable ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
    }

}
