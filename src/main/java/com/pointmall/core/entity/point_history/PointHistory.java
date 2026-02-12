package com.pointmall.core.entity.point_history;

import com.pointmall.core.entity.BaseTimeEntity;
import com.pointmall.core.entity.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistory extends BaseTimeEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type;

    private Long amount;

    private Long balanceAfter;

    private String reason;

//    private String refType;

//    private Long refId;

    public static PointHistory createHistory(User user, Type type, Long amount, Long balaceAfter, String reason) {
        PointHistory pointHistory = new PointHistory();
        pointHistory.user = user;
        pointHistory.type = type;
        pointHistory.amount = amount;
        pointHistory.balanceAfter = balaceAfter;
        pointHistory.reason = reason;
        return  pointHistory;
    }
}
