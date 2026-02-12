package com.pointmall.core.service;

import com.pointmall.core.dto.order.OrderRequest;
import com.pointmall.core.dto.order.OrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderFacade {
    private final OrderService orderService;
//    private final int MAX_WAIT_TIME = 1000;

    public OrderResult createOrder(Long userId, OrderRequest request, AtomicInteger deadlockCounter) throws InterruptedException {
        int maxRetry = 10;
        int retryCount = 0;
        long waitTime = 50;
        long maxWaitTime = 1000 ;
        while(retryCount < maxRetry) {
            retryCount++;
            try {
                Long orderId = orderService.createOrder(userId, request);
                if (retryCount > 0) {
                    log.info("재시도 {}회 주문 성공, 사용자 ID: {}",retryCount, userId);
                }
                return new OrderResult(orderId, retryCount);
            } catch (CannotAcquireLockException e) { // DB 데드락 및 락 획득 실패
                deadlockCounter.incrementAndGet(); // 데드락 카운트 증가
                applyBackoff(waitTime);
                waitTime = Math.min(waitTime * 2, maxWaitTime);
            } catch (OptimisticLockingFailureException e){
                // 낙관적 락 충돌 시 50초 이후에 재시도
//                log.info("주문 재시도 횟수: {}", retryCount);
                applyBackoff(waitTime);
                waitTime = Math.min(waitTime * 2, maxWaitTime);
            } catch (Exception e) {
                // 그 외 예외처리
                log.error("재시도 실패 : {}", e.getMessage());
                throw e;
            }
        }
        throw new RuntimeException("최대 재시도 횟수를 초과했습니다.");
    }

    // jitter 추가해서 분산 -> 효과 없음
    private void applyBackoff(long waitTime) throws InterruptedException {
        long jitter = (long) (Math.random() * 50);
        Thread.sleep(jitter + waitTime);
    }
}
