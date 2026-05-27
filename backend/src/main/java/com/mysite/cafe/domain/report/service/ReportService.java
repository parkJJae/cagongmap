package com.mysite.cafe.domain.report.service;

import com.mysite.cafe.domain.cafevisit.entity.CafeVisit;
import com.mysite.cafe.domain.cafevisit.repository.CafeVisitRepository;
import com.mysite.cafe.domain.report.dto.ReportRequest;
import com.mysite.cafe.domain.report.entity.CafeVisitReport;
import com.mysite.cafe.domain.report.repository.CafeVisitReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final CafeVisitRepository cafeVisitRepository;
    private final CafeVisitReportRepository reportRepository;

    @Value("${app.report.threshold}")
    private int reportThreshold;

    // 신고 등록
    @Transactional
    public void report(Long cafeVisitId, ReportRequest request, String reporterIp) {
        // 신고 대상 카페 조회
        CafeVisit cafeVisit = cafeVisitRepository.findById(cafeVisitId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "카페 방문 기록을 찾을 수 없습니다. id=" + cafeVisitId
                ));

        //같은 IP가 이미 신고했는지 확인
        if (reportRepository.existsByCafeVisitIdAndReporterIp(cafeVisitId, reporterIp)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 신고한 글입니다."
            );
        }

        // 신고 저장
        CafeVisitReport report = CafeVisitReport.builder()
                .cafeVisit(cafeVisit)
                .reporterIp(reporterIp)
                .reason(request.reason())
                .build();
        reportRepository.save(report);

        //카페의 신고 카운트 증가 + 임계값 도달 시 검토 플래그 ON
        cafeVisit.increaseReportCount(reportThreshold);
    }
}