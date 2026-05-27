package com.mysite.cafe.domain.report.repository;

import com.mysite.cafe.domain.report.entity.CafeVisitReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CafeVisitReportRepository extends JpaRepository<CafeVisitReport, Long> {

    // 같은 IP가 이미 신고했는지 확인
    boolean existsByCafeVisitIdAndReporterIp(Long cafeVisitId, String reporterIp);

    // 특정 카페의 총 신고 횟수
    long countByCafeVisitId(Long cafeVisitId);

    // 특정 카페의 신고 내역 조회 (관리자용)
    List<CafeVisitReport> findByCafeVisitId(Long cafeVisitId);

    // 카페 글 처리 시 신고 내역 함께 삭제
    void deleteByCafeVisitId(Long cafeVisitId);
}