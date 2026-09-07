package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.reservation.Reservation;
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
        final Reservation reservation = em.getReference(Reservation.class, reservationId);
        final ReservationToken rt = new ReservationToken(token, reservation, action, false, createdAt, expiresAt);
        em.persist(rt);
        return rt;
    }

    @Override
    public Optional<ReservationToken> findByToken(final String token) {
        return em.createQuery(
                "SELECT rt FROM ReservationToken rt "
                        + "JOIN FETCH rt.reservation r "
                        + "JOIN FETCH r.pack p JOIN FETCH p.commerce "
                        + "WHERE rt.token = :token",
                ReservationToken.class)
                .setParameter("token", token)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public void markAsUsed(final String token) {
        final ReservationToken rt = em.find(ReservationToken.class, token);
        if (rt == null) {
            throw new IllegalArgumentException("Reservation token not found");
        }
        rt.setUsed(true);
    }
}
