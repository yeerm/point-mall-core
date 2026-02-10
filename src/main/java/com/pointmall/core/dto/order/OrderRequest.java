package com.pointmall.core.dto.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private List<OrderItemDetail> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDetail {
        private Long productId;
        private int quantity;
    }
}
