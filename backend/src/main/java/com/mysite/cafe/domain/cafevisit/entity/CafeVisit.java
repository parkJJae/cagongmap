package com.mysite.cafe.domain.cafevisit.entity;

import com.mysite.cafe.domain.user.entity.User;
import com.mysite.cafe.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cafe_visits")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CafeVisit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 카페 이름

    private String address; // 주소

    private Double lat; //위도
    private Double lng; // 경도

    private Integer rating; // 만족도 (1~5)
    private Integer priceLevel; // 가격대 (1~3)

    private Boolean hasOutlet; // 콘센트 여부

    @Enumerated(EnumType.STRING)
    private WifiSpeed wifiSpeed;

    private Integer studyScore; // 공부하기 좋은 정도 (1~5)

    @Column(length = 500)
    private String memo;

    private LocalDateTime visitedAt; // 방문 시각

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 신고 횟수
    @Column(nullable = false)
    private int reportCount = 0;

    // 운영자 검토 필요 플래그 (신고 임계값 도달 시 true)
    @Column(nullable = false)
    private boolean flaggedForReview = false;

    @Builder
    private CafeVisit(String name, String address, Double lat, Double lng,
                      Integer rating, Integer priceLevel, Boolean hasOutlet,
                      WifiSpeed wifiSpeed, Integer studyScore, String memo,
                      LocalDateTime visitedAt, User user) {
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.rating = rating;
        this.priceLevel = priceLevel;
        this.hasOutlet = hasOutlet;
        this.wifiSpeed = wifiSpeed;
        this.studyScore = studyScore;
        this.memo = memo;
        this.visitedAt = visitedAt;
        this.user = user;
    }

    //카페 방문 기록 수정
    public void update(String name, String address, Double lat, Double lng,
                       Integer rating, Integer priceLevel, Boolean hasOutlet,
                       WifiSpeed wifiSpeed, Integer studyScore, String memo,
                       LocalDateTime visitedAt) {
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.rating = rating;
        this.priceLevel = priceLevel;
        this.hasOutlet = hasOutlet;
        this.wifiSpeed = wifiSpeed;
        this.studyScore = studyScore;
        this.memo = memo;
        this.visitedAt = visitedAt;
    }

    // 신고 횟수 증가, 임계값 도달 시 검토 플래그 ON
    public void increaseReportCount(int threshold) {
        this.reportCount++;
        if (this.reportCount >= threshold) {
            this.flaggedForReview = true;
        }
    }

    // 운영자 검토 완료 처리 (카운트 유지 플래그만 해제)
    public void markAsReviewed() {
        this.flaggedForReview = false;
    }
}