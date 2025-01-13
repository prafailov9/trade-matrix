package com.ntros.cache;

import com.ntros.model.order.Order;

import java.math.BigDecimal;

public interface OrderCache extends Cache<Integer, Order> {

    BigDecimal getBestBidPrice();

    BigDecimal getBestAskPrice();

    String getMarketCode();

}
