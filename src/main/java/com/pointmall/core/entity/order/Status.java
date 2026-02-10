package com.pointmall.core.entity.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {
    CREATED("생성"),
    PAID("결제"),
    CANCELLED("취소"),
    COMPLETED("완료");
    private final String description;
}
