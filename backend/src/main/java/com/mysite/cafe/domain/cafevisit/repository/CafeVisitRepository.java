package com.mysite.cafe.domain.cafevisit.repository;

import com.mysite.cafe.domain.cafevisit.entity.CafeVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CafeVisitRepository extends JpaRepository<CafeVisit, Long> {

    @Query("SELECT c FROM CafeVisit c LEFT JOIN FETCH c.user")
    List<CafeVisit> findAllWithUser();

    List<CafeVisit> findByFlaggedForReviewTrueOrderByReportCountDesc();

    @Query("SELECT c FROM CafeVisit c LEFT JOIN FETCH c.user ORDER BY c.createdAt DESC")
    List<CafeVisit> findAllWithUserOrderByCreatedAtDesc();

    @Query("""
    SELECT COUNT(c) > 0 FROM CafeVisit c
    WHERE c.lat = :lat AND c.lng = :lng
      AND c.createdAt > :since
""")
    boolean existsRecentByLocation(
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("since") LocalDateTime since
    );
}
