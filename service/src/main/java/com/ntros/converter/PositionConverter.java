package com.ntros.converter;

import com.ntros.dto.PositionDTO;
import com.ntros.model.Position;
import org.springframework.stereotype.Component;

@Component
public class PositionConverter implements Converter<PositionDTO, Position> {
    @Override
    public PositionDTO toDTO(Position model) {
        PositionDTO dto = new PositionDTO();
        dto.setAccName(model.getPortfolio().getAccount().getAccountName());
        dto.setAccNum(model.getPortfolio().getAccount().getAccountNumber());
        dto.setPortName(model.getPortfolio().getPortfolioName());
        dto.setQuantity(model.getQuantity());
        dto.setProdName(model.getProduct().getProductName());
        dto.setProdIsin(model.getProduct().getIsin());

        return dto;
    }

    @Override
    public Position toModel(PositionDTO dto) {
        return null;
    }
}
