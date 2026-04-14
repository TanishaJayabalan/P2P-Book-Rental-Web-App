package com.pesu.bookrental.tanisha.service;

import com.pesu.bookrental.domain.model.Notification;
import com.pesu.bookrental.domain.model.User;
import com.pesu.bookrental.factory.BookRentalFactory;
import com.pesu.bookrental.repository.NotificationRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final BookRentalFactory bookRentalFactory;

    public NotificationService(NotificationRepository notificationRepository,
                               BookRentalFactory bookRentalFactory) {
        this.notificationRepository = notificationRepository;
        this.bookRentalFactory = bookRentalFactory;
    }

    @Transactional
    public Notification createNotification(User user, String message) {
        Notification notification = bookRentalFactory.createNotification(user, message);
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> findNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public long countUnreadNotifications(User user) {
        return notificationRepository.countByUserAndReadFalse(user);
    }

    @Transactional
    public void markAllRead(User user) {
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        notifications.stream()
                .filter(notification -> !notification.isRead())
                .forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(notifications);
    }
}
