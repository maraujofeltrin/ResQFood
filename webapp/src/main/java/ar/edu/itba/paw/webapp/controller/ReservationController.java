package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.models.Commerce;
import ar.edu.itba.paw.models.Pack;
import ar.edu.itba.paw.models.Reservation;
import ar.edu.itba.paw.models.ReservationToken;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.services.CommerceService;
import ar.edu.itba.paw.services.PackService;
import ar.edu.itba.paw.services.ReservationService;
import ar.edu.itba.paw.services.ReservationTokenService;
import ar.edu.itba.paw.services.ReservationTokenService.TokenValidationResult;
import ar.edu.itba.paw.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

@Controller
@RequestMapping("/reservations")
public class ReservationController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
    private static final int PAGE_SIZE = 6;

    private final ReservationTokenService reservationTokenService;
    private final ReservationService reservationService;
    private final UserService userService;
    private final PackService packService;
    private final CommerceService commerceService;
    private final ZoneId displayZone;

    @Autowired
    public ReservationController(final ReservationTokenService reservationTokenService,
            final ReservationService reservationService,
            final UserService userService,
            final PackService packService,
            final CommerceService commerceService,
            @Value("${app.display-zone:}") final String displayZoneStr) {
        this.reservationTokenService = reservationTokenService;
        this.reservationService = reservationService;
        this.userService = userService;
        this.packService = packService;
        this.commerceService = commerceService;
        this.displayZone = (displayZoneStr == null || displayZoneStr.trim().isEmpty())
                ? ZoneId.of("America/Argentina/Buenos_Aires")
                : ZoneId.of(displayZoneStr.trim());
    }

    private String formatUtcDateTimeForDisplay(final LocalDateTime utc) {
        if (utc == null) {
            return null;
        }
        return ZonedDateTime.of(utc, ZoneOffset.UTC).withZoneSameInstant(displayZone).format(DATE_FORMATTER);
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

    @GetMapping("/mine")
    public ModelAndView myReservations(@RequestParam(value = "page", defaultValue = "1") final int page,
            final Authentication authentication) {
        final User currentUser = resolveCurrentUser(authentication);
        if (currentUser.getRole() != User.Role.CLIENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        final List<Reservation> allReservations = new ArrayList<>(
            reservationService.findByCustomerId(currentUser.getId()));
        allReservations.sort(Comparator.comparing(Reservation::getReservationDate,
                Comparator.nullsLast(LocalDateTime::compareTo)).reversed());

        final int totalPages = Math.max(1, (int) Math.ceil((double) allReservations.size() / PAGE_SIZE));
        final int safePage = Math.max(1, Math.min(page, totalPages));
        final int fromIdx = (safePage - 1) * PAGE_SIZE;
        final int toIdx = Math.min(fromIdx + PAGE_SIZE, allReservations.size());
        final List<Reservation> reservations = allReservations.subList(fromIdx, toIdx);

        final Map<Long, Pack> packsByReservationId = new HashMap<>();
        final Map<Long, String> commerceNamesByReservationId = new HashMap<>();
        final Map<Long, String> formattedReservationDatesById = new HashMap<>();

        for (final Reservation reservation : reservations) {
            if (reservation.getReservationDate() != null) {
                formattedReservationDatesById.put(reservation.getId(),
                        formatUtcDateTimeForDisplay(reservation.getReservationDate()));
            }

            if (reservation.getPackId() == null) {
                continue;
            }

            packService.findById(reservation.getPackId()).ifPresent(pack -> {
                packsByReservationId.put(reservation.getId(), pack);
                final String commerceName = commerceService.findByUserId(pack.getCommerceId())
                        .map(Commerce::getCommercialName)
                        .filter(name -> name != null && !name.isBlank())
                        .orElse("-");
                commerceNamesByReservationId.put(reservation.getId(), commerceName);
            });
        }

        final ModelAndView mav = new ModelAndView("reservations/myReservations");
        mav.addObject("reservations", reservations);
        mav.addObject("packsByReservationId", packsByReservationId);
        mav.addObject("commerceNamesByReservationId", commerceNamesByReservationId);
        mav.addObject("formattedReservationDatesById", formattedReservationDatesById);
        mav.addObject("currentPage", safePage);
        mav.addObject("totalPages", totalPages);
        mav.addObject("paginationBaseUrl", "/reservations/mine");
        return mav;
    }

    private User resolveCurrentUser(final Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        final String email = authentication.getName();
        if (email == null || email.isBlank() || "anonymousUser".equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return userService.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
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
