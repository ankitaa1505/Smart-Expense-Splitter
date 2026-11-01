package com.expensetracker.dto.response;

import com.expensetracker.entity.enums.NotificationType;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        NotificationType notificationType,
        String title,
        String message,
        boolean isRead,
        Long referenceId,
        Instant createdAt
) {}
