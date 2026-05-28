package com.mysite.cafe.domain.report.controller;

import com.mysite.cafe.domain.report.dto.ReportDetailResponse;
import com.mysite.cafe.domain.report.dto.ReportedCafeResponse;
import com.mysite.cafe.domain.report.service.AdminReportService;
import com.mysite.cafe.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    // 검토 필요 카페 목록
    @GetMapping("/api/admin/reports")
    public ResponseEntity<ApiResponse<List<ReportedCafeResponse>>> findFlagged() {
        List<ReportedCafeResponse> response = adminReportService.findFlaggedCafes();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 특정 카페의 신고 상세
    @GetMapping("/api/admin/reports/{cafeVisitId}")
    public ResponseEntity<ApiResponse<ReportDetailResponse>> findDetail(
            @PathVariable Long cafeVisitId
    ) {
        ReportDetailResponse response = adminReportService.findReportDetail(cafeVisitId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 운영자 검토 완료 처리
    @PostMapping("/api/admin/reports/{cafeVisitId}/review")
    public ResponseEntity<ApiResponse<Void>> markAsReviewed(
            @PathVariable Long cafeVisitId
    ) {
        adminReportService.markAsReviewed(cafeVisitId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 악성 카페 글 삭제
    @DeleteMapping("/api/admin/cafes/{cafeVisitId}")
    public ResponseEntity<ApiResponse<Void>> deleteCafe(
            @PathVariable Long cafeVisitId
    ) {
        adminReportService.deleteCafe(cafeVisitId);
        return ResponseEntity.noContent().build();
    }
}