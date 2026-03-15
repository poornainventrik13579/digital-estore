package com.inventrik.digitalestore.service;

import com.inventrik.digitalestore.domain.auth.RefreshToken;
import com.inventrik.digitalestore.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final int REFRESH_TOKEN_LENGTH = 64;
    private static final int REFRESH_TOKEN_DAYS = 1;
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private final RefreshTokenRepository refreshTokenRepository;

    public String generateRefreshToken() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(REFRESH_TOKEN_LENGTH);
        for (int i = 0; i < REFRESH_TOKEN_LENGTH; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    @Transactional
    public RefreshToken createRefreshToken(String username, String tokenId) {
        RefreshToken refreshToken = RefreshToken.builder()
                .id(tokenId)
                .username(username)
                .refreshToken(generateRefreshToken())
                .expiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_DAYS))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken findByRefreshToken(String token) {
        return refreshTokenRepository.findByRefreshToken(token).orElse(null);
    }

    public List<RefreshToken> findByUsername(String username) {
        return refreshTokenRepository.findByUsername(username);
    }

    @Transactional
    public void revokeRefreshToken(String refreshToken) {
        refreshTokenRepository.revokeByRefreshToken(refreshToken);
    }

    @Transactional
    public void revokeAllByUsername(String username) {
        refreshTokenRepository.revokeAllByUsername(username);
    }

    @Transactional
    public RefreshToken rotateToken(String oldRefreshToken, String username) {
        refreshTokenRepository.revokeByRefreshToken(oldRefreshToken);
        String newTokenId = java.util.UUID.randomUUID().toString();
        return createRefreshToken(username, newTokenId);
    }

    public boolean isValid(RefreshToken token) {
        return token != null && !token.getRevoked() && !token.isExpired();
    }

    @Transactional
    public void cleanupExpiredTokens() {
        List<RefreshToken> expiredTokens = refreshTokenRepository.findAll().stream()
                .filter(RefreshToken::isExpired)
                .toList();
        refreshTokenRepository.deleteAll(expiredTokens);
        log.info("Cleaned up {} expired refresh tokens", expiredTokens.size());
    }
}
