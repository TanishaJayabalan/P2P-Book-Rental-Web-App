package com.pesu.bookrental.repository;

import com.pesu.bookrental.domain.model.ChatMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("""
            select m from ChatMessage m
            join fetch m.sender s
            join fetch m.receiver r
            join fetch m.rentalTransaction rt
            where rt.id = :rentalId
            order by m.sentAt asc, m.id asc
            """)
    List<ChatMessage> findConversationByRentalId(@Param("rentalId") Long rentalId);
}
