package ar.edu.itba.paw.webapp.controller.reservation;

import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.user.ClientService;
import ar.edu.itba.paw.services.commerce.CommerceService;
import ar.edu.itba.paw.services.pack.PackService;
import ar.edu.itba.paw.services.reservation.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
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
public class ReservationListController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
    private static final int PAGE_SIZE = 6;

    private final ReservationService reservationService;
    private final PackService packService;
    private final CommerceService commerceService;
    private final ClientService clientService;
    private final ZoneId displayZone;
    private final AuthenticatedUserResolver authResolver;

    @Autowired
    public ReservationListController(final ReservationService reservationService,
            final PackService packService,
            final CommerceService commerceService,
            final ClientService clientService,
            final AuthenticatedUserResolver authResolver,
            final ZoneId businessZone) {
        this.reservationService = reservationService;
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
            baseUrl.append(firstParam ? "?" : "&").append("q=").append(URLEncoder.encode(query.trim(), StandardCharsets.UTF_8));
            firstParam = false;
        }
        if (statusFilter != null) {
            baseUrl.append(firstParam ? "?" : "&").append("status=").append(statusFilter.name());
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
            if (reservation.getPackId() == null) continue;
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
}
