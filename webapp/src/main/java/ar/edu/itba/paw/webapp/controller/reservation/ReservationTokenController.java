package ar.edu.itba.paw.webapp.controller.reservation;

import ar.edu.itba.paw.models.reservation.AlreadyUsedTokenStatus;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.reservation.ReservationToken;
import ar.edu.itba.paw.models.reservation.ReservationTokenActionError;
import ar.edu.itba.paw.services.reservation.ReservationService;
import ar.edu.itba.paw.services.reservation.ReservationService.TokenValidationResult;
import ar.edu.itba.paw.services.reservation.ReservationServiceResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Controller
@RequestMapping("/reservations")
public class ReservationTokenController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private final ReservationService reservationService;
    private final ZoneId displayZone;

    @Autowired
    public ReservationTokenController(final ReservationService reservationService,
            final ZoneId businessZone) {
        this.reservationService = reservationService;
        this.displayZone = businessZone;
    }

    private String formatUtcDateTimeForDisplay(final LocalDateTime utc) {
        if (utc == null) {
            return null;
        }
        return ZonedDateTime.of(utc, ZoneOffset.UTC).withZoneSameInstant(displayZone).format(DATE_FORMATTER);
    }

    @GetMapping("/accept")
    @PreAuthorize("#token == null or #token.isEmpty() or @own.canWriteToken(#token, authentication.principal.id)")
    public String acceptGet(@RequestParam(required = false) final String token, final Model model) {
        return handleConfirmGet(token, model, ReservationToken.Action.ACCEPT, "reservations/confirm-action");
    }

    @GetMapping("/reject")
    @PreAuthorize("#token == null or #token.isEmpty() or @own.canWriteToken(#token, authentication.principal.id)")
    public String rejectGet(@RequestParam(required = false) final String token, final Model model) {
        return handleConfirmGet(token, model, ReservationToken.Action.REJECT, "reservations/reject-confirm");
    }

    @PostMapping("/accept")
    @PreAuthorize("#token == null or #token.isEmpty() or @own.canWriteToken(#token, authentication.principal.id)")
    public String acceptPost(@RequestParam(required = false) final String token,
                             @RequestParam(required = false) final String pickupCode,
                             final Model model) {
        return handleConsumePost(token, pickupCode, model, ReservationToken.Action.ACCEPT, "reservation.token.action.accepted");
    }

    @PostMapping("/reject")
    @PreAuthorize("#token == null or #token.isEmpty() or @own.canWriteToken(#token, authentication.principal.id)")
    public String rejectPost(@RequestParam(required = false) final String token, final Model model) {
        return handleConsumePost(token, null, model, ReservationToken.Action.REJECT, "reservation.token.action.rejected");
    }

    private String handleConfirmGet(final String token, final Model model,
            final ReservationToken.Action action, final String viewName) {
        if (token == null || token.isBlank()) {
            model.addAttribute("tokenStatus", "invalid");
            return "reservations/token-status";
        }

        final TokenValidationResult result = reservationService.validateToken(token, action);

        switch (result) {
            case SUCCESS: {
                final Long reservationId = reservationService
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
            final ReservationToken.Action action, final String actionCode) {
        if (token == null || token.isBlank()) {
            model.addAttribute("tokenStatus", "invalid");
            return "reservations/token-status";
        }

        if (action == ReservationToken.Action.ACCEPT) {
            final ReservationServiceResult<ReservationTokenActionError> result =
                    reservationService.acceptByToken(token, pickupCode);
            return mapAcceptTokenResult(result, token, model, actionCode);
        }

        final ReservationServiceResult<ReservationTokenActionError> result =
                reservationService.rejectByToken(token);
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
        reservationService.getAlreadyUsedTokenStatus(token)
                .map(AlreadyUsedTokenStatus::getDetailCode)
                .ifPresent(code -> model.addAttribute("alreadyUsedDetailCode", code));
        model.addAttribute("tokenStatus", "already-used");
        return "reservations/token-status";
    }
}
