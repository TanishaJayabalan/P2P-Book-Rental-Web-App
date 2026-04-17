package com.pesu.bookrental.vennela.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChatMessageForm {

    @NotBlank(message = "Message cannot be empty.")
    @Size(max = 1000, message = "Message must be at most 1000 characters.")
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
