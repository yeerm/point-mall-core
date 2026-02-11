package com.pointmall.core.service;

import com.pointmall.core.dto.order.OrderRequest;
import com.pointmall.core.entity.order.Order;
import com.pointmall.core.entity.point_history.PointHistory;
import com.pointmall.core.entity.product.Product;
import com.pointmall.core.entity.user.User;
import com.pointmall.core.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
//@Transactional
public class OrderServiceTest {
    Logger log = LoggerFactory.getLogger(OrderServiceTest.class);
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
    @Autowired
    OrderItemRepository orderItemRepository;
    @Autowired
    EntityManager em;


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


        // (2) 재고 차감 확인
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

        // When & Then: 예외가 발생하는지 확인

        assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(user.getId(), request);
        });

    }

    @Test
    @DisplayName("100명이 동시에 남은 재고 1개를 주문하면 1명만 성공해야 한다")
    void concurrency_order_success() throws InterruptedException {
        // 1. Given: 재고가 1개인 상품 준비
        int threadCount = 32;
        int userCount = 100;

        // 100명 사용자 생성
        List<User> users = new ArrayList<>();
        for (int i = 0; i < userCount; i++) {
            users.add(User.createUser("user" + i, "user" + i + "@test.com", 100000L));
        }
        userRepository.saveAllAndFlush(users);

        // 상품 설정
        Product product = productRepository.saveAndFlush(Product.createProduct("한정판 상품", 10000L, 1));

        // 멀티스레드
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(userCount);

        // 2. When: 100명이 동시에 주문 호출
        for (int i = 0; i < userCount; i++) {
            User user = users.get(i);
            executorService.submit(() -> {
                try{
                    OrderRequest request = new OrderRequest(List.of(
                            new OrderRequest.OrderItemDetail(product.getId(), 1)
                    ));
                    orderService.createOrder(user.getId(), request);
                } catch (Exception e) {
                    // 낙관적 락 충돌하면 여기서 예외 발생
                    log.error("주문 실패: {}", e.getMessage());
                } finally {
                  latch.countDown();
                }
            });
        }

        latch.await();

//        em.flush();
//        em.clear();

        // 3. Then: 결과 확인
        Product updatedProduct = productRepository.findById(product.getId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        log.info("version: {}", updatedProduct.getVersion());

        // 전체 주문 개수 확인
        long orderCount = orderRepository.count();

        log.info("상품 재고: {}", updatedProduct.getStockQuantity());
        log.info("성공한 주문 개수: {}", orderCount);

        // 상품 재고는 1개 -> 0개
        assertThat(updatedProduct.getStockQuantity()).isEqualTo(0);
        // 1명만 성공해야함
        assertThat(orderCount).isEqualTo(1);
    }

    @AfterEach
    void tearDown() {
        orderItemRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        pointHistoryRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }
}
