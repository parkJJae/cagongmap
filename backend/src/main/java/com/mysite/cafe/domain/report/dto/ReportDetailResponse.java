package com.mysite.cafe.domain.report.dto;

import com.mysite.cafe.domain.report.entity.ReportReason;

import java.util.Map;

public record ReportDetailResponse(
        Long cafeVisitId,
        String cafeName,
        int reportCount,
        boolean flaggedForReview,
        Map<ReportReason, Long> reasonCounts  // 사유별 신고 횟수
) {
}