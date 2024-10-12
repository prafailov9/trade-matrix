package com.ntros.model.product;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@RequiredArgsConstructor
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer regionId;

    @Column(name = "region_name", nullable = false, unique = true, length = 50)
    private String regionName;
}
