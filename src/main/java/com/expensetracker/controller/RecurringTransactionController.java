package com.expensetracker.controller;

import com.expensetracker.dto.common.ApiResponse;
import com.expensetracker.dto.request.RecurringTransactionRequest;
import com.expensetracker.dto.response.RecurringTransactionResponse;
import com.expensetracker.security.SecurityUtils;
import com.expensetracker.service.RecurringTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/recurring")
@Tag(name = "Recurring Transactions", description = "Endpoints for scheduling recurring expenses and income records")
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    public RecurringTransactionController(RecurringTransactionService recurringTransactionService) {
        this.recurringTransactionService = recurringTransactionService;
    }

    @PostMapping
    @Operation(summary = "Set up a new recurring transaction (DAILY, WEEKLY, MONTHLY, YEARLY)")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> createRecurring(
            @Valid @RequestBody RecurringTransactionRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        RecurringTransactionResponse response = recurringTransactionService.createRecurringTransaction(request, userId);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location)
                .body(ApiResponse.success("Recurring transaction created successfully", response));
    }

    @GetMapping
    @Operation(summary = "List all recurring schedules for the authenticated user")
    public ResponseEntity<ApiResponse<List<RecurringTransactionResponse>>> getRecurringTransactions() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<RecurringTransactionResponse> list = recurringTransactionService.getUserRecurringTransactions(userId);
        return ResponseEntity.ok(ApiResponse.success("Recurring transactions retrieved successfully", list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get single recurring schedule by ID")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> getRecurringById(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        RecurringTransactionResponse response = recurringTransactionService.getRecurringTransactionById(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Recurring transaction retrieved successfully", response));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle active/inactive state of a recurring schedule")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> toggleActive(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        RecurringTransactionResponse response = recurringTransactionService.toggleActive(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Recurring transaction status updated", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a recurring transaction schedule")
    public ResponseEntity<Void> deleteRecurring(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        recurringTransactionService.deleteRecurringTransaction(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/trigger-process")
    @Operation(summary = "Manually trigger processing of all due recurring transactions (Admin / Testing)")
    public ResponseEntity<ApiResponse<Integer>> triggerProcess() {
        int processedCount = recurringTransactionService.processDueRecurringTransactions();
        return ResponseEntity.ok(ApiResponse.success("Processed " + processedCount + " due recurring transactions", processedCount));
    }
}
