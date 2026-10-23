package com.expensetracker.controller;

import com.expensetracker.dto.request.TransactionRequest;
import com.expensetracker.dto.response.CategoryResponse;
import com.expensetracker.dto.response.TransactionResponse;
import com.expensetracker.entity.enums.PaymentMethod;
import com.expensetracker.entity.enums.TransactionType;
import com.expensetracker.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @Test
    @DisplayName("POST /api/transactions: Should return 201 Created with Location header on valid request")
    void testCreateTransactionSuccess() throws Exception {
        TransactionRequest request = new TransactionRequest(
                1L, TransactionType.EXPENSE, new BigDecimal("45.50"),
                "Grocery Run", "Weekly essentials",
                LocalDate.of(2026, 10, 23), PaymentMethod.CREDIT_CARD, null
        );

        CategoryResponse categoryResponse = new CategoryResponse(
                1L, "Groceries", TransactionType.EXPENSE, "cart", "#2ECC71", true, Instant.now()
        );

        TransactionResponse response = new TransactionResponse(
                101L, TransactionType.EXPENSE, new BigDecimal("45.50"),
                "Grocery Run", "Weekly essentials", LocalDate.of(2026, 10, 23),
                PaymentMethod.CREDIT_CARD, null, categoryResponse,
                Instant.now(), Instant.now()
        );

        when(transactionService.createTransaction(any(TransactionRequest.class), eq(1L))).thenReturn(response);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(101))
                .andExpect(jsonPath("$.data.title").value("Grocery Run"))
                .andExpect(jsonPath("$.data.amount").value(45.50));
    }

    @Test
    @DisplayName("POST /api/transactions: Should return 400 Bad Request with field errors when payload is invalid")
    void testCreateTransactionValidationFailure() throws Exception {
        // Missing categoryId, negative amount, blank title
        TransactionRequest invalidRequest = new TransactionRequest(
                null, TransactionType.EXPENSE, new BigDecimal("-10.00"),
                "", "Invalid payload",
                LocalDate.of(2026, 10, 23), PaymentMethod.CASH, null
        );

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.validationErrors.categoryId").exists())
                .andExpect(jsonPath("$.validationErrors.amount").exists())
                .andExpect(jsonPath("$.validationErrors.title").exists());
    }

    @Test
    @DisplayName("GET /api/transactions/{id}: Should return 200 OK with transaction details")
    void testGetTransactionById() throws Exception {
        CategoryResponse categoryResponse = new CategoryResponse(
                1L, "Groceries", TransactionType.EXPENSE, "cart", "#2ECC71", true, Instant.now()
        );

        TransactionResponse response = new TransactionResponse(
                101L, TransactionType.EXPENSE, new BigDecimal("45.50"),
                "Grocery Run", "Weekly essentials", LocalDate.of(2026, 10, 23),
                PaymentMethod.CREDIT_CARD, null, categoryResponse,
                Instant.now(), Instant.now()
        );

        when(transactionService.getTransactionById(101L, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/transactions/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(101))
                .andExpect(jsonPath("$.data.title").value("Grocery Run"));
    }

    @Test
    @DisplayName("DELETE /api/transactions/{id}: Should return 204 No Content")
    void testDeleteTransaction() throws Exception {
        doNothing().when(transactionService).deleteTransaction(101L, 1L);

        mockMvc.perform(delete("/api/transactions/101"))
                .andExpect(status().isNoContent());
    }
}
