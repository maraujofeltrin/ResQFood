package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Reservation;
import ar.edu.itba.paw.models.ReservationToken;
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

    @Autowired
    public ReservationTokenServiceImpl(final ReservationTokenDao reservationTokenDao,
            final ReservationDao reservationDao) {
        this.reservationTokenDao = reservationTokenDao;
        this.reservationDao = reservationDao;
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
            reservationDao.updateStatus(reservationToken.getReservationId(), Reservation.Status.CANCELED);
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
