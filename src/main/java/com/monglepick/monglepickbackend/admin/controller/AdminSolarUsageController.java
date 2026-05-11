package com.monglepick.monglepickbackend.admin.controller;

import com.monglepick.monglepickbackend.admin.dto.SolarUsageDto.SolarUsageLogRequest;
import com.monglepick.monglepickbackend.admin.dto.SolarUsageDto.SolarUsageStatsResponse;
import com.monglepick.monglepickbackend.admin.service.AdminSolarUsageService;
import com.monglepick.monglepickbackend.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Upstage Solar API 사용량/비용 관리자 컨트롤러.
 *
 * <p>두 종류의 EP 를 제공한다.</p>
 * <ul>
 *   <li>{@code POST /log} — Agent fire-and-forget 적재 (X-Service-Key 인증, ROLE_SERVICE).
 *       SecurityConfig 에서 {@code /api/v1/admin/**} 보다 우선 매칭되도록 명시 등록됨.</li>
 *   <li>{@code GET ""} — 매출 탭 섹션 응답 (ADMIN 인증).</li>
 * </ul>
 *
 * <p>비용 계산은 Agent 측에서 수행한 결과를 그대로 신뢰하며, Backend 는 누적/집계만 담당한다.</p>
 */
@Tag(name = "관리자 — Solar API 사용량", description = "Upstage Solar API 토큰/비용 적재 및 집계")
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/stats/solar-usage")
@RequiredArgsConstructor
public class AdminSolarUsageController {

    private final AdminSolarUsageService adminSolarUsageService;

    /**
     * Agent → Backend 단일 호출 적재.
     *
     * <p>X-Service-Key 헤더가 없거나 불일치하면 SecurityConfig + ServiceKeyAuthFilter
     * 단계에서 401 을 받는다. ROLE_SERVICE 통과 후에만 본 메서드가 호출된다.</p>
     *
     * @param req 적재 요청
     * @return 저장된 로그의 PK
     */
    @Operation(summary = "Solar 호출 1건 적재", description = "Agent 가 LangChain 콜백에서 fire-and-forget 으로 호출")
    @PostMapping("/log")
    public ResponseEntity<ApiResponse<Long>> appendUsageLog(
            @Valid @RequestBody SolarUsageLogRequest req
    ) {
        Long id = adminSolarUsageService.appendUsageLog(req);
        return ResponseEntity.ok(ApiResponse.ok(id));
    }

    /**
     * 매출 탭 "Solar API 사용/비용" 섹션 데이터.
     *
     * @param period "7d" | "30d" | "90d" — 기본 30d
     * @return KPI + 일별 추이 + 모델/에이전트 분포
     */
    @Operation(summary = "Solar API 사용/비용 통계", description = "오늘/어제/이번달/기간/누적 KPI + 일별 추이 + 모델·에이전트 분포")
    @GetMapping
    public ResponseEntity<ApiResponse<SolarUsageStatsResponse>> getUsageStats(
            @Parameter(description = "기간 (7d/30d/90d)", example = "30d")
            @RequestParam(defaultValue = "30d") String period
    ) {
        SolarUsageStatsResponse data = adminSolarUsageService.getUsageStats(period);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}
