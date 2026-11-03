package com.farmchainx.service.impl;

import com.farmchainx.entity.Notification;
import com.farmchainx.entity.User;
import com.farmchainx.enums.NotificationType;
import com.farmchainx.repository.NotificationRepository;
import com.farmchainx.service.NotificationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Notification send(User user, String message, NotificationType type) {
        Notification n = new Notification();
        n.setUser(user);
        n.setMessage(message);
        n.setNotificationType(type);
        n.setIsRead(false);
        return notificationRepository.save(n);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getUnreadForUser(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Notification> getAllForUser(User user, org.springframework.data.domain.Pageable pageable) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadForUser(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Override
    public int markAllReadForUser(User user) {
        return notificationRepository.markAllReadForUser(user);
    }
}
