package com.pointmall.core.entity.product;

import com.pointmall.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(unique  = true, nullable = false, length = 50)
    private String txId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    private Integer stockQuantity  = 0;

    @Version
    private Integer version;

    @Builder
    private Product(String name, Long price, Integer stockQuantity) {
        this.name = name;
        this.txId = UUID.randomUUID().toString();
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.status = Status.ACTIVE;
        this.version = 0;
    }

    public void removeStock(int quantity) {
        int currentStock = this.stockQuantity - quantity;
        if(currentStock >= 0) {
            this.stockQuantity = currentStock;
        }else {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }
    }
    public static Product createProduct(String name, Long price, Integer stockQuantity) {
        return Product.builder()
                .name(name)
                .price(price)
                .stockQuantity(stockQuantity)
                .build();
    }
}
