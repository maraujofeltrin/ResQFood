package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.security.Token;
import ar.edu.itba.paw.models.security.TokenType;
import ar.edu.itba.paw.models.user.User;
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
        final User user = em.getReference(User.class, userId);
        final Token newToken = new Token(token, user, false, type, createdAt, expiresAt);
        em.persist(newToken);
        return newToken;
    }

    @Override
    public Optional<Token> findByTokenAndType(final String token, final TokenType type) {
        return em.createQuery("SELECT t FROM Token t JOIN FETCH t.user "
                        + "WHERE t.token = :token AND t.type = :type", Token.class)
                .setParameter("token", token)
                .setParameter("type", type)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public void markAsUsed(final String token, final TokenType type) {
        final Token entity = em.find(Token.class, token);
        if (entity == null || entity.getType() != type) {
            throw new IllegalArgumentException("Token not found or type mismatch");
        }
        entity.setUsed(true);
    }
}
