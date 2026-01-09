package com.backend.backend_boilerplate.domain.auth;

import com.backend.backend_boilerplate.domain.auth.dto.AuthTokens;
import com.backend.backend_boilerplate.domain.auth.dto.LoginRequest;
import com.backend.backend_boilerplate.domain.auth.dto.SignupRequest;
import com.backend.backend_boilerplate.domain.user.User;
import com.backend.backend_boilerplate.domain.user.UserRepository;
import com.backend.backend_boilerplate.global.exception.BusinessException;
import com.backend.backend_boilerplate.global.exception.ErrorCode;
import com.backend.backend_boilerplate.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public Long signup(SignupRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        String encoded = passwordEncoder.encode(req.getPassword());
        User user = new User(req.getEmail(), req.getName(), encoded);
        return userRepository.save(user).getId();
    }

    public AuthTokens login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        String access = jwtProvider.createAccessToken(user.getEmail());
        String refresh = jwtProvider.createRefreshToken(user.getEmail());

        // 기존 토큰 전부 삭제(단일 디바이스 정책) — 다중 디바이스면 저장만 하고 revoke 전략 변경
        refreshTokenRepository.deleteAllByUserId(user.getId());

        var rt = new RefreshToken(
                user.getId(),
                refresh,
                java.time.LocalDateTime.now().plusDays(14) // yml과 맞추면 더 좋음(아래 참고)
        );
        refreshTokenRepository.save(rt);

        return new AuthTokens(access, refresh);
    }

    public AuthTokens refresh(String refreshToken) {
        RefreshToken saved = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (saved.isRevoked() || saved.isExpired()) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // JWT 자체도 검증
        if (!jwtProvider.validate(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String email = jwtProvider.getSubject(refreshToken);

        // rotate: 기존 토큰 revoke 후 새 토큰 발급/저장
        saved.revoke();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String newAccess = jwtProvider.createAccessToken(email);
        String newRefresh = jwtProvider.createRefreshToken(email);

        RefreshToken newEntity = new RefreshToken(
                user.getId(),
                newRefresh,
                java.time.LocalDateTime.now().plusDays(14)
        );

        refreshTokenRepository.save(newEntity);

        return new AuthTokens(newAccess, newRefresh);
    }

    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(RefreshToken::revoke);
    }
}
