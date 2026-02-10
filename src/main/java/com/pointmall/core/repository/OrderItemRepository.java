package com.pointmall.core.repository;

import com.pointmall.core.entity.order_item.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository <OrderItem, Long> {
}
