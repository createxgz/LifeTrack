package com.lifetrack.controller;

import com.lifetrack.common.Result;
import com.lifetrack.service.DashboardService;
import com.lifetrack.vo.DashboardCalendarVO;
import com.lifetrack.vo.DashboardOverviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "仪表盘")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "仪表盘总览")
    @GetMapping("/overview")
    public Result<DashboardOverviewVO> overview(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(dashboardService.getOverview(userId));
    }

    @Operation(summary = "日历热力图")
    @GetMapping("/calendar")
    public Result<DashboardCalendarVO> calendar(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(dashboardService.getCalendar(userId, year, month));
    }
}
