package com.taskmanagement.controller;

import com.taskmanagement.dto.response.DashboardMetricsResponse;
import com.taskmanagement.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Endpoints for platform overview metrics and KPIs")
@SecurityRequirement(name = "Bearer Authentication")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/metrics")
    @Operation(summary = "Get aggregated workspace statistics", responses = {
            @ApiResponse(responseCode = "200", description = "Dashboard metrics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DashboardMetricsResponse.class)))
    })
    public ResponseEntity<DashboardMetricsResponse> getMetrics() {
        DashboardMetricsResponse response = dashboardService.getMetrics();
        return ResponseEntity.ok(response);
    }
}
