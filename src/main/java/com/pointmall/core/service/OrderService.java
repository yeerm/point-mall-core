package com.pointmall.core.service;

import com.pointmall.core.dto.order.OrderRequest;
import com.pointmall.core.entity.order.Order;
import com.pointmall.core.entity.order_item.OrderItem;
import com.pointmall.core.entity.point_history.PointHistory;
import com.pointmall.core.entity.product.Product;
import com.pointmall.core.entity.user.User;
import com.pointmall.core.repository.OrderRepository;
import com.pointmall.core.repository.PointHistoryRepository;
import com.pointmall.core.repository.ProductRepository;
import com.pointmall.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PointHistoryRepository pointHistoryRepository;

    // 1. 조회
    // 2. 검증 및 차감
    // 3. 이력 생성(PointHistory)
    // 4. 주문 생성(Order, OrderItem)
    @Transactional
    public Long createOrder(Long userId, OrderRequest request) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 주문 리스트 생성
        List<OrderItem> orderItems = request.getItems().stream().map(
                item -> {
                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

                    // 재고차감
                    product.removeStock(item.getQuantity());

                    return OrderItem.createOrderItem(product, (long)item.getQuantity());
                }
        ).collect(Collectors.toList());

        // 총금액 계산
        long totalAmount = orderItems.stream()
                .mapToLong(OrderItem::getLineAmount)
                .sum();

        // 사용자 포인트 차감
        user.usePoint(totalAmount);

        // 주문생성
        Order order = Order.createOrder(user, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase(), orderItems);

        // 이력생성
        PointHistory pointHistory = PointHistory.createHistory(user, "USED", totalAmount, user.getPointBalance(), "상품 " + orderItems.size() + "건 구매");

        orderRepository.save(order);
        pointHistoryRepository.save(pointHistory);

        return order.getId();
    }
}
