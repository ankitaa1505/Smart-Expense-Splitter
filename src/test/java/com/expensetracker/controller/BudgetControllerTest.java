package com.expensetracker.controller;

import com.expensetracker.dto.request.BudgetRequest;
import com.expensetracker.dto.response.BudgetResponse;
import com.expensetracker.service.BudgetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BudgetController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BudgetService budgetService;

    @MockBean
    private com.expensetracker.security.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.expensetracker.security.CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("POST /api/budgets: Creates budget and returns 201 Created")
    void testCreateBudget() throws Exception {
        BudgetRequest request = new BudgetRequest(1L, 10, 2026, new BigDecimal("500.00"));
        BudgetResponse response = new BudgetResponse(
                1L, 1L, "Groceries", 10, 2026, new BigDecimal("500.00"),
                BigDecimal.ZERO, new BigDecimal("500.00"), 0.0, "ON_TRACK", Instant.now()
        );

        when(budgetService.createBudget(any(BudgetRequest.class), eq(1L))).thenReturn(response);

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.targetAmount").value(500.00))
                .andExpect(jsonPath("$.data.status").value("ON_TRACK"));
    }

    @Test
    @DisplayName("GET /api/budgets: Retrieves budget list for month")
    void testGetBudgets() throws Exception {
        BudgetResponse response = new BudgetResponse(
                1L, 1L, "Groceries", 10, 2026, new BigDecimal("500.00"),
                new BigDecimal("200.00"), new BigDecimal("300.00"), 40.0, "ON_TRACK", Instant.now()
        );

        when(budgetService.getBudgetsForMonth(10, 2026, 1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/budgets?month=10&year=2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].categoryName").value("Groceries"))
                .andExpect(jsonPath("$.data[0].actualSpent").value(200.00));
    }

    @Test
    @DisplayName("DELETE /api/budgets/{id}: Returns 204 No Content")
    void testDeleteBudget() throws Exception {
        doNothing().when(budgetService).deleteBudget(1L, 1L);

        mockMvc.perform(delete("/api/budgets/1"))
                .andExpect(status().isNoContent());
    }
}
