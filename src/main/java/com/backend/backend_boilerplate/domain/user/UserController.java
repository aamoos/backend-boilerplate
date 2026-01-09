package com.backend.backend_boilerplate.domain.user;

import com.backend.backend_boilerplate.domain.user.dto.UserCreateRequest;
import com.backend.backend_boilerplate.domain.user.dto.UserMeResponse;
import com.backend.backend_boilerplate.domain.user.dto.UserResponse;
import com.backend.backend_boilerplate.domain.user.dto.UserSearchCond;
import com.backend.backend_boilerplate.global.api.ApiResponse;
import com.backend.backend_boilerplate.global.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    // 예: /api/v1/users?nameKeyword=kim&emailKeyword=gmail&page=0&size=20&sort=id,desc
    @Operation(summary = "사용자 검색", description = "조건(name/email) + 페이징/정렬로 검색합니다.")
    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> search(
            UserSearchCond cond,
            @PageableDefault(size = 20) Pageable pageable
    ){
        Page<UserResponse> page = userService.search(cond, pageable)
                .map(UserResponse::from);

        return ApiResponse.ok(new PageResponse<>(page));
    }

    @GetMapping("/me")
    public ApiResponse<UserMeResponse> me() {
        User user = userService.getCurrentUser();
        return ApiResponse.ok(UserMeResponse.from(user));
    }
}
