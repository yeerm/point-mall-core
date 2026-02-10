package com.pointmall.core.entity.point_history;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Type {
    EARNED("적림"),
    USED("포인트 사용"),
    CANCELLED("주문 취소");
    private final String description;
}
