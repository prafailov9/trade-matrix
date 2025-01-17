package com.ntros.model.order;

import com.ntros.model.product.MarketProduct;
import com.ntros.model.wallet.Wallet;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "`order`")
@Data
@Builder
@EqualsAndHashCode
@ToString(exclude = {"wallet", "marketProduct", "orderStatuses"})
@RequiredArgsConstructor
@AllArgsConstructor
public class Order { // TODO: Add expiryDate field for TTL in cache
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;

    @ManyToOne
    @JoinColumn(name = "order_type_id", nullable = false)
    private OrderType orderType;

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @ManyToOne
    @JoinColumn(name = "market_product_id", nullable = false)
    private MarketProduct marketProduct;

    @OneToMany(mappedBy = "order", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    private List<OrderStatus> orderStatuses;

    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false)
    private Side side;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "price", precision = 10, nullable = false)
    private BigDecimal price;

    @Column(name = "placed_at", updatable = false)
    private OffsetDateTime placedAt;

    @Column(name = "filled_quantity", nullable = false)
    private int filledQuantity;

    @Column(name = "remaining_quantity", nullable = false)
    private int remainingQuantity;

    // optimistic lock mechanism
    @Version
    private Long version;

    public void adjustQuantity(int matchedQuantity) {
        filledQuantity += matchedQuantity;
        remainingQuantity -= matchedQuantity;
    }

    public OrderStatus getCurrentOrderStatus() {
        return (orderStatuses != null && !orderStatuses.isEmpty())
                ? orderStatuses.get(orderStatuses.size() - 1)
                : null;
    }

    public String market() {
        return marketProduct.getMarket().getMarketCode();
    }
    public String isin() {
        return marketProduct.getProduct().getIsin();
    }

    public String priceString() {
        return String.format("%010.2f", price); // Converts price to a zero-padded String
    }
}
