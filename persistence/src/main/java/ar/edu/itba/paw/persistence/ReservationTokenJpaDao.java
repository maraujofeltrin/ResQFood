package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.reservation.ReservationToken;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.Optional;

@Primary
@Repository("reservationTokenJpaDao")
public class ReservationTokenJpaDao implements ReservationTokenDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public ReservationToken create(final String token, final Long reservationId, final ReservationToken.Action action,
            final LocalDateTime createdAt, final LocalDateTime expiresAt) {
        final ReservationToken rt = new ReservationToken(token, reservationId, action, false, createdAt, expiresAt);
        em.persist(rt);
        return rt;
    }

    @Override
    public Optional<ReservationToken> findByToken(final String token) {
        return Optional.ofNullable(em.find(ReservationToken.class, token));
    }

    @Override
    public void markAsUsed(final String token) {
        final int updated = em.createQuery("UPDATE ReservationToken rt SET rt.used = true WHERE rt.token = :token")
            .setParameter("token", token)
            .executeUpdate();
        if (updated <= 0) {
            throw new IllegalArgumentException("Reservation token not found");
        }
    }
}
