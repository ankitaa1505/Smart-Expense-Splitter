package com.expensetracker.controller;

import com.expensetracker.dto.common.ApiResponse;
import com.expensetracker.dto.request.BudgetRequest;
import com.expensetracker.dto.response.BudgetResponse;
import com.expensetracker.security.SecurityUtils;
import com.expensetracker.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@Tag(name = "Budget Management", description = "Endpoints for managing monthly spending budgets and threshold notifications")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    @Operation(summary = "Set a monthly budget (overall or category-specific)")
    public ResponseEntity<ApiResponse<BudgetResponse>> createBudget(@Valid @RequestBody BudgetRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        BudgetResponse response = budgetService.createBudget(request, userId);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location)
                .body(ApiResponse.success("Budget set successfully", response));
    }

    @GetMapping
    @Operation(summary = "List budgets with actual spending and utilization stats for a month/year")
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getBudgets(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        Long userId = SecurityUtils.getCurrentUserId();
        int targetMonth = (month != null) ? month : LocalDate.now().getMonthValue();
        int targetYear = (year != null) ? year : LocalDate.now().getYear();

        List<BudgetResponse> budgets = budgetService.getBudgetsForMonth(targetMonth, targetYear, userId);
        return ResponseEntity.ok(ApiResponse.success("Budgets retrieved successfully", budgets));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get single budget details by ID")
    public ResponseEntity<ApiResponse<BudgetResponse>> getBudgetById(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        BudgetResponse response = budgetService.getBudgetById(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Budget retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update target amount for a budget")
    public ResponseEntity<ApiResponse<BudgetResponse>> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        BudgetResponse response = budgetService.updateBudget(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Budget updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a budget target")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        budgetService.deleteBudget(id, userId);
        return ResponseEntity.noContent().build();
    }
}
