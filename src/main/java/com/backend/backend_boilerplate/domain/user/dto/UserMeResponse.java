package com.backend.backend_boilerplate.domain.user.dto;

import com.backend.backend_boilerplate.domain.user.User;
import lombok.Getter;

@Getter
public class UserMeResponse {

    private final Long id;
    private final String email;
    private final String name;

    private UserMeResponse(Long id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }

    public static UserMeResponse from(User user) {
        return new UserMeResponse(
                user.getId(),
                user.getEmail(),
                user.getName()
        );
    }
}
