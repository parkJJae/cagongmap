package com.mysite.cafe.domain.report.entity;

import com.mysite.cafe.domain.cafevisit.entity.CafeVisit;
import com.mysite.cafe.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "cafe_visit_reports",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"visit_id", "reporter_ip"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CafeVisitReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 신고 대상 카페 방문 기록
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", nullable = false)
    private CafeVisit cafeVisit;

    // 신고자 IP (같은 IP가 같은 글을 중복 신고하지 못하도록)
    @Column(name = "reporter_ip", nullable = false, length = 45)
    private String reporterIp;

    // 신고 사유
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

    @Builder
    private CafeVisitReport(CafeVisit cafeVisit, String reporterIp, ReportReason reason) {
        this.cafeVisit = cafeVisit;
        this.reporterIp = reporterIp;
        this.reason = reason;
    }
}