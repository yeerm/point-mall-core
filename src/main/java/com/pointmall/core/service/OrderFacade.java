package com.pointmall.core.service;

import com.pointmall.core.dto.order.OrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderFacade {
    private final OrderService orderService;

    public Long createOrder(Long userId, OrderRequest request) throws InterruptedException {
        int maxRetry = 100;
        int countRetry = 0;
        while(countRetry < maxRetry) {
            try {
                return orderService.createOrder(userId, request);
            } catch (OptimisticLockingFailureException e){
                // 낙관적 락 충돌 시 50초 이후에 재시도
                log.info("주문 재시도 횟수: {}", countRetry);
                Thread.sleep(50);
                countRetry++;
            } catch (IllegalArgumentException e) {
                // 재고 없는 경우는 바로 예외 터트림
                throw e;
            } catch (Exception e) {
                // 그 외 예외처리
                log.error("주문 재시도 발생: {}", e.getMessage());
                throw e;
            }
        }
        throw new RuntimeException("최대 재시도 횟수를 초과했습니다.");
    }
}
