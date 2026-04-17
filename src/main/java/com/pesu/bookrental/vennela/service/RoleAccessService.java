package com.pesu.bookrental.vennela.service;

import com.pesu.bookrental.domain.enums.Role;
import com.pesu.bookrental.domain.model.User;
import org.springframework.stereotype.Service;

@Service
public class RoleAccessService {

    public void requireModeratorOrAdmin(User user) {
        if (user == null || (user.getRole() != Role.MODERATOR && user.getRole() != Role.ADMINISTRATOR)) {
            throw new IllegalArgumentException("Only moderators or administrators can access this page.");
        }
    }

    public void requireAdmin(User user) {
        if (user == null || user.getRole() != Role.ADMINISTRATOR) {
            throw new IllegalArgumentException("Only administrators can access this page.");
        }
    }
}
