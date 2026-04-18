package com.pesu.bookrental.config;

import com.pesu.bookrental.domain.model.User;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    public static final String SESSION_USER_ID = "currentUserId";

    public Optional<Long> getUserId(HttpSession session) {
        Object value = session.getAttribute(SESSION_USER_ID);
        if (value instanceof Long userId) {
            return Optional.of(userId);
        }
        if (value instanceof Integer userId) {
            return Optional.of(userId.longValue());
        }
        return Optional.empty();
    }

    public void login(HttpSession session, User user) {
        session.setAttribute(SESSION_USER_ID, user.getId());
    }

    public void logout(HttpSession session) {
        session.removeAttribute(SESSION_USER_ID);
    }
}
