package com.ntros.model.order;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_type")
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
public class OrderType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderTypeId;

    @Column(name = "order_type_name", nullable = false, length = 32, unique = true)
    private String orderTypeName;
}
