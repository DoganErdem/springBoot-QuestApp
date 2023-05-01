package com.project.questapp.services;

import com.project.questapp.entities.RefreshToken;
import com.project.questapp.entities.User;
import com.project.questapp.repos.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${refresh.token.expires.in}")
    Long expiresSeconds;

    private RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String createRefreshToken(User user){
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(Date.from(Instant.now().plusSeconds(expiresSeconds)));
        refreshTokenRepository.save(token);
        return token.getToken();

    }

    public boolean isRefreshTokenExpired(RefreshToken token){
        return token.getExpiryDate().before(new Date());
    }

    public RefreshToken getByUser(Long userId) {
        return refreshTokenRepository.findByUserId(userId);
    }
}
