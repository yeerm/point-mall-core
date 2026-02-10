package com.pointmall.core.service;

import com.pointmall.core.dto.order.OrderRequest;
import com.pointmall.core.entity.order.Order;
import com.pointmall.core.entity.point_history.PointHistory;
import com.pointmall.core.entity.product.Product;
import com.pointmall.core.entity.user.User;
import com.pointmall.core.repository.OrderRepository;
import com.pointmall.core.repository.PointHistoryRepository;
import com.pointmall.core.repository.ProductRepository;
import com.pointmall.core.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class OrderServiceTest {
    @Autowired
    OrderService orderService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    PointHistoryRepository pointHistoryRepository;

    @Test
    @DisplayName("다중 상품 주문 시 재고와 포인트가 정확히 차감되어야 한다")
    void createOrder_success() {
        // 1. Given: 테스트 데이터 준비
        User user = userRepository.save(User.createUser("김철수", "chulsoo@test.com", 100000L)); // 10만 포인트 보유

        Product laptop = productRepository.save(Product.createProduct("노트북",50000L, 10)); // 5만 원, 재고 10개
        Product mouse = productRepository.save(Product.createProduct("마우스", 10000L, 20));  // 1만 원, 재고 20개

        // 주문 요청 객체 생성 (노트북 1개, 마우스 2개 = 총 7만 원)
        OrderRequest.OrderItemDetail item1 = new OrderRequest.OrderItemDetail(laptop.getId(), 1);
        OrderRequest.OrderItemDetail item2 = new OrderRequest.OrderItemDetail(mouse.getId(), 2);
        OrderRequest request = new OrderRequest(List.of(item1, item2));

        // 2. When: 주문 생성 로직 실행
        Long orderId = orderService.createOrder(user.getId(), request);

        // 3. Then: 결과 검증 (Assert)
        // (1) 주문 저장 확인
        Order savedOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(savedOrder.getOrderItems()).hasSize(2);


        // (2) 재고 차감 확인 (영속성 컨텍스트 초기화 후 재조회 권장)
        assertThat(laptop.getStockQuantity()).isEqualTo(9);
        assertThat(mouse.getStockQuantity()).isEqualTo(18);

        // (3) 포인트 차감 확인
        assertThat(user.getPointBalance()).isEqualTo(30000L); // 10만 - 7만

        // (4) 포인트 이력 생성 확인
        List<PointHistory> histories = pointHistoryRepository.findAll();
        assertThat(histories).isNotEmpty();
        assertThat(histories.get(0).getAmount()).isEqualTo(70000L);
    }

    @Test
    @DisplayName("포인트가 부족하면 주문에 실패하고 예외가 발생해야 한다")
    void createOrder_fail_notEnoughPoint() {
        // Given: 포인트가 부족한 유저 (1000원 보유)
        User user = userRepository.save(User.createUser("김철수", "chulsoo@test.com", 1000L));
        Product product = productRepository.save(Product.createProduct("비싼상품", 50000L, 10));


        OrderRequest request = new OrderRequest(List.of(
                new OrderRequest.OrderItemDetail(product.getId(), 1)
        ));

        System.out.println("결제전 포인트: " + user.getPointBalance());
        // When & Then: 예외가 발생하는지 확인
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(user.getId(), request);
        });
        System.out.println("결제후 포인트: " + user.getPointBalance());

        assertThat(exception.getMessage()).isEqualTo("포인트가 부족합니다.");
    }
}
