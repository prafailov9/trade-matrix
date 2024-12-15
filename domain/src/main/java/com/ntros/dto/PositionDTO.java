package com.ntros.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PositionDTO {

    // portfol
    private int quantity;

    private String portName;
    private String accName;
    private String accNum;
    private String prodName;
    private String prodIsin;




}
