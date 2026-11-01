package com.expensetracker.scheduler;

import com.expensetracker.service.RecurringTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RecurringTransactionScheduler {

    private static final Logger log = LoggerFactory.getLogger(RecurringTransactionScheduler.class);

    private final RecurringTransactionService recurringTransactionService;

    public RecurringTransactionScheduler(RecurringTransactionService recurringTransactionService) {
        this.recurringTransactionService = recurringTransactionService;
    }

    /**
     * Executes daily at 01:00 AM UTC.
     * Checks for any recurring transactions that have reached or passed their nextRunDate.
     */
    @Scheduled(cron = "0 0 1 * * ?", zone = "UTC")
    public void runDailyRecurringTransactionJob() {
        log.info("Starting daily scheduled recurring transaction processing job...");
        int processed = recurringTransactionService.processDueRecurringTransactions();
        log.info("Daily recurring transaction job completed. Processed {} transactions.", processed);
    }
}
