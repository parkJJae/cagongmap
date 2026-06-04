package com.mysite.cafe.domain.report.service;

import com.mysite.cafe.domain.cafevisit.dto.AdminCafeListResponse;
import com.mysite.cafe.domain.cafevisit.entity.CafeVisit;
import com.mysite.cafe.domain.cafevisit.repository.CafeVisitRepository;
import com.mysite.cafe.domain.report.dto.ReportDetailResponse;
import com.mysite.cafe.domain.report.dto.ReportedCafeResponse;
import com.mysite.cafe.domain.report.entity.CafeVisitReport;
import com.mysite.cafe.domain.report.entity.ReportReason;
import com.mysite.cafe.domain.report.repository.CafeVisitReportRepository;
import lombok.RequiredArgsConstructor;
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

    // 검토 필요 카페 목록
    public List<ReportedCafeResponse> findFlaggedCafes() {
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
    public ReportDetailResponse findReportDetail(Long cafeVisitId) {
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
    public void markAsReviewed(Long cafeVisitId) {
        CafeVisit cafeVisit = cafeVisitRepository.findById(cafeVisitId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "카페 방문 기록을 찾을 수 없습니다. id=" + cafeVisitId
                ));

        cafeVisit.markAsReviewed();
    }

    // 악성 카페 글 삭제 (신고 내역 같이 삭제)
    @Transactional
    public void deleteCafe(Long cafeVisitId) {
        if (!cafeVisitRepository.existsById(cafeVisitId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "카페 방문 기록을 찾을 수 없습니다. id=" + cafeVisitId
            );
        }

        reportRepository.deleteByCafeVisitId(cafeVisitId);
        cafeVisitRepository.deleteById(cafeVisitId);
    }

    // 전체 카페 목록
    public List<AdminCafeListResponse> findAllCafes() {
        return cafeVisitRepository.findAllWithUserOrderByCreatedAtDesc().stream()
                .map(c -> new AdminCafeListResponse(
                        c.getId(),
                        c.getName(),
                        c.getAddress(),
                        c.getUser() != null ? c.getUser().getNickname() : null,
                        c.getRating(),
                        c.getHasOutlet(),
                        c.getWifiSpeed() != null ? c.getWifiSpeed().name() : null,
                        c.getMemo(),
                        c.getReportCount(),
                        c.isFlaggedForReview(),
                        c.getCreatedAt()
                ))
                .toList();
    }
}