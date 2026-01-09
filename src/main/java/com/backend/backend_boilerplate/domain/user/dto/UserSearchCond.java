package com.backend.backend_boilerplate.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserSearchCond {
    private String nameKeyword;
    private String emailKeyword;
}
