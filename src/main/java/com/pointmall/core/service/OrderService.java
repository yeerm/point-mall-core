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
import com.pointmall.core.entity.point_history.Type;
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
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<OrderRequest.OrderItemDetail> itemRequests = request.getItems();

        // 2. 상품 정보 일괄 조회
        List<Product> products = itemRequests.stream()
                .map(req -> productRepository.findById(req.getProductId())
                        .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다.: " + req.getProductId())))
                .collect(Collectors.toList());

        // 3. 총 주문 금액 계산 및 재고 유효성 검증
        long totalAmount = 0;
        for (int i = 0; i < itemRequests.size(); i++) {
            Product product = products.get(i);
            int quantity = itemRequests.get(i).getQuantity();
            if (product.getStockQuantity() < quantity) {
                throw new IllegalArgumentException("재고가 부족합니다. 상품 ID: " + product.getId());
            }
            totalAmount += product.getPrice() * quantity;
        }

        // 4. 사용자 포인트 확인 및 차감
        user.usePoint(totalAmount);

        // 5. 주문 항목 생성 및 실제 재고 차감
        List<OrderItem> orderItems = new java.util.ArrayList<>();
        for (int i = 0; i < itemRequests.size(); i++) {
            Product product = products.get(i);
            int quantity = itemRequests.get(i).getQuantity();
            product.removeStock(quantity);
            orderItems.add(OrderItem.createOrderItem(product, (long) quantity));
        }

        // 6. 주문 생성
        Order order = Order.createOrder(user, generateOrderNumber(), orderItems);

        // 7. 포인트 사용 이력 생성
        PointHistory pointHistory = PointHistory.createHistory(user, Type.USED, totalAmount, user.getPointBalance(), "상품 " + orderItems.size() + "건 구매");

        orderRepository.save(order);
        pointHistoryRepository.save(pointHistory);

        return order.getId();
    }

    // 주문번호 생성
    private String generateOrderNumber() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
