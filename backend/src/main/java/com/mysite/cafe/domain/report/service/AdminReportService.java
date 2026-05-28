package com.mysite.cafe.domain.report.service;

import com.mysite.cafe.domain.cafevisit.entity.CafeVisit;
import com.mysite.cafe.domain.cafevisit.repository.CafeVisitRepository;
import com.mysite.cafe.domain.report.dto.ReportDetailResponse;
import com.mysite.cafe.domain.report.dto.ReportedCafeResponse;
import com.mysite.cafe.domain.report.entity.CafeVisitReport;
import com.mysite.cafe.domain.report.entity.ReportReason;
import com.mysite.cafe.domain.report.repository.CafeVisitReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportService {

    private final CafeVisitRepository cafeVisitRepository;
    private final CafeVisitReportRepository reportRepository;

    @Value("${app.admin.token}")
    private String adminToken;

    // 검토 필요 카페 목록
    public List<ReportedCafeResponse> findFlaggedCafes(String token) {
        verifyToken(token);

        List<CafeVisit> flagged = cafeVisitRepository.findByFlaggedForReviewTrueOrderByReportCountDesc();
        return flagged.stream()
                .map(c -> new ReportedCafeResponse(
                        c.getId(),
                        c.getName(),
                        c.getReportCount(),
                        c.getCreatedAt()
                ))
                .toList();
    }

    // 특정 카페의 신고 상세
    public ReportDetailResponse findReportDetail(Long cafeVisitId, String token) {
        verifyToken(token);

        CafeVisit cafeVisit = cafeVisitRepository.findById(cafeVisitId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "카페 방문 기록을 찾을 수 없습니다. id=" + cafeVisitId
                ));

        List<CafeVisitReport> reports = reportRepository.findByCafeVisitId(cafeVisitId);

        Map<ReportReason, Long> reasonCounts = reports.stream()
                .collect(Collectors.groupingBy(
                        CafeVisitReport::getReason,
                        Collectors.counting()
                ));

        return new ReportDetailResponse(
                cafeVisit.getId(),
                cafeVisit.getName(),
                cafeVisit.getReportCount(),
                cafeVisit.isFlaggedForReview(),
                reasonCounts
        );
    }

    // 운영자 검토 완료 처리
    @Transactional
    public void markAsReviewed(Long cafeVisitId, String token) {
        verifyToken(token);

        CafeVisit cafeVisit = cafeVisitRepository.findById(cafeVisitId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "카페 방문 기록을 찾을 수 없습니다. id=" + cafeVisitId
                ));

        cafeVisit.markAsReviewed();
    }

    // 악성 카페 글 삭제 (신고 내역 같이 삭제)
    @Transactional
    public void deleteCafe(Long cafeVisitId, String token) {
        verifyToken(token);

        if (!cafeVisitRepository.existsById(cafeVisitId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "카페 방문 기록을 찾을 수 없습니다. id=" + cafeVisitId
            );
        }

        // 신고 내역 먼저 삭제
        reportRepository.deleteByCafeVisitId(cafeVisitId);
        // 카페 삭제
        cafeVisitRepository.deleteById(cafeVisitId);
    }

    // 토큰 검증
    private void verifyToken(String token) {
        if (token == null || !token.equals(adminToken)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "관리자 권한이 필요합니다."
            );
        }
    }
}