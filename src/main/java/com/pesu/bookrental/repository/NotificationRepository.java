package com.pesu.bookrental.repository;

import com.pesu.bookrental.domain.model.Notification;
import com.pesu.bookrental.domain.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
            select n from Notification n
            where n.user = :user
            order by n.createdAt desc
            """)
    List<Notification> findByUserOrderByCreatedAtDesc(@Param("user") User user);

    long countByUserAndReadFalse(User user);
}
