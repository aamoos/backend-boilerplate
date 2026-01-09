package com.backend.backend_boilerplate.domain.user.dto;

import com.backend.backend_boilerplate.domain.user.User;
import lombok.Getter;

@Getter
public class UserResponse {
    private final Long id;
    private final String email;
    private final String name;

    public UserResponse(Long id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }

    public static UserResponse from(User user){
        return new UserResponse(user.getId(), user.getEmail(), user.getName());
    }
}
