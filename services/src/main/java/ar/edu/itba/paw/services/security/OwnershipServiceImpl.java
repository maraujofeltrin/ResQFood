package ar.edu.itba.paw.services.security;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.notification.Notification;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.reservation.ReservationToken;
import ar.edu.itba.paw.persistence.AuctionDao;
import ar.edu.itba.paw.persistence.NotificationDao;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.persistence.ReservationDao;
import ar.edu.itba.paw.persistence.ReservationTokenDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service("own")
public class OwnershipServiceImpl implements OwnershipService {

    private final PackDao packDao;
    private final AuctionDao auctionDao;
    private final ReservationDao reservationDao;
    private final ReservationTokenDao reservationTokenDao;
    private final NotificationDao notificationDao;

    @Autowired
    public OwnershipServiceImpl(final PackDao packDao,
                                final AuctionDao auctionDao,
                                final ReservationDao reservationDao,
                                final ReservationTokenDao reservationTokenDao,
                                final NotificationDao notificationDao) {
        this.packDao = packDao;
        this.auctionDao = auctionDao;
        this.reservationDao = reservationDao;
        this.reservationTokenDao = reservationTokenDao;
        this.notificationDao = notificationDao;
    }

    @Transactional(readOnly = true)
    @Override
    public boolean canWritePack(final long packId, final long currentUserId) {
        final Pack pack = findPackOrThrow(packId);
        return pack.getCommerceId() != null && pack.getCommerceId().equals(currentUserId);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean canWriteAuction(final long auctionId, final long currentUserId) {
        final Optional<Auction> auctionOpt = auctionDao.findById(auctionId);
        if (auctionOpt.isEmpty()) {
            throw new OwnershipResourceNotFoundException("Auction not found");
        }
        final Pack pack = auctionOpt.get().getPack();
        if (pack == null || Boolean.TRUE.equals(pack.getDeleted())) {
            throw new OwnershipResourceNotFoundException("Auction pack not found");
        }
        return pack.getCommerceId() != null && pack.getCommerceId().equals(currentUserId);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean canWriteReservation(final long reservationId, final long currentUserId) {
        final Reservation reservation = findReservationOrThrow(reservationId);
        final Pack pack = reservation.getPack();
        if (pack == null) {
            throw new OwnershipResourceNotFoundException("Reservation pack not found");
        }
        return pack.getCommerceId() != null && pack.getCommerceId().equals(currentUserId);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean canWriteToken(final String token, final long currentUserId) {
        final Optional<ReservationToken> tokenOpt = reservationTokenDao.findByToken(token);
        if (tokenOpt.isEmpty()) {
            throw new OwnershipResourceNotFoundException("Token not found");
        }
        final Reservation reservation = tokenOpt.get().getReservation();
        final Pack pack = reservation.getPack();
        if (pack == null) {
            throw new OwnershipResourceNotFoundException("Pack not found");
        }
        return pack.getCommerceId() != null && pack.getCommerceId().equals(currentUserId);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean canWriteNotification(final long notificationId, final long currentUserId) {
        final Notification notif = notificationDao.findById(notificationId)
                .orElseThrow(() -> new OwnershipResourceNotFoundException("Notification not found"));
        if (notif.getDeletedAt() != null) {
            return false;
        }
        return notif.getRecipient().getId().equals(currentUserId);
    }

    private Pack findPackOrThrow(final long packId) {
        final Optional<Pack> packOpt = packDao.findById(packId);
        if (packOpt.isEmpty() || Boolean.TRUE.equals(packOpt.get().getDeleted())) {
            throw new OwnershipResourceNotFoundException("Pack not found");
        }
        return packOpt.get();
    }

    private Reservation findReservationOrThrow(final long reservationId) {
        final Optional<Reservation> reservationOpt = reservationDao.findByIdWithPackAndCommerce(reservationId);
        if (reservationOpt.isEmpty()) {
            throw new OwnershipResourceNotFoundException("Reservation not found");
        }
        return reservationOpt.get();
    }
}
