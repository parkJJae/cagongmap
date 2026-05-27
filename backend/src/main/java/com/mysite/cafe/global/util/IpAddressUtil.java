package com.mysite.cafe.global.util;

import jakarta.servlet.http.HttpServletRequest;

public class IpAddressUtil {

    private IpAddressUtil() {
    }

    // 클라이언트 IP 추출
    public static String extract(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // 프록시를 여러 번 거치면 콤마로 구분되므로 첫 번째 값이 실제 클라이언트 IP
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}