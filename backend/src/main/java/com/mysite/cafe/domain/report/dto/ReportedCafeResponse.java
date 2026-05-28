package com.mysite.cafe.domain.report.dto;

import java.time.LocalDateTime;

public record ReportedCafeResponse(
        Long cafeVisitId,
        String cafeName,
        int reportCount,
        LocalDateTime createdAt  // 카페 등록 시점
) {
}