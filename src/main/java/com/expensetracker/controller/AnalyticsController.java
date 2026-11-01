package com.expensetracker.controller;

import com.expensetracker.dto.common.ApiResponse;
import com.expensetracker.dto.response.CategoryBreakdownResponse;
import com.expensetracker.dto.response.FinancialSummaryResponse;
import com.expensetracker.dto.response.MonthOverMonthResponse;
import com.expensetracker.dto.response.MonthlyTrendResponse;
import com.expensetracker.entity.enums.TransactionType;
import com.expensetracker.security.SecurityUtils;
import com.expensetracker.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Financial Analytics", description = "Endpoints for financial dashboards, category spending breakdowns, trends, and MoM changes")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final com.expensetracker.service.SmartInsightService smartInsightService;

    public AnalyticsController(AnalyticsService analyticsService,
                               com.expensetracker.service.SmartInsightService smartInsightService) {
        this.analyticsService = analyticsService;
        this.smartInsightService = smartInsightService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get aggregated financial summary (total income, expense, balance, savings rate, daily average)")
    public ResponseEntity<ApiResponse<FinancialSummaryResponse>> getFinancialSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = SecurityUtils.getCurrentUserId();
        FinancialSummaryResponse summary = analyticsService.getFinancialSummary(userId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Financial summary retrieved successfully", summary));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get category-wise spending breakdown with percentages")
    public ResponseEntity<ApiResponse<List<CategoryBreakdownResponse>>> getCategoryBreakdown(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<CategoryBreakdownResponse> breakdown = analyticsService.getCategoryBreakdown(userId, type, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Category breakdown retrieved successfully", breakdown));
    }

    @GetMapping("/monthly")
    @Operation(summary = "Get monthly income and expense trends over time")
    public ResponseEntity<ApiResponse<List<MonthlyTrendResponse>>> getMonthlyTrends(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<MonthlyTrendResponse> trends = analyticsService.getMonthlyTrends(userId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Monthly trends retrieved successfully", trends));
    }

    @GetMapping("/mom")
    @Operation(summary = "Get Month-over-Month (MoM) spending comparison")
    public ResponseEntity<ApiResponse<MonthOverMonthResponse>> getMonthOverMonthChange(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        Long userId = SecurityUtils.getCurrentUserId();
        int targetYear = (year != null) ? year : LocalDate.now().getYear();
        int targetMonth = (month != null) ? month : LocalDate.now().getMonthValue();

        MonthOverMonthResponse response = analyticsService.getMonthOverMonthChange(userId, targetYear, targetMonth);
        return ResponseEntity.ok(ApiResponse.success("Month-over-month comparison retrieved successfully", response));
    }

    @GetMapping("/insights")
    @Operation(summary = "Get smart financial insights, unusual-spending alerts, and budget recommendations")
    public ResponseEntity<ApiResponse<com.expensetracker.dto.response.SpendingInsightResponse>> getSpendingInsights() {
        Long userId = SecurityUtils.getCurrentUserId();
        com.expensetracker.dto.response.SpendingInsightResponse insights = smartInsightService.generateSpendingInsights(userId);
        return ResponseEntity.ok(ApiResponse.success("Spending insights generated successfully", insights));
    }
}
