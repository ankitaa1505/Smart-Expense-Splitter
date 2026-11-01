package com.expensetracker.service;

import com.expensetracker.entity.Transaction;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.repository.TransactionRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ExportService {

    private static final Logger log = LoggerFactory.getLogger(ExportService.class);

    private static final String[] CSV_HEADERS = {
            "ID", "Date", "Type", "Category", "Amount", "Payment Method", "Title", "Description", "Notes", "Created At"
    };

    private final TransactionRepository transactionRepository;

    public ExportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public byte[] exportTransactionsToCsv(Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("Exporting transactions to CSV for user ID: {} from {} to {}", userId, startDate, endDate);

        if (startDate == null) startDate = LocalDate.of(2000, 1, 1);
        if (endDate == null) endDate = LocalDate.now();
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        List<Transaction> transactions = transactionRepository
                .findAllByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(userId, startDate, endDate);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CSVPrinter printer = new CSVPrinter(
                new OutputStreamWriter(out, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.builder().setHeader(CSV_HEADERS).build())) {

            for (Transaction tx : transactions) {
                printer.printRecord(
                        tx.getId(),
                        tx.getTransactionDate(),
                        tx.getType(),
                        tx.getCategory() != null ? tx.getCategory().getName() : "Uncategorized",
                        tx.getAmount(),
                        tx.getPaymentMethod(),
                        tx.getTitle(),
                        tx.getDescription() != null ? tx.getDescription() : "",
                        tx.getNotes() != null ? tx.getNotes() : "",
                        tx.getCreatedAt()
                );
            }
            printer.flush();
        } catch (IOException e) {
            log.error("Failed to generate CSV export for user ID: {}: {}", userId, e.getMessage());
            throw new RuntimeException("Could not generate CSV export: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }
}
