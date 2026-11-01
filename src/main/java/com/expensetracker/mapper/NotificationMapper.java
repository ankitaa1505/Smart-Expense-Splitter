package com.expensetracker.mapper;

import com.expensetracker.dto.response.NotificationResponse;
import com.expensetracker.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toDto(Notification entity) {
        if (entity == null) return null;
        return new NotificationResponse(
                entity.getId(),
                entity.getNotificationType(),
                entity.getTitle(),
                entity.getMessage(),
                entity.isRead(),
                entity.getReferenceId(),
                entity.getCreatedAt()
        );
    }
}
