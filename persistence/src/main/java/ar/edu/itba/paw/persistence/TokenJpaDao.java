package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.security.Token;
import ar.edu.itba.paw.models.security.TokenType;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.Optional;

@Primary
@Repository
public class TokenJpaDao implements TokenDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Token create(final String token, final Long userId, final TokenType type, final LocalDateTime createdAt, final LocalDateTime expiresAt) {
        final Token newToken = new Token(token, userId, false, type, createdAt, expiresAt);
        em.persist(newToken);
        return newToken;
    }

    @Override
    public Optional<Token> findByTokenAndType(final String token, final TokenType type) {
        return em.createQuery("FROM Token t WHERE t.token = :token AND t.type = :type", Token.class)
                .setParameter("token", token)
                .setParameter("type", type)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public void markAsUsed(final String token, final TokenType type) {
        int updated = em.createQuery("UPDATE Token t SET t.used = true WHERE t.token = :token AND t.type = :type")
                .setParameter("token", token)
                .setParameter("type", type)
                .executeUpdate();
        if (updated == 0) {
            throw new IllegalArgumentException("Token not found or type mismatch");
        }
    }
}
