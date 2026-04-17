package com.pesu.bookrental.vennela.service;

import com.pesu.bookrental.vennela.dto.ChatMessageForm;
import com.pesu.bookrental.domain.model.ChatMessage;
import com.pesu.bookrental.domain.model.RentalTransaction;
import com.pesu.bookrental.domain.model.User;
import com.pesu.bookrental.repository.ChatMessageRepository;
import com.pesu.bookrental.repository.RentalTransactionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final RentalTransactionRepository rentalTransactionRepository;

    public ChatService(ChatMessageRepository chatMessageRepository,
                       RentalTransactionRepository rentalTransactionRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.rentalTransactionRepository = rentalTransactionRepository;
    }

    @Transactional(readOnly = true)
    public RentalTransaction getRentalForConversation(Long rentalId, User user) {
        RentalTransaction rentalTransaction = rentalTransactionRepository.findDetailedById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Rental transaction not found."));
        validateParticipant(rentalTransaction, user);
        return rentalTransaction;
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> findConversation(Long rentalId, User user) {
        RentalTransaction rentalTransaction = getRentalForConversation(rentalId, user);
        return chatMessageRepository.findConversationByRentalId(rentalTransaction.getId());
    }

    @Transactional
    public void sendMessage(Long rentalId, ChatMessageForm form, User sender) {
        RentalTransaction rentalTransaction = getRentalForConversation(rentalId, sender);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRentalTransaction(rentalTransaction);
        chatMessage.setSender(sender);
        chatMessage.setReceiver(resolveReceiver(rentalTransaction, sender));
        chatMessage.setMessage(form.getMessage().trim());
        chatMessageRepository.save(chatMessage);
    }

    private void validateParticipant(RentalTransaction rentalTransaction, User user) {
        if (!rentalTransaction.getRenter().getId().equals(user.getId())
                && !rentalTransaction.getLender().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only access chat for your own rental transactions.");
        }
    }

    private User resolveReceiver(RentalTransaction rentalTransaction, User sender) {
        return rentalTransaction.getRenter().getId().equals(sender.getId())
                ? rentalTransaction.getLender()
                : rentalTransaction.getRenter();
    }
}
