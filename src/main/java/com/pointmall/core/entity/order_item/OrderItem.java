package com.pointmall.core.entity.order_item;

import com.pointmall.core.entity.BaseTimeEntity;
import com.pointmall.core.entity.order.Order;
import com.pointmall.core.entity.product.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private String productName;

    private Long unitPrice;

    private Long quantity;

    private Long lineAmount;

    protected void setOrder(Order order) {
        this.order = order;
    }
}
