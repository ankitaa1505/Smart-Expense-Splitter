package com.expensetracker.mapper;

import com.expensetracker.dto.request.RegisterRequest;
import com.expensetracker.dto.response.UserProfileResponse;
import com.expensetracker.entity.User;
import com.expensetracker.entity.enums.Role;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserProfileResponse toDto(User entity) {
        if (entity == null) return null;
        return new UserProfileResponse(
                entity.getId(),
                entity.getEmail(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getRole(),
                entity.getCreatedAt()
        );
    }

    public User toEntity(RegisterRequest request, String encodedPassword) {
        if (request == null) return null;
        return new User(
                request.email().trim().toLowerCase(),
                encodedPassword,
                request.firstName().trim(),
                request.lastName().trim(),
                Role.ROLE_USER
        );
    }
}
