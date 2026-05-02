package ar.edu.itba.paw.webapp.controller.reservation;

import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.reservation.ReservationToken;
import ar.edu.itba.paw.models.reservation.ReservationTokenActionError;
import ar.edu.itba.paw.services.reservation.ReservationService;
import ar.edu.itba.paw.services.reservation.ReservationTokenService;
import ar.edu.itba.paw.services.reservation.ReservationTokenService.TokenValidationResult;
import ar.edu.itba.paw.services.reservation.ReservationServiceResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import ar.edu.itba.paw.webapp.controller.helpers.AuthenticatedUserResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Controller
@RequestMapping("/reservations")
public class ReservationTokenController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservationTokenController.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private final ReservationTokenService reservationTokenService;
    private final ReservationService reservationService;
    private final ZoneId displayZone;
    private final AuthenticatedUserResolver authResolver;

    @Autowired
    public ReservationTokenController(final ReservationTokenService reservationTokenService,
            final ReservationService reservationService,
            final AuthenticatedUserResolver authResolver,
            final ZoneId businessZone) {
        this.reservationTokenService = reservationTokenService;
        this.reservationService = reservationService;
        this.authResolver = authResolver;
        this.displayZone = businessZone;
    }

    private String formatUtcDateTimeForDisplay(final LocalDateTime utc) {
        if (utc == null) {
            return null;
        }
        return ZonedDateTime.of(utc, ZoneOffset.UTC).withZoneSameInstant(displayZone).format(DATE_FORMATTER);
    }

    @GetMapping("/accept")
    public String acceptGet(@RequestParam(required = false) final String token, final Model model,
            final Authentication authentication) {
        return handleConfirmGet(token, model, authentication, ReservationToken.Action.ACCEPT, "reservations/confirm-action");
    }

    @GetMapping("/reject")
    public String rejectGet(@RequestParam(required = false) final String token, final Model model,
            final Authentication authentication) {
        return handleConfirmGet(token, model, authentication, ReservationToken.Action.REJECT, "reservations/reject-confirm");
    }

    @PostMapping("/accept")
    public String acceptPost(@RequestParam(required = false) final String token,
                             @RequestParam(required = false) final String pickupCode,
                             final Model model,
                             final Authentication authentication) {
        return handleConsumePost(token, pickupCode, model, authentication, ReservationToken.Action.ACCEPT, "reservation.token.action.accepted");
    }

    @PostMapping("/reject")
    public String rejectPost(@RequestParam(required = false) final String token, final Model model,
            final Authentication authentication) {
        return handleConsumePost(token, null, model, authentication, ReservationToken.Action.REJECT, "reservation.token.action.rejected");
    }

    private String handleConfirmGet(final String token, final Model model, final Authentication authentication,
            final ReservationToken.Action action, final String viewName) {
        if (token == null || token.isBlank()) {
            model.addAttribute("tokenStatus", "invalid");
            return "reservations/token-status";
        }

        final TokenValidationResult result = reservationTokenService.validateOnly(token, action);

        if (result == TokenValidationResult.SUCCESS || result == TokenValidationResult.ALREADY_USED) {
            verifyTokenOwnership(token, authentication);
        }

        switch (result) {
            case SUCCESS: {
                final Long reservationId = reservationTokenService
                        .findReservationIdByToken(token)
                        .orElseThrow(() -> new IllegalStateException("Reservation id missing for token: " + token));
                final Optional<Reservation> reservation = reservationService.findById(reservationId);
                if (reservation.isEmpty()) {
                    model.addAttribute("tokenStatus", "invalid");
                    return "reservations/token-status";
                }
                final Reservation res = reservation.get();
                model.addAttribute("reservation", res);
                model.addAttribute("token", token);

                if (res.getReservationDate() != null) {
                    model.addAttribute("reservationDateFormatted", formatUtcDateTimeForDisplay(res.getReservationDate()));
                }
                if (res.getPickupConfirmationDate() != null) {
                    model.addAttribute("pickupConfirmationDateFormatted",
                            formatUtcDateTimeForDisplay(res.getPickupConfirmationDate()));
                }

                if (action == ReservationToken.Action.ACCEPT) {
                    model.addAttribute("confirmEndpoint", "accept");
                }
                return viewName;
            }
            case ALREADY_USED:
                return buildAlreadyUsedView(token, model);
            case EXPIRED:
                model.addAttribute("tokenStatus", "expired");
                return "reservations/token-status";
            case NOT_FOUND:
            default:
                model.addAttribute("tokenStatus", "invalid");
                return "reservations/token-status";
        }
    }

    private String handleConsumePost(final String token, final String pickupCode, final Model model,
            final Authentication authentication, final ReservationToken.Action action,
            final String actionCode) {
        if (token == null || token.isBlank()) {
            model.addAttribute("tokenStatus", "invalid");
            return "reservations/token-status";
        }

        final Commerce commerce = authResolver.resolveCommerce(authentication);

        if (action == ReservationToken.Action.ACCEPT) {
            final ReservationServiceResult<ReservationTokenActionError> result = reservationTokenService.acceptReservationTokenWithPickupCode(
                    token, pickupCode, commerce.getUserId());
            return mapAcceptTokenResult(result, token, model, actionCode);
        }

        final ReservationServiceResult<ReservationTokenActionError> result = reservationTokenService.rejectReservationToken(
                token, commerce.getUserId());
        return mapTokenActionResult(result, token, model, actionCode);
    }

    private String mapTokenActionResult(final ReservationServiceResult<ReservationTokenActionError> result, final String token,
            final Model model, final String actionCode) {
        if (result.isSuccess()) {
            model.addAttribute("actionCode", actionCode);
            return "reservations/action-success";
        }

        final ReservationTokenActionError error = result.error().orElseThrow(IllegalStateException::new);
        switch (error) {
            case ALREADY_USED:
                return buildAlreadyUsedView(token, model);
            case EXPIRED:
                model.addAttribute("tokenStatus", "expired");
                return "reservations/token-status";
            case WRONG_COMMERCE:
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            case INVALID_TOKEN:
            case NOT_FOUND:
            default:
                model.addAttribute("tokenStatus", "invalid");
                return "reservations/token-status";
        }
    }

    private String mapAcceptTokenResult(final ReservationServiceResult<ReservationTokenActionError> result, final String token,
            final Model model, final String actionCode) {
        if (result.isSuccess()) {
            model.addAttribute("actionCode", actionCode);
            return "reservations/action-success";
        }

        final ReservationTokenActionError error = result.error().orElseThrow(IllegalStateException::new);
        switch (error) {
            case MISSING_PICKUP_CODE:
                result.reservation().ifPresent(reservation -> model.addAttribute("reservation", reservation));
                model.addAttribute("token", token);
                model.addAttribute("confirmEndpoint", "accept");
                return "reservations/confirm-action";
            case INVALID_PICKUP_CODE:
                result.reservation().ifPresent(reservation -> model.addAttribute("reservation", reservation));
                model.addAttribute("token", token);
                model.addAttribute("pickupError", "reservation.token.pickup.invalidCode");
                model.addAttribute("confirmEndpoint", "accept");
                return "reservations/confirm-action";
            default:
                return mapTokenActionResult(result, token, model, actionCode);
        }
    }

    private String buildAlreadyUsedView(final String token, final Model model) {
        final Optional<Long> reservationId = reservationTokenService.findReservationIdByToken(token);
        if (reservationId.isPresent()) {
            final Optional<Reservation> reservation = reservationService.findById(reservationId.get());
            if (reservation.isPresent() && reservation.get().getStatus() != null) {
                final Reservation.Status status = reservation.get().getStatus();
                if (status == Reservation.Status.PAID) {
                    model.addAttribute("alreadyUsedDetailCode", "reservation.token.status.used.accepted");
                } else if (status == Reservation.Status.CANCELED) {
                    model.addAttribute("alreadyUsedDetailCode", "reservation.token.status.used.rejected");
                }
            }
        }
        model.addAttribute("tokenStatus", "already-used");
        return "reservations/token-status";
    }

    /**
     * Verifies that the authenticated commerce user owns the reservation behind the token.
     * Used for GET (display-only) endpoints; POST endpoints delegate ownership to the service layer.
     */
    private void verifyTokenOwnership(final String token, final Authentication authentication) {
        final Commerce commerce = authResolver.resolveCommerce(authentication);
        final Long reservationId = reservationTokenService.findReservationIdByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        try {
            reservationService.validateReservationBelongsToCommerce(reservationId, commerce.getUserId());
        } catch (final IllegalArgumentException ex) {
            LOGGER.debug("Token GET ownership check failed commerceUserId={} reservationId={}",
                    Long.valueOf(commerce.getUserId()), reservationId, ex);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
