package com.expensetracker.controller;

import com.expensetracker.dto.response.FinancialSummaryResponse;
import com.expensetracker.dto.response.MonthlyTrendResponse;
import com.expensetracker.dto.response.SpendingInsightResponse;
import com.expensetracker.service.AnalyticsService;
import com.expensetracker.service.SmartInsightService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private SmartInsightService smartInsightService;

    @MockBean
    private com.expensetracker.security.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.expensetracker.security.CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("GET /api/analytics/summary: Returns 200 OK with summary metrics")
    void testGetFinancialSummary() throws Exception {
        FinancialSummaryResponse summary = new FinancialSummaryResponse(
                new BigDecimal("6000.00"),
                new BigDecimal("2400.00"),
                new BigDecimal("3600.00"),
                60.0,
                new BigDecimal("80.00"),
                "Housing",
                15L,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
        );

        when(analyticsService.getFinancialSummary(eq(1L), any(), any())).thenReturn(summary);

        mockMvc.perform(get("/api/analytics/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalIncome").value(6000.00))
                .andExpect(jsonPath("$.data.netBalance").value(3600.00))
                .andExpect(jsonPath("$.data.savingsRatePercentage").value(60.0));
    }

    @Test
    @DisplayName("GET /api/analytics/monthly: Returns monthly trends list")
    void testGetMonthlyTrends() throws Exception {
        MonthlyTrendResponse trend = new MonthlyTrendResponse(
                2026, 10, new BigDecimal("6000.00"), new BigDecimal("2400.00"), new BigDecimal("3600.00")
        );

        when(analyticsService.getMonthlyTrends(eq(1L), any(), any())).thenReturn(List.of(trend));

        mockMvc.perform(get("/api/analytics/monthly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].year").value(2026))
                .andExpect(jsonPath("$.data[0].month").value(10))
                .andExpect(jsonPath("$.data[0].netSavings").value(3600.00));
    }

    @Test
    @DisplayName("GET /api/analytics/insights: Returns smart recommendations and alerts")
    void testGetSpendingInsights() throws Exception {
        SpendingInsightResponse.LowBalanceAlert lowBalance = new SpendingInsightResponse.LowBalanceAlert(
                false, new BigDecimal("3600.00"), new BigDecimal("250.00"), "Balance is healthy."
        );
        SpendingInsightResponse response = new SpendingInsightResponse(
                Collections.emptyList(),
                Collections.emptyList(),
                lowBalance,
                List.of("Your savings rate is strong.")
        );

        when(smartInsightService.generateSpendingInsights(1L)).thenReturn(response);

        mockMvc.perform(get("/api/analytics/insights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.lowBalanceAlert.isTriggered").value(false))
                .andExpect(jsonPath("$.data.actionableAdvice[0]").value("Your savings rate is strong."));
    }
}
