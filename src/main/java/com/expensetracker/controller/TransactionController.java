package com.expensetracker.controller;

import com.expensetracker.dto.common.ApiResponse;
import com.expensetracker.dto.common.PageResponse;
import com.expensetracker.dto.request.TransactionFilterRequest;
import com.expensetracker.dto.request.TransactionRequest;
import com.expensetracker.dto.response.TransactionResponse;
import com.expensetracker.security.SecurityUtils;
import com.expensetracker.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transaction Management", description = "Endpoints for creating, querying, updating, and deleting financial transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @Operation(summary = "Record a new expense or income transaction")
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @Valid @RequestBody TransactionRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        TransactionResponse response = transactionService.createTransaction(request, userId);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location)
                .body(ApiResponse.success("Transaction created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction details by ID")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(
            @PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        TransactionResponse response = transactionService.getTransactionById(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Transaction retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing transaction")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        TransactionResponse response = transactionService.updateTransaction(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Transaction updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transaction")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        transactionService.deleteTransaction(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Search, filter, sort, and paginate transactions")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getTransactions(
            @ModelAttribute TransactionFilterRequest filter) {
        Long userId = SecurityUtils.getCurrentUserId();
        PageResponse<TransactionResponse> response = transactionService.getTransactions(filter, userId);
        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved successfully", response));
    }
}
