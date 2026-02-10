package com.pointmall.core.entity.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {
    ACTIVE("활성화"),
    BLOCKED("차단"),
    WITHDRAWN("탈퇴");
    private final String description;
}
