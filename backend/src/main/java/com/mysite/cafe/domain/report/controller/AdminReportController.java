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
    public ResponseEntity<ApiResponse<List<ReportedCafeResponse>>> findFlagged(
            @RequestHeader(value = "X-Admin-Token", required = false) String token
    ) {
        List<ReportedCafeResponse> response = adminReportService.findFlaggedCafes(token);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 특정 카페의 신고 상세
    @GetMapping("/api/admin/reports/{cafeVisitId}")
    public ResponseEntity<ApiResponse<ReportDetailResponse>> findDetail(
            @PathVariable Long cafeVisitId,
            @RequestHeader(value = "X-Admin-Token", required = false) String token
    ) {
        ReportDetailResponse response = adminReportService.findReportDetail(cafeVisitId, token);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 운영자 검토 완료 처리
    @PostMapping("/api/admin/reports/{cafeVisitId}/review")
    public ResponseEntity<ApiResponse<Void>> markAsReviewed(
            @PathVariable Long cafeVisitId,
            @RequestHeader(value = "X-Admin-Token", required = false) String token
    ) {
        adminReportService.markAsReviewed(cafeVisitId, token);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 악성 카페 글 삭제
    @DeleteMapping("/api/admin/cafes/{cafeVisitId}")
    public ResponseEntity<ApiResponse<Void>> deleteCafe(
            @PathVariable Long cafeVisitId,
            @RequestHeader(value = "X-Admin-Token", required = false) String token
    ) {
        adminReportService.deleteCafe(cafeVisitId, token);
        return ResponseEntity.noContent().build();
    }
}