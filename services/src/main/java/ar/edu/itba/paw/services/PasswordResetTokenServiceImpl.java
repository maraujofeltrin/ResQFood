package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.PasswordResetToken;
import ar.edu.itba.paw.persistence.PasswordResetTokenDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

    private final PasswordResetTokenDao passwordResetTokenDao;

    @Autowired
    public PasswordResetTokenServiceImpl(final PasswordResetTokenDao passwordResetTokenDao) {
        this.passwordResetTokenDao = passwordResetTokenDao;
    }

    @Override
    public PasswordResetToken createForUser(final Long userId) {
        final String token = UUID.randomUUID().toString();
        final LocalDateTime createdAt = LocalDateTime.now();
        final LocalDateTime expiresAt = createdAt.plusHours(1);
        return passwordResetTokenDao.create(token, userId, createdAt, expiresAt);
    }

    @Override
    public Optional<PasswordResetToken> findByToken(final String token) {
        return passwordResetTokenDao.findByToken(token);
    }

    @Override
    public boolean isValid(final PasswordResetToken token) {
        return !token.isUsed() && token.getExpiresAt().isAfter(LocalDateTime.now());
    }

    @Override
    public void markAsUsed(final String token) {
        passwordResetTokenDao.markAsUsed(token);
    }
}