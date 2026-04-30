package ar.edu.itba.paw.services.reservation;

import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.reservation.ReservationToken;
import ar.edu.itba.paw.models.reservation.ReservationTokenActionError;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.persistence.ReservationDao;
import ar.edu.itba.paw.persistence.ReservationTokenDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Optional;

@Service
public class ReservationTokenServiceImpl implements ReservationTokenService {

    private final ReservationTokenDao reservationTokenDao;
    private final ReservationDao reservationDao;
    private final PackDao packDao;
    private final ReservationService reservationService;

    @Autowired
    public ReservationTokenServiceImpl(final ReservationTokenDao reservationTokenDao,
            final ReservationDao reservationDao,
            final PackDao packDao,
            final ReservationService reservationService) {
        this.reservationTokenDao = reservationTokenDao;
        this.reservationDao = reservationDao;
        this.packDao = packDao;
        this.reservationService = reservationService;
    }

    @Override
    public TokenValidationResult validateOnly(final String token, final ReservationToken.Action action) {
        return resolveValidation(token, action);
    }

    @Override
    public Optional<Long> findReservationIdByToken(final String token) {
        return reservationTokenDao.findByToken(token).map(ReservationToken::getReservationId);
    }

    @Transactional
    @Override
    public ReservationServiceResult<ReservationTokenActionError> acceptReservationTokenWithPickupCode(final String token,
            final String pickupCode, final Long commerceUserId) {
        final ReservationServiceResult<ReservationTokenActionError> common =
                validateTokenCommon(token, ReservationToken.Action.ACCEPT, commerceUserId);
        if (common != null) {
            return common;
        }

        final ReservationToken reservationToken = reservationTokenDao.findByToken(token).orElseThrow();
        final Reservation reservation = reservationDao.findById(reservationToken.getReservationId()).orElseThrow();

        if (pickupCode == null || pickupCode.isBlank()) {
            return ReservationServiceResult.failure(ReservationTokenActionError.MISSING_PICKUP_CODE, reservation);
        }

        final String inputCode = pickupCode.trim().toUpperCase(Locale.ROOT);
        final String storedCode = reservation.getPickupCode() == null ? ""
                : reservation.getPickupCode().trim().toUpperCase(Locale.ROOT);
        if (!inputCode.equals(storedCode)) {
            return ReservationServiceResult.failure(ReservationTokenActionError.INVALID_PICKUP_CODE, reservation);
        }

        reservationTokenDao.markAsUsed(token);
        final Reservation confirmed = reservationService.confirmPickup(reservation.getId());
        return ReservationServiceResult.success(confirmed);
    }

    @Transactional
    @Override
    public ReservationServiceResult<ReservationTokenActionError> rejectReservationToken(final String token,
            final Long commerceUserId) {
        final ReservationServiceResult<ReservationTokenActionError> common =
                validateTokenCommon(token, ReservationToken.Action.REJECT, commerceUserId);
        if (common != null) {
            return common;
        }

        final ReservationToken reservationToken = reservationTokenDao.findByToken(token).orElseThrow();
        final Reservation reservation = reservationDao.findById(reservationToken.getReservationId()).orElseThrow();

        reservationTokenDao.markAsUsed(token);
        reservationService.rejectReservation(reservation.getId());

        final Reservation updated = reservationDao.findById(reservation.getId()).orElseThrow();
        return ReservationServiceResult.success(updated);
    }

    /**
     * Shared validation for both accept and reject token flows.
     *
     * @return a failure result if validation fails, or {@code null} if the token is valid
     *         and the caller should proceed with the action-specific logic.
     */
    private ReservationServiceResult<ReservationTokenActionError> validateTokenCommon(final String token,
            final ReservationToken.Action expectedAction, final Long commerceUserId) {
        if (token == null || token.isBlank()) {
            return ReservationServiceResult.failure(ReservationTokenActionError.INVALID_TOKEN);
        }

        final Optional<ReservationToken> tokenOpt = reservationTokenDao.findByToken(token);
        if (tokenOpt.isEmpty()) {
            return ReservationServiceResult.failure(ReservationTokenActionError.NOT_FOUND);
        }
        final ReservationToken reservationToken = tokenOpt.get();
        if (reservationToken.getAction() != expectedAction) {
            return ReservationServiceResult.failure(ReservationTokenActionError.NOT_FOUND);
        }

        final Optional<Reservation> reservationOpt = reservationDao.findById(reservationToken.getReservationId());
        if (reservationOpt.isEmpty()) {
            return ReservationServiceResult.failure(ReservationTokenActionError.NOT_FOUND);
        }
        final Reservation reservation = reservationOpt.get();

        if (reservation.getStatus() == Reservation.Status.PAID
                || reservation.getStatus() == Reservation.Status.CANCELED
                || reservationToken.isUsed()) {
            return ReservationServiceResult.failure(ReservationTokenActionError.ALREADY_USED, reservation);
        }
        if (LocalDateTime.now(ZoneOffset.UTC).isAfter(reservationToken.getExpiresAt())) {
            return ReservationServiceResult.failure(ReservationTokenActionError.EXPIRED, reservation);
        }

        if (reservation.getPackId() == null) {
            return ReservationServiceResult.failure(ReservationTokenActionError.NOT_FOUND, reservation);
        }
        final boolean isOwned = packDao.findById(reservation.getPackId())
                .map(pack -> commerceUserId.equals(pack.getCommerceId()))
                .orElse(false);
        if (!isOwned) {
            return ReservationServiceResult.failure(ReservationTokenActionError.WRONG_COMMERCE, reservation);
        }

        return null;
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
        if (LocalDateTime.now(ZoneOffset.UTC).isAfter(reservationToken.getExpiresAt())) {
            return TokenValidationResult.EXPIRED;
        }
        return TokenValidationResult.SUCCESS;
    }
}
