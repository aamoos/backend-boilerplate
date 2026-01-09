package com.backend.backend_boilerplate.domain.user;

import com.backend.backend_boilerplate.domain.user.dto.UserSearchCond;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserRepositoryCustom {
    Page<User> search(UserSearchCond cond, Pageable pageable);
}
