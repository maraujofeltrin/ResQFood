package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.notification.Notification;
import ar.edu.itba.paw.models.notification.NotificationType;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.user.User;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Primary
@Repository
public class NotificationJpaDao implements NotificationDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Notification create(final Long recipientId, final NotificationType type,
            final Long reservationId, final Long auctionId, final Long packId,
            final String packTitle, final String commerceName, final Double amount,
            final String pickupCode, final LocalDateTime pickupDate, final LocalDateTime createdAt) {
        final User recipient = em.getReference(User.class, recipientId);
        final Reservation reservation = reservationId == null ? null : em.getReference(Reservation.class, reservationId);
        final Auction auction = auctionId == null ? null : em.getReference(Auction.class, auctionId);
        final Pack pack = packId == null ? null : em.getReference(Pack.class, packId);
        final Notification notification = new Notification(null, recipient, type,
                reservation, auction, pack, packTitle, commerceName, amount, pickupCode, pickupDate,
                createdAt, null, null);
        em.persist(notification);
        em.flush();
        return notification;
    }

    @Override
    public Optional<Notification> findById(final Long id) {
        return Optional.ofNullable(em.find(Notification.class, id));
    }

    @Override
    public List<Notification> findRecentByRecipient(final Long userId, final int limit) {
        final List<?> rawIds = em.createNativeQuery(
                        "SELECT n.id FROM notifications n "
                        + "WHERE n.recipient_id = :uid AND n.deleted_at IS NULL "
                        + "ORDER BY n.created_at DESC")
                .setParameter("uid", userId)
                .setMaxResults(Math.max(1, limit))
                .getResultList();
        if (rawIds.isEmpty()) {
            return Collections.emptyList();
        }
        final List<Long> ids = new ArrayList<>(rawIds.size());
        for (final Object rawId : rawIds) {
            ids.add(rawId instanceof Number ? ((Number) rawId).longValue() : Long.parseLong(rawId.toString()));
        }
        return em.createQuery(
                        "FROM Notification n "
                        + "LEFT JOIN FETCH n.reservation "
                        + "LEFT JOIN FETCH n.auction "
                        + "LEFT JOIN FETCH n.pack p "
                        + "LEFT JOIN FETCH p.auction "
                        + "WHERE n.id IN :ids "
                        + "ORDER BY n.createdAt DESC",
                        Notification.class)
                .setParameter("ids", ids)
                .getResultList();
    }

    @Override
    public int countUnread(final Long userId) {
        final Number count = em.createQuery(
                "SELECT COUNT(n) FROM Notification n WHERE n.recipient.id = :uid AND n.readAt IS NULL AND n.deletedAt IS NULL",
                Number.class)
                .setParameter("uid", userId)
                .getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public Optional<Notification> markRead(final Long id, final LocalDateTime readAt) {
        final Notification notification = em.find(Notification.class, id);
        if (notification == null || notification.getDeletedAt() != null) {
            return Optional.empty();
        }
        notification.setReadAt(readAt);
        return Optional.of(notification);
    }

    @Override
    public Optional<Notification> markUnread(final Long id) {
        final Notification notification = em.find(Notification.class, id);
        if (notification == null || notification.getDeletedAt() != null) {
            return Optional.empty();
        }
        notification.setReadAt(null);
        return Optional.of(notification);
    }

    @Override
    public int markAllRead(final Long userId, final LocalDateTime readAt) {
        return em.createQuery(
                "UPDATE Notification n SET n.readAt = :readAt WHERE n.recipient.id = :uid AND n.readAt IS NULL AND n.deletedAt IS NULL")
                .setParameter("readAt", readAt)
                .setParameter("uid", userId)
                .executeUpdate();
    }

    @Override
    public Optional<Notification> softDelete(final Long id, final LocalDateTime deletedAt) {
        final Notification notification = em.find(Notification.class, id);
        if (notification == null || notification.getDeletedAt() != null) {
            return Optional.empty();
        }
        notification.setDeletedAt(deletedAt);
        return Optional.of(notification);
    }

    @Override
    public boolean belongsToRecipient(final Long notificationId, final Long userId) {
        final Number count = em.createQuery(
                "SELECT COUNT(n) FROM Notification n WHERE n.id = :nid AND n.recipient.id = :uid",
                Number.class)
                .setParameter("nid", notificationId)
                .setParameter("uid", userId)
                .getSingleResult();
        return count != null && count.intValue() > 0;
    }
}
