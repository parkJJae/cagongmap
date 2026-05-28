package com.mysite.cafe.domain.cafevisit.controller;

import com.mysite.cafe.domain.cafevisit.dto.CafeVisitRequest;
import com.mysite.cafe.domain.cafevisit.dto.CafeVisitResponse;
import com.mysite.cafe.domain.cafevisit.service.CafeVisitService;
import com.mysite.cafe.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cafes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class CafeVisitController {

    private final CafeVisitService cafeVisitService;

    //카페 방문 기록 등록
    @PostMapping
    public ResponseEntity<ApiResponse<CafeVisitResponse>> create(
            @RequestBody CafeVisitRequest request) // json을 cafevisitrequest 객체로 자동 변환
    {
        CafeVisitResponse response = cafeVisitService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    //카페 방문 기록 전체 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<CafeVisitResponse>>> findAll() {
        List<CafeVisitResponse> response = cafeVisitService.findAll();
        return ResponseEntity
                .ok(ApiResponse.success(response));
    }

    //카페 방문 기록 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CafeVisitResponse>> findById(
            @PathVariable Long id
    ) {
        CafeVisitResponse response = cafeVisitService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    //카페 방문 기록 수정
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CafeVisitResponse>> update(
            @PathVariable Long id,
            @RequestBody CafeVisitRequest request
    ) {
        CafeVisitResponse response = cafeVisitService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    //카페 방문 기록 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id
    ) {
        cafeVisitService.delete(id);
        return ResponseEntity.noContent().build();
    }

}