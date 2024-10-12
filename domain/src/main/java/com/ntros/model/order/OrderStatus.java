package com.ntros.model.order;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class OrderStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderStatusId;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "current_status", nullable = false, length = 32)
    private String currentStatus;

    @Column(name = "updated_date")
    private OffsetDateTime updatedDate;
}
