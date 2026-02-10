package com.pointmall.core.entity.order;

import com.pointmall.core.entity.BaseTimeEntity;
import com.pointmall.core.entity.order_item.OrderItem;
import com.pointmall.core.entity.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String orderNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    private Long totalAmount = 0L;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    private LocalDateTime paidAt;

    private LocalDateTime cancelledAt;

    // 주문 생성 정적 팩토리 메서드 예시
    public static Order createOrder(User user, String orderNo, List<OrderItem> items) {
        Order order = new Order();
        order.user = user;
        order.orderNo = orderNo;
        long total = 0;
        for (OrderItem item : items) {
            order.addOrderItem(item);
            total += item.getLineAmount();
        }
        order.totalAmount = total;
        order.status = Status.CREATED;
        return order;
    }

    public void addOrderItem(OrderItem item) {
        this.orderItems.add(item);
        item.setOrder(this);
    }

}
