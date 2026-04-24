package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.reservation.ReservationToken;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.user.ClientService;
import ar.edu.itba.paw.services.commerce.CommerceService;
import ar.edu.itba.paw.services.pack.PackService;
import ar.edu.itba.paw.services.reservation.ReservationService;
import ar.edu.itba.paw.services.reservation.ReservationTokenService;
import ar.edu.itba.paw.services.reservation.ReservationTokenService.TokenValidationResult;
import ar.edu.itba.paw.services.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ar.edu.itba.paw.webapp.controller.utils.AuthenticatedUserResolver;

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
    private final ClientService clientService;
    private final ZoneId displayZone;
    private final AuthenticatedUserResolver authResolver;

    @Autowired
    public ReservationController(final ReservationTokenService reservationTokenService,
            final ReservationService reservationService,
            final UserService userService,
            final PackService packService,
            final CommerceService commerceService,
            final ClientService clientService,
            final AuthenticatedUserResolver authResolver,
            final ZoneId businessZone) {
        this.reservationTokenService = reservationTokenService;
        this.reservationService = reservationService;
        this.userService = userService;
        this.packService = packService;
        this.commerceService = commerceService;
        this.clientService = clientService;
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

    @PostMapping("/{id}/reject")
    public String rejectReservationFromCard(@PathVariable("id") final Long reservationId,
            @RequestParam(value = "page", required = false) final Integer page,
            @RequestParam(value = "q", required = false) final String query,
            @RequestParam(value = "status", required = false) final String status,
            final Authentication authentication,
            final RedirectAttributes redirectAttributes) {
        final User currentUser = authResolver.resolveUser(authentication);
        if (currentUser.getRole() != User.Role.COMMERCE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        try {
            reservationService.rejectReservationForCommerce(reservationId, currentUser.getId());
            redirectAttributes.addFlashAttribute("reservationActionKind", "success");
            redirectAttributes.addFlashAttribute("reservationActionMessageCode", "commerce.reservations.action.reject.success");
        } catch (final IllegalArgumentException ex) {
            if ("WRONG_COMMERCE".equals(ex.getMessage())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
            redirectAttributes.addFlashAttribute("reservationActionKind", "error");
            redirectAttributes.addFlashAttribute("reservationActionMessageCode", "commerce.reservations.action.reject.error");
        } catch (final IllegalStateException ex) {
            final String messageCode;
            if ("ALREADY_CANCELED".equals(ex.getMessage())) {
                messageCode = "commerce.reservations.action.reject.alreadyCanceled";
            } else if ("ALREADY_COMPLETED".equals(ex.getMessage())) {
                messageCode = "commerce.reservations.action.reject.alreadyCompleted";
            } else if ("INVALID_STATUS".equals(ex.getMessage())) {
                messageCode = "commerce.reservations.action.reject.invalidStatus";
            } else {
                messageCode = "commerce.reservations.action.reject.error";
            }
            redirectAttributes.addFlashAttribute("reservationActionKind", "error");
            redirectAttributes.addFlashAttribute("reservationActionMessageCode", messageCode);
        }

        return "redirect:" + buildReservationsRedirectUrl(page, query, status);
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
        final StringBuilder baseUrl = new StringBuilder("/reservations");
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

    private static String buildReservationsRedirectUrl(final Integer page, final String query, final String status) {
        final StringBuilder baseUrl = new StringBuilder("/reservations");
        boolean firstParam = true;

        if (page != null && page > 1) {
            baseUrl.append(firstParam ? "?" : "&")
                    .append("page=")
                    .append(page);
            firstParam = false;
        }

        if (query != null && !query.isBlank()) {
            baseUrl.append(firstParam ? "?" : "&")
                    .append("q=")
                    .append(URLEncoder.encode(query.trim(), StandardCharsets.UTF_8));
            firstParam = false;
        }

        final Reservation.Status statusFilter = parseStatusFilter(status);
        if (statusFilter != null) {
            baseUrl.append(firstParam ? "?" : "&")
                    .append("status=")
                    .append(statusFilter.name());
        }

        return baseUrl.toString();
    }

    @GetMapping
    public ModelAndView reservations(@RequestParam(value = "page", defaultValue = "1") final int page,
            @RequestParam(value = "q", required = false) final String query,
            @RequestParam(value = "status", required = false) final String status,
            final Authentication authentication) {
        final User currentUser = authResolver.resolveUser(authentication);
        final boolean isCommerce = currentUser.getRole() == User.Role.COMMERCE;
        final boolean isClient = currentUser.getRole() == User.Role.CLIENT;

        if (!isCommerce && !isClient) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        final String normalizedQuery = query == null ? "" : query.trim();
        final Reservation.Status statusFilter = parseStatusFilter(status);

        List<Reservation> allReservations;
        if (isCommerce) {
            final Commerce commerce = commerceService.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
            allReservations = new ArrayList<>(reservationService.findByCommerceId(commerce.getUserId()));
        } else {
            allReservations = new ArrayList<>(reservationService.findByCustomerId(currentUser.getId()));
        }

        allReservations.sort(Comparator.comparing(Reservation::getReservationDate,
                Comparator.nullsLast(LocalDateTime::compareTo)).reversed());

        final List<Reservation> filteredReservations = new ArrayList<>();
        final Map<Long, Pack> allPacksByReservationId = new HashMap<>();
        final Map<Long, String> allCommerceNamesByReservationId = new HashMap<>();
        final Map<Long, String> allClientNamesByReservationId = new HashMap<>();

        for (final Reservation reservation : allReservations) {
            Pack pack = null;
            String commerceName = "-";
            String clientName = "-";

            if (reservation.getPackId() != null) {
                final Optional<Pack> packOpt = packService.findById(reservation.getPackId());
                if (packOpt.isPresent()) {
                    pack = packOpt.get();
                    if (isClient) {
                        commerceName = commerceService.findByUserId(pack.getCommerceId())
                                .map(Commerce::getCommercialName)
                                .filter(name -> name != null && !name.isBlank())
                                .orElse("-");
                    }
                }
            }

            if (isCommerce && reservation.getCustomerId() != null) {
                clientName = clientService.findByUserId(reservation.getCustomerId())
                        .map(Client::getFullName)
                        .orElse("-");
            }

            final boolean matchesStatus = statusFilter == null || statusFilter.equals(reservation.getStatus());
            final boolean matchesQuery = normalizedQuery.isBlank()
                    || containsIgnoreCase(pack == null ? null : pack.getTitle(), normalizedQuery)
                    || containsIgnoreCase(pack == null ? null : pack.getDescription(), normalizedQuery)
                    || (isClient && containsIgnoreCase(commerceName, normalizedQuery))
                    || (isCommerce && containsIgnoreCase(clientName, normalizedQuery));

            if (matchesStatus && matchesQuery) {
                filteredReservations.add(reservation);
                if (pack != null) {
                    allPacksByReservationId.put(reservation.getId(), pack);
                }
                if (isClient) {
                    allCommerceNamesByReservationId.put(reservation.getId(), commerceName);
                }
                if (isCommerce) {
                    allClientNamesByReservationId.put(reservation.getId(), clientName);
                }
            }
        }

        final int totalPages = Math.max(1, (int) Math.ceil((double) filteredReservations.size() / PAGE_SIZE));
        final int safePage = Math.max(1, Math.min(page, totalPages));
        final int fromIdx = (safePage - 1) * PAGE_SIZE;
        final int toIdx = Math.min(fromIdx + PAGE_SIZE, filteredReservations.size());
        final List<Reservation> reservations = filteredReservations.subList(fromIdx, toIdx);

        final Map<Long, Pack> packsByReservationId = new HashMap<>();
        final Map<Long, String> commerceNamesByReservationId = new HashMap<>();
        final Map<Long, String> clientNamesByReservationId = new HashMap<>();
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
            if (isClient && allCommerceNamesByReservationId.containsKey(reservation.getId())) {
                commerceNamesByReservationId.put(reservation.getId(), allCommerceNamesByReservationId.get(reservation.getId()));
            }
            if (isCommerce && allClientNamesByReservationId.containsKey(reservation.getId())) {
                clientNamesByReservationId.put(reservation.getId(), allClientNamesByReservationId.get(reservation.getId()));
            }
        }

        final ModelAndView mav = new ModelAndView("reservations/reservationsView");
        mav.addObject("reservations", reservations);
        mav.addObject("packsByReservationId", packsByReservationId);
        mav.addObject("commerceNamesByReservationId", commerceNamesByReservationId);
        mav.addObject("clientNamesByReservationId", clientNamesByReservationId);
        mav.addObject("formattedReservationDatesById", formattedReservationDatesById);
        mav.addObject("searchQuery", normalizedQuery);
        mav.addObject("selectedStatus", statusFilter == null ? "" : statusFilter.name());
        mav.addObject("statusOptions", Reservation.Status.values());
        mav.addObject("hasAnyReservations", !allReservations.isEmpty());
        mav.addObject("hasActiveFilters", !normalizedQuery.isBlank() || statusFilter != null);
        mav.addObject("currentPage", safePage);
        mav.addObject("totalPages", totalPages);
        mav.addObject("paginationBaseUrl", buildPaginationBaseUrl(normalizedQuery, statusFilter));
        mav.addObject("messagePrefix", isCommerce ? "commerce.reservations" : "reservation.my");
        return mav;
    }



    private String handleConfirmGet(final String token, final Model model, final Authentication authentication,
            final ReservationToken.Action action,
            final String viewName) {
        if (token == null || token.isBlank()) {
            model.addAttribute("tokenStatus", "invalid");
            return "reservations/token-status";
        }
        final TokenValidationResult result = reservationTokenService.validateOnly(token, action);
        if (result == TokenValidationResult.SUCCESS || result == TokenValidationResult.ALREADY_USED) {
            try {
                verifyReservationOwnership(token, authentication);
            } catch (final IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }
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

    private String handleConsumePost(final String token, final String pickupCode, final Model model,
            final Authentication authentication, final ReservationToken.Action action,
            final String actionCode) {
        if (token == null || token.isBlank()) {
            model.addAttribute("tokenStatus", "invalid");
            return "reservations/token-status";
        }
        final TokenValidationResult validate = reservationTokenService.validateOnly(token, action);
        if (validate == TokenValidationResult.SUCCESS || validate == TokenValidationResult.ALREADY_USED) {
            try {
                verifyReservationOwnership(token, authentication);
            } catch (final IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }
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
                        model.addAttribute("pickupError", "reservation.token.pickup.invalidCode");
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
                    model.addAttribute("actionCode", actionCode);
                    return "reservations/action-success";
                } else {
                    // Non-accept actions (e.g., REJECT): consume token and apply effect
                    final TokenValidationResult result = reservationTokenService.validateAndConsume(token, action);
                    switch (result) {
                        case SUCCESS:
                            model.addAttribute("actionCode", actionCode);
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
                    model.addAttribute("alreadyUsedDetailCode", "reservation.token.status.used.accepted");
                } else if (status == Reservation.Status.CANCELED) {
                    model.addAttribute("alreadyUsedDetailCode", "reservation.token.status.used.rejected");
                }
            }
        }
        model.addAttribute("tokenStatus", "already-used");
        return "reservations/token-status";
    }

    private void verifyReservationOwnership(final String token, final Authentication authentication) {
        final User currentUser = authResolver.resolveUser(authentication);
        if (currentUser.getRole() != User.Role.COMMERCE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        final Commerce commerce = commerceService.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        final Long reservationId = reservationTokenService.findReservationIdByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        reservationService.validateReservationBelongsToCommerce(reservationId, commerce.getUserId());
    }
}
