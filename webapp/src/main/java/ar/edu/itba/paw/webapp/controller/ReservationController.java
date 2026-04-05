package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.models.Reservation;
import ar.edu.itba.paw.models.ReservationToken;
import ar.edu.itba.paw.services.ReservationService;
import ar.edu.itba.paw.services.ReservationTokenService;
import ar.edu.itba.paw.services.ReservationTokenService.TokenValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

@Controller
@RequestMapping("/reservations")
public class ReservationController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private final ReservationTokenService reservationTokenService;
    private final ReservationService reservationService;

    @Autowired
    public ReservationController(final ReservationTokenService reservationTokenService,
            final ReservationService reservationService) {
        this.reservationTokenService = reservationTokenService;
        this.reservationService = reservationService;
    }

    @GetMapping("/accept")
    public String acceptGet(@RequestParam(required = false) final String token, final Model model) {
        return handleConfirmGet(token, model, ReservationToken.Action.ACCEPT, "reservations/confirm-action");
    }

    @GetMapping("/reject")
    public String rejectGet(@RequestParam(required = false) final String token, final Model model) {
        return handleConfirmGet(token, model, ReservationToken.Action.REJECT, "reservations/reject-confirm");
    }

    @PostMapping("/accept")
    public String acceptPost(@RequestParam(required = false) final String token,
                             @RequestParam(required = false) final String pickupCode,
                             final Model model) {
        return handleConsumePost(token, pickupCode, model, ReservationToken.Action.ACCEPT, "ACEPTADA");
    }

    @PostMapping("/reject")
    public String rejectPost(@RequestParam(required = false) final String token, final Model model) {
        return handleConsumePost(token, null, model, ReservationToken.Action.REJECT, "RECHAZADA");
    }

    private String handleConfirmGet(final String token, final Model model, final ReservationToken.Action action,
            final String viewName) {
        if (token == null || token.isBlank()) {
            model.addAttribute("tokenStatus", "invalid");
            return "reservations/token-status";
        }
        final TokenValidationResult result = reservationTokenService.validateOnly(token, action);
        switch (result) {
            case SUCCESS:
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
                
                // Add formatted dates for display
                if (res.getReservationDate() != null) {
                    model.addAttribute("reservationDateFormatted", res.getReservationDate().format(DATE_FORMATTER));
                }
                if (res.getPickupConfirmationDate() != null) {
                    model.addAttribute("pickupConfirmationDateFormatted", res.getPickupConfirmationDate().format(DATE_FORMATTER));
                }
                
                if (action == ReservationToken.Action.ACCEPT) {
                    model.addAttribute("confirmEndpoint", "accept");
                }
                return viewName;
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

    private String handleConsumePost(final String token, final String pickupCode, final Model model, final ReservationToken.Action action,
            final String actionLabel) {
        if (token == null || token.isBlank()) {
            model.addAttribute("tokenStatus", "invalid");
            return "reservations/token-status";
        }
        final TokenValidationResult validate = reservationTokenService.validateOnly(token, action);
        switch (validate) {
            case SUCCESS:
                // If accepting, require pickup code to confirm pickup
                if (action == ReservationToken.Action.ACCEPT) {
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

                    if (pickupCode == null || pickupCode.isBlank()) {
                        // Show form to enter pickup code
                        model.addAttribute("confirmEndpoint", "accept");
                        return "reservations/confirm-action";
                    }

                    final String inputCode = pickupCode == null ? "" : pickupCode.trim().toUpperCase(Locale.ROOT);
                    final String storedCode = res.getPickupCode() == null ? ""
                            : res.getPickupCode().trim().toUpperCase(Locale.ROOT);
                    if (!inputCode.equals(storedCode)) {
                        model.addAttribute("pickupError", "Código de retiro inválido.");
                        model.addAttribute("confirmEndpoint", "accept");
                        return "reservations/confirm-action";
                    }

                    // pickup code correct -> consume token and confirm pickup
                    final TokenValidationResult result = reservationTokenService.validateAndConsume(token, action);
                    if (result != TokenValidationResult.SUCCESS) {
                        switch (result) {
                            case ALREADY_USED:
                                model.addAttribute("tokenStatus", "already-used");
                                return "reservations/token-status";
                            case EXPIRED:
                                model.addAttribute("tokenStatus", "expired");
                                return "reservations/token-status";
                            case NOT_FOUND:
                            default:
                                model.addAttribute("tokenStatus", "invalid");
                                return "reservations/token-status";
                        }
                    }

                    reservationService.confirmPickup(reservationId);
                    model.addAttribute("action", actionLabel);
                    return "reservations/action-success";
                } else {
                    // Non-accept actions (e.g., REJECT): consume token and apply effect
                    final TokenValidationResult result = reservationTokenService.validateAndConsume(token, action);
                    switch (result) {
                        case SUCCESS:
                            model.addAttribute("action", actionLabel);
                            return "reservations/action-success";
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

    private String buildAlreadyUsedView(final String token, final Model model) {
        final Optional<Long> reservationId = reservationTokenService.findReservationIdByToken(token);
        if (reservationId.isPresent()) {
            final Optional<Reservation> reservation = reservationService.findById(reservationId.get());
            if (reservation.isPresent() && reservation.get().getStatus() != null) {
                final Reservation.Status status = reservation.get().getStatus();
                if (status == Reservation.Status.PAID) {
                    model.addAttribute("alreadyUsedDetail", "Este pedido ya fue aceptado.");
                } else if (status == Reservation.Status.CANCELED) {
                    model.addAttribute("alreadyUsedDetail", "Este pedido ya fue rechazado.");
                }
            }
        }
        model.addAttribute("tokenStatus", "already-used");
        return "reservations/token-status";
    }
}
