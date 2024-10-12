package com.ntros.model.product;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@RequiredArgsConstructor
public class Sector {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer sectorId;

    @Column(name = "sector_name", nullable = false, unique = true, length = 50)
    private String sectorName;
}
