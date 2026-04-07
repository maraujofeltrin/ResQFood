package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Reservation;
import ar.edu.itba.paw.models.ReservationToken;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.persistence.ReservationDao;
import ar.edu.itba.paw.persistence.ReservationTokenDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ReservationTokenServiceImpl implements ReservationTokenService {

    private final ReservationTokenDao reservationTokenDao;
    private final ReservationDao reservationDao;
    private final UserService userService;
    private final ReservationMailService reservationMailService;

    @Autowired
    public ReservationTokenServiceImpl(final ReservationTokenDao reservationTokenDao,
            final ReservationDao reservationDao,
            final UserService userService,
            final ReservationMailService reservationMailService) {
        this.reservationTokenDao = reservationTokenDao;
        this.reservationDao = reservationDao;
        this.userService = userService;
        this.reservationMailService = reservationMailService;
    }

    @Override
    public TokenValidationResult validateOnly(final String token, final ReservationToken.Action action) {
        return resolveValidation(token, action);
    }

    @Override
    @Transactional
    public TokenValidationResult validateAndConsume(final String token, final ReservationToken.Action action) {
        final TokenValidationResult validation = resolveValidation(token, action);
        if (validation != TokenValidationResult.SUCCESS) {
            return validation;
        }

        final ReservationToken reservationToken = reservationTokenDao
                .findByToken(token)
                .orElseThrow(() -> new IllegalStateException("Token expected after validation: " + token));

        reservationTokenDao.markAsUsed(token);

        // For REJECT, update reservation status immediately. For ACCEPT, controller will
        // verify pickup code and call reservationService.confirmPickup, so here we only
        // mark token as used for ACCEPT.
        if (action == ReservationToken.Action.REJECT) {
            final Reservation reservation = reservationDao.updateStatus(reservationToken.getReservationId(), Reservation.Status.CANCELED);
            final String clientEmail = userService.findById(reservation.getCustomerId())
                    .map(User::getEmail)
                    .orElseThrow(() -> new IllegalStateException("Customer user not found for reservation id: " + reservation.getId()));
            reservationMailService.sendReservationRejectedToClient(reservation, clientEmail);
        }

        return TokenValidationResult.SUCCESS;
    }

    @Override
    public Optional<Long> findReservationIdByToken(final String token) {
        return reservationTokenDao.findByToken(token).map(ReservationToken::getReservationId);
    }

    private TokenValidationResult resolveValidation(final String token, final ReservationToken.Action action) {
        final Optional<ReservationToken> optionalToken = reservationTokenDao.findByToken(token);
        if (optionalToken.isEmpty()) {
            return TokenValidationResult.NOT_FOUND;
        }
        final ReservationToken reservationToken = optionalToken.get();

        final Optional<Reservation> reservation = reservationDao.findById(reservationToken.getReservationId());
        if (reservation.isPresent()) {
            final Reservation.Status status = reservation.get().getStatus();
            
            if (status == Reservation.Status.PAID || status == Reservation.Status.CANCELED) {
                return TokenValidationResult.ALREADY_USED;
            }
        }

        if (reservationToken.getAction() != action) {
            return TokenValidationResult.NOT_FOUND;
        }
        if (reservationToken.isUsed()) {
            return TokenValidationResult.ALREADY_USED;
        }
        if (LocalDateTime.now().isAfter(reservationToken.getExpiresAt())) {
            return TokenValidationResult.EXPIRED;
        }
        return TokenValidationResult.SUCCESS;
    }
}
