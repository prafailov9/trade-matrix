package com.ntros.order;

import com.ntros.model.order.CurrentOrderStatus;
import com.ntros.model.order.Order;
import com.ntros.model.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderStatusRepository extends JpaRepository<OrderStatus, Integer> {

    @Query(value = "SELECT os FROM OrderStatus os " +
            "WHERE os.order = :order")
    Optional<OrderStatus> findOneByOrder(@Param("order") Order order);

    @Query(value = "SELECT os FROM OrderStatus os " +
            "WHERE os.order = :order AND os.currentStatus = :currentStatus")
    Optional<OrderStatus> findOneByOrderCurrentStatus(@Param("order") Order order,
                                                      @Param("currentStatus") CurrentOrderStatus currentStatus);

}
