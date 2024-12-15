package com.ntros.controller;

import com.ntros.converter.PositionConverter;
import com.ntros.service.position.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@RestController
@RequestMapping("api/pos")
public class PositionController extends AbstractApiController {

    private final PositionService positionService;
    private final PositionConverter positionConverter;

    @Autowired
    public PositionController(PositionService positionService, PositionConverter positionConverter) {
        this.positionService = positionService;
        this.positionConverter = positionConverter;
    }

    @GetMapping("/all")
    public CompletableFuture<ResponseEntity<?>> getAllPositions() {
        return positionService.getAllPositionsAsync()
                .thenApplyAsync(positions -> positions.stream()
                        .map(positionConverter::toDTO)
                        .collect(Collectors.toList()))
                .handleAsync(this::handleResponseAsync);
    }

}
