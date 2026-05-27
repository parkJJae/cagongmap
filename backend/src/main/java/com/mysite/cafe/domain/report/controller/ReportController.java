package com.mysite.cafe.domain.report.controller;

import com.mysite.cafe.domain.report.dto.ReportRequest;
import com.mysite.cafe.domain.report.service.ReportService;
import com.mysite.cafe.global.response.ApiResponse;
import com.mysite.cafe.global.util.IpAddressUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cafes/{cafeVisitId}/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ReportController {

    private final ReportService reportService;

    // 카페 방문 기록 신고
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> report(
            @PathVariable Long cafeVisitId,
            @RequestBody ReportRequest request,
            HttpServletRequest httpRequest
    ) {
        String reporterIp = IpAddressUtil.extract(httpRequest);
        reportService.report(cafeVisitId, request, reporterIp);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(null));
    }
}