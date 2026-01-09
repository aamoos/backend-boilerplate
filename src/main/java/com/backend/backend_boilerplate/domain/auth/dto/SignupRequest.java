package com.backend.backend_boilerplate.domain.auth.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {

    @NotBlank(message = "email은 필수입니다.")
    @Email(message = "email 형식이 올바르지 않습니다.")
    private String email;

    @Size(max = 30, message = "name은 30자 이하입니다.")
    private String name;

    @NotBlank(message = "password는 필수입니다.")
    @Size(min = 8, max = 50, message = "password는 8~50자입니다.")
    private String password;
}
