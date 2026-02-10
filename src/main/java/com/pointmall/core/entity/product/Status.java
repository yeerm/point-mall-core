package com.pointmall.core.entity.product;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {
    ACTIVE("판매중"),
    OUT_OF_STOCK("품절"),
    DISCONTINUED("단종");
    private final String description;
}
