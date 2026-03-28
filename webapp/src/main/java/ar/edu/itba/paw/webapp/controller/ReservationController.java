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
    public String acceptPost(@RequestParam(required = false) final String token, final Model model) {
        return handleConsumePost(token, model, ReservationToken.Action.ACCEPT, "ACEPTADA");
    }

    @PostMapping("/reject")
    public String rejectPost(@RequestParam(required = false) final String token, final Model model) {
        return handleConsumePost(token, model, ReservationToken.Action.REJECT, "RECHAZADA");
    }

    private String handleConfirmGet(final String token, final Model model, final ReservationToken.Action action,
            final String viewName) {
        if (token == null || token.isBlank()) {
            return "reservations/token-invalid";
        }
        final TokenValidationResult result = reservationTokenService.validateOnly(token, action);
        switch (result) {
            case SUCCESS:
                final Long reservationId = reservationTokenService
                        .findReservationIdByToken(token)
                        .orElseThrow(() -> new IllegalStateException("Reservation id missing for token: " + token));
                final Optional<Reservation> reservation = reservationService.findById(reservationId);
                if (reservation.isEmpty()) {
                    return "reservations/token-invalid";
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
                return "reservations/token-already-used";
            case EXPIRED:
                return "reservations/token-expired";
            case NOT_FOUND:
            default:
                return "reservations/token-invalid";
        }
    }

    private String handleConsumePost(final String token, final Model model, final ReservationToken.Action action,
            final String actionLabel) {
        if (token == null || token.isBlank()) {
            return "reservations/token-invalid";
        }
        final TokenValidationResult result = reservationTokenService.validateAndConsume(token, action);
        switch (result) {
            case SUCCESS:
                model.addAttribute("action", actionLabel);
                return "reservations/action-success";
            case ALREADY_USED:
                return "reservations/token-already-used";
            case EXPIRED:
                return "reservations/token-expired";
            case NOT_FOUND:
            default:
                return "reservations/token-invalid";
        }
    }
}
