package com.ntros.order;

import com.ntros.model.order.OrderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderTypeRepository extends JpaRepository<OrderType, Integer> {

    Optional<OrderType> findOneByOrderTypeName(String orderTypeName);

}
