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

import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
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

    private static boolean containsIgnoreCase(final String value, final String needle) {
        return value != null && needle != null && value.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }

    private static Reservation.Status parseStatusFilter(final String statusValue) {
        if (statusValue == null || statusValue.isBlank()) {
            return null;
        }
        try {
            return Reservation.Status.valueOf(statusValue.trim().toUpperCase(Locale.ROOT));
        } catch (final IllegalArgumentException ex) {
            return null;
        }
    }

    private static String buildPaginationBaseUrl(final String query, final Reservation.Status statusFilter) {
        final StringBuilder baseUrl = new StringBuilder("/reservations/mine");
        boolean firstParam = true;

        if (query != null && !query.isBlank()) {
            baseUrl.append(firstParam ? "?" : "&")
                    .append("q=")
                    .append(URLEncoder.encode(query.trim(), StandardCharsets.UTF_8));
            firstParam = false;
        }

        if (statusFilter != null) {
            baseUrl.append(firstParam ? "?" : "&")
                    .append("status=")
                    .append(statusFilter.name());
        }

        return baseUrl.toString();
    }

    @GetMapping("/mine")
    public ModelAndView myReservations(@RequestParam(value = "page", defaultValue = "1") final int page,
            @RequestParam(value = "q", required = false) final String query,
            @RequestParam(value = "status", required = false) final String status,
            final Authentication authentication) {
        final User currentUser = resolveCurrentUser(authentication);
        if (currentUser.getRole() != User.Role.CLIENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        final String normalizedQuery = query == null ? "" : query.trim();
        final Reservation.Status statusFilter = parseStatusFilter(status);

        final List<Reservation> allReservations = new ArrayList<>(
            reservationService.findByCustomerId(currentUser.getId()));
        allReservations.sort(Comparator.comparing(Reservation::getReservationDate,
                Comparator.nullsLast(LocalDateTime::compareTo)).reversed());

        final List<Reservation> filteredReservations = new ArrayList<>();
        final Map<Long, Pack> allPacksByReservationId = new HashMap<>();
        final Map<Long, String> allCommerceNamesByReservationId = new HashMap<>();

        for (final Reservation reservation : allReservations) {
            Pack pack = null;
            String commerceName = "-";

            if (reservation.getPackId() != null) {
                final Optional<Pack> packOpt = packService.findById(reservation.getPackId());
                if (packOpt.isPresent()) {
                    pack = packOpt.get();
                    commerceName = commerceService.findByUserId(pack.getCommerceId())
                            .map(Commerce::getCommercialName)
                            .filter(name -> name != null && !name.isBlank())
                            .orElse("-");
                }
            }

            final boolean matchesStatus = statusFilter == null || statusFilter.equals(reservation.getStatus());
            final boolean matchesQuery = normalizedQuery.isBlank()
                    || containsIgnoreCase(pack == null ? null : pack.getTitle(), normalizedQuery)
                    || containsIgnoreCase(pack == null ? null : pack.getDescription(), normalizedQuery)
                    || containsIgnoreCase(commerceName, normalizedQuery);

            if (matchesStatus && matchesQuery) {
                filteredReservations.add(reservation);
                if (pack != null) {
                    allPacksByReservationId.put(reservation.getId(), pack);
                }
                allCommerceNamesByReservationId.put(reservation.getId(), commerceName);
            }
        }

        final int totalPages = Math.max(1, (int) Math.ceil((double) filteredReservations.size() / PAGE_SIZE));
        final int safePage = Math.max(1, Math.min(page, totalPages));
        final int fromIdx = (safePage - 1) * PAGE_SIZE;
        final int toIdx = Math.min(fromIdx + PAGE_SIZE, filteredReservations.size());
        final List<Reservation> reservations = filteredReservations.subList(fromIdx, toIdx);

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

            if (allPacksByReservationId.containsKey(reservation.getId())) {
                packsByReservationId.put(reservation.getId(), allPacksByReservationId.get(reservation.getId()));
            }
            if (allCommerceNamesByReservationId.containsKey(reservation.getId())) {
                commerceNamesByReservationId.put(reservation.getId(), allCommerceNamesByReservationId.get(reservation.getId()));
            }
        }

        final ModelAndView mav = new ModelAndView("reservations/myReservations");
        mav.addObject("reservations", reservations);
        mav.addObject("packsByReservationId", packsByReservationId);
        mav.addObject("commerceNamesByReservationId", commerceNamesByReservationId);
        mav.addObject("formattedReservationDatesById", formattedReservationDatesById);
        mav.addObject("searchQuery", normalizedQuery);
        mav.addObject("selectedStatus", statusFilter == null ? "" : statusFilter.name());
        mav.addObject("statusOptions", Reservation.Status.values());
        mav.addObject("hasAnyReservations", !allReservations.isEmpty());
        mav.addObject("hasActiveFilters", !normalizedQuery.isBlank() || statusFilter != null);
        mav.addObject("currentPage", safePage);
        mav.addObject("totalPages", totalPages);
        mav.addObject("paginationBaseUrl", buildPaginationBaseUrl(normalizedQuery, statusFilter));
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
