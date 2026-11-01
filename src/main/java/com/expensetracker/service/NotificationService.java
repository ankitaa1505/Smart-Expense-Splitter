package com.expensetracker.service;

import com.expensetracker.dto.common.PageResponse;
import com.expensetracker.dto.response.NotificationResponse;
import com.expensetracker.entity.Notification;
import com.expensetracker.entity.User;
import com.expensetracker.entity.enums.NotificationType;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.mapper.NotificationMapper;
import com.expensetracker.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationRepository notificationRepository, NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
    }

    @Transactional
    public void createNotification(User user, NotificationType type, String title, String message, Long referenceId) {
        log.info("Creating notification '{}' for user ID: {}", title, user.getId());
        Notification notification = new Notification(user, type, title, message, referenceId);
        notificationRepository.save(notification);
    }

    public PageResponse<NotificationResponse> getUserNotifications(Long userId, Pageable pageable) {
        Page<NotificationResponse> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(notificationMapper::toDto);
        return PageResponse.of(page);
    }

    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId).stream()
                .map(notificationMapper::toDto)
                .toList();
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}
