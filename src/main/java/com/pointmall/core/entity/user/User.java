package com.pointmall.core.entity.user;

import com.pointmall.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    private String phone;

    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private Long pointBalance = 0L;

    @Builder // 클래스 위가 아닌 생성자 위에 붙이는 것이 더 안전합니다.
    private User(String name, String email, long pointBalance) {
        this.name = name;
        this.email = email;
        this.pointBalance = pointBalance;
        this.status = Status.ACTIVE;
    }

    public void usePoint(Long point) {
        Long currentPoint = this.pointBalance - point;
        if(point > 0) {
            this.pointBalance = currentPoint;
        }else {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }
    }

    public static User createUser(String name, String email, Long pointBalance) {
       return User.builder()
               .name(name)
               .email(email)
               .pointBalance(pointBalance)
               .build();
    }
}
