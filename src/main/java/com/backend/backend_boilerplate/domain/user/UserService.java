package com.backend.backend_boilerplate.domain.user;

import com.backend.backend_boilerplate.domain.user.dto.UserSearchCond;
import com.backend.backend_boilerplate.global.exception.BusinessException;
import com.backend.backend_boilerplate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public Page<User> search(UserSearchCond cond, Pageable pageable) {
        return userRepository.search(cond, pageable);
    }

    @Transactional
    public Long create(String email, String name, String password) {
        if (userRepository.existsByEmail(email)){
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        User user = new User(email, name, password);
        return userRepository.save(user).getId();
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
