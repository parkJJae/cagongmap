package com.mysite.cafe.domain.report.dto;

import com.mysite.cafe.domain.report.entity.ReportReason;

public record ReportRequest(ReportReason reason) {
}