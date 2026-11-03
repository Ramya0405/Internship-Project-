package com.farmchainx.service;

import com.farmchainx.entity.Notification;
import com.farmchainx.entity.User;
import com.farmchainx.enums.NotificationType;

import java.util.List;

public interface NotificationService {

    Notification send(User user, String message, NotificationType type);

    List<Notification> getUnreadForUser(User user);

    org.springframework.data.domain.Page<Notification> getAllForUser(User user, org.springframework.data.domain.Pageable pageable);

    long countUnreadForUser(User user);

    int markAllReadForUser(User user);
}
