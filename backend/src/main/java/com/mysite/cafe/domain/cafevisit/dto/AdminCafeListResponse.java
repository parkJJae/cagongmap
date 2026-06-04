package com.mysite.cafe.domain.cafevisit.dto;

import java.time.LocalDateTime;

public record AdminCafeListResponse(
        Long id,
        String name,
        String address,
        String registeredBy,
        Integer rating,
        Boolean hasOutlet,
        String wifiSpeed,
        String memo,
        int reportCount,
        boolean flaggedForReview,
        LocalDateTime createdAt
) {}