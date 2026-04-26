package ar.edu.itba.paw.webapp.controller.reservation;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.auction.Bid;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.auction.AuctionService;
import ar.edu.itba.paw.services.user.ClientService;
import ar.edu.itba.paw.services.commerce.CommerceService;
import ar.edu.itba.paw.services.pack.PackService;
import ar.edu.itba.paw.services.reservation.ReservationService;
import ar.edu.itba.paw.webapp.controller.utils.AuthenticatedUserResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/reservations")
public class ReservationListController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
    private static final int PAGE_SIZE = 6;

    private final ReservationService reservationService;
    private final PackService packService;
    private final CommerceService commerceService;
    private final ClientService clientService;
    private final AuctionService auctionService;
    private final ZoneId displayZone;
    private final AuthenticatedUserResolver authResolver;

    @Autowired
    public ReservationListController(final ReservationService reservationService,
            final PackService packService,
            final CommerceService commerceService,
            final ClientService clientService,
            final AuctionService auctionService,
            final AuthenticatedUserResolver authResolver,
            final ZoneId businessZone) {
        this.reservationService = reservationService;
        this.packService = packService;
        this.commerceService = commerceService;
        this.clientService = clientService;
        this.auctionService = auctionService;
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

    private static Auction.Status parseAuctionStatusFilter(final String statusValue) {
        if (statusValue == null || statusValue.isBlank()) {
            return null;
        }
        try {
            return Auction.Status.valueOf(statusValue.trim().toUpperCase(Locale.ROOT));
        } catch (final IllegalArgumentException ex) {
            return null;
        }
    }

    private static String normalizeClientTab(final String tab) {
        if (tab == null) {
            return "items";
        }
        final String t = tab.trim().toLowerCase(Locale.ROOT);
        if ("packs".equals(t) || "auctions".equals(t)) {
            return t;
        }
        return "items";
    }

    private Set<Long> auctionPackIdsForReservationPacks(final List<Reservation> reservations) {
        final Set<Long> distinct = new HashSet<>();
        for (final Reservation r : reservations) {
            if (r.getPackId() != null) {
                distinct.add(r.getPackId());
            }
        }
        final Set<Long> auctionPacks = new HashSet<>();
        for (final Long packId : distinct) {
            auctionService.findByPackId(packId).ifPresent(a -> auctionPacks.add(packId));
        }
        return auctionPacks;
    }

    private static Set<Long> reservationIdsWithAuctionPack(final List<Reservation> reservations,
            final Set<Long> auctionPackIds) {
        final Set<Long> ids = new HashSet<>();
        for (final Reservation r : reservations) {
            if (r.getPackId() != null && auctionPackIds.contains(r.getPackId())) {
                ids.add(r.getId());
            }
        }
        return ids;
    }

    /**
     * @return badge code for the participation card, or {@code null} when no badge should be shown (cancelled, or finished
     *         and the client won).
     */
    private static String clientParticipationAuctionBadge(final Auction auction, final boolean leading) {
        if (auction.getStatus() == Auction.Status.CANCELLED) {
            return null;
        }
        if (auction.isActive()) {
            return leading ? "WINNING" : "OUTBID_ACTIVE";
        }
        if (leading) {
            return null;
        }
        return "OUTBID_FINISHED";
    }

    private static String buildCommercePaginationBaseUrl(final String query, final Reservation.Status statusFilter) {
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

    private static String buildClientPaginationBaseUrl(final String tab, final String query,
            final Reservation.Status reservationStatus, final Auction.Status auctionStatus) {
        final StringBuilder baseUrl = new StringBuilder("/reservations?tab=");
        baseUrl.append(tab);
        if (query != null && !query.isBlank()) {
            baseUrl.append("&q=").append(URLEncoder.encode(query.trim(), StandardCharsets.UTF_8));
        }
        if (reservationStatus != null) {
            baseUrl.append("&status=").append(reservationStatus.name());
        }
        if (auctionStatus != null) {
            baseUrl.append("&auctionStatus=").append(auctionStatus.name());
        }
        return baseUrl.toString();
    }

    @GetMapping
    public ModelAndView reservations(
            @RequestParam(value = "page", defaultValue = "1") final int page,
            @RequestParam(value = "q", required = false) final String query,
            @RequestParam(value = "status", required = false) final String status,
            @RequestParam(value = "auctionStatus", required = false) final String auctionStatusParam,
            @RequestParam(value = "tab", required = false) final String tabParam,
            final Authentication authentication) {
        final User currentUser = authResolver.resolveUser(authentication);
        final boolean isCommerce = currentUser.getRole() == User.Role.COMMERCE;
        final boolean isClient = currentUser.getRole() == User.Role.CLIENT;

        if (!isCommerce && !isClient) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        final String normalizedQuery = query == null ? "" : query.trim();
        final Reservation.Status statusFilter = parseStatusFilter(status);
        final Auction.Status auctionStatusFilter = parseAuctionStatusFilter(auctionStatusParam);

        if (isClient) {
            return clientReservations(page, normalizedQuery, statusFilter, auctionStatusFilter,
                    normalizeClientTab(tabParam), currentUser);
        }

        final List<Reservation> allReservations;
        final Commerce commerce = commerceService.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        allReservations = new ArrayList<>(reservationService.findByCommerceId(commerce.getUserId()));

        allReservations.sort(Comparator.comparing(Reservation::getReservationDate,
                Comparator.nullsLast(LocalDateTime::compareTo)).reversed());

        final List<Reservation> filteredReservations = new ArrayList<>();
        final Map<Long, Pack> allPacksByReservationId = new HashMap<>();
        final Map<Long, String> allClientNamesByReservationId = new HashMap<>();

        for (final Reservation reservation : allReservations) {
            Pack pack = null;
            String clientName = "-";

            if (reservation.getPackId() != null) {
                final Optional<Pack> packOpt = packService.findById(reservation.getPackId());
                if (packOpt.isPresent()) {
                    pack = packOpt.get();
                }
            }

            if (reservation.getCustomerId() != null) {
                clientName = clientService.findByUserId(reservation.getCustomerId())
                        .map(Client::getFullName)
                        .orElse("-");
            }

            final boolean matchesStatus = statusFilter == null || statusFilter.equals(reservation.getStatus());
            final boolean matchesQuery = normalizedQuery.isBlank()
                    || containsIgnoreCase(pack == null ? null : pack.getTitle(), normalizedQuery)
                    || containsIgnoreCase(pack == null ? null : pack.getDescription(), normalizedQuery)
                    || containsIgnoreCase(clientName, normalizedQuery);

            if (matchesStatus && matchesQuery) {
                filteredReservations.add(reservation);
                if (pack != null) {
                    allPacksByReservationId.put(reservation.getId(), pack);
                }
                allClientNamesByReservationId.put(reservation.getId(), clientName);
            }
        }

        final int totalPages = Math.max(1, (int) Math.ceil((double) filteredReservations.size() / PAGE_SIZE));
        final int safePage = Math.max(1, Math.min(page, totalPages));
        final int fromIdx = (safePage - 1) * PAGE_SIZE;
        final int toIdx = Math.min(fromIdx + PAGE_SIZE, filteredReservations.size());
        final List<Reservation> reservations = filteredReservations.subList(fromIdx, toIdx);

        final Map<Long, Pack> packsByReservationId = new HashMap<>();
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
            if (allClientNamesByReservationId.containsKey(reservation.getId())) {
                clientNamesByReservationId.put(reservation.getId(), allClientNamesByReservationId.get(reservation.getId()));
            }
        }

        final ModelAndView mav = new ModelAndView("reservations/reservationsView");
        mav.addObject("reservations", reservations);
        mav.addObject("packsByReservationId", packsByReservationId);
        mav.addObject("commerceNamesByReservationId", new HashMap<Long, String>());
        mav.addObject("clientNamesByReservationId", clientNamesByReservationId);
        mav.addObject("formattedReservationDatesById", formattedReservationDatesById);
        mav.addObject("searchQuery", normalizedQuery);
        mav.addObject("selectedStatus", statusFilter == null ? "" : statusFilter.name());
        mav.addObject("statusOptions", Reservation.Status.values());
        mav.addObject("hasAnyReservations", !allReservations.isEmpty());
        mav.addObject("hasActiveFilters", !normalizedQuery.isBlank() || statusFilter != null);
        mav.addObject("currentPage", safePage);
        mav.addObject("totalPages", totalPages);
        mav.addObject("paginationBaseUrl", buildCommercePaginationBaseUrl(normalizedQuery, statusFilter));
        mav.addObject("messagePrefix", "commerce.reservations");
        mav.addObject("clientReservationsTab", null);
        mav.addObject("clientAuctionsView", Boolean.FALSE);
        mav.addObject("selectedAuctionStatus", "");
        mav.addObject("auctionStatusOptions", Auction.Status.values());
        mav.addObject("clientParticipationAuctions", List.of());
        mav.addObject("auctionParticipationBadges", Map.of());
        mav.addObject("auctionVisualReservationIds",
                reservationIdsWithAuctionPack(reservations, auctionPackIdsForReservationPacks(reservations)));
        return mav;
    }

    private ModelAndView clientReservations(final int page, final String normalizedQuery,
            final Reservation.Status statusFilter, final Auction.Status auctionStatusFilter,
            final String clientTab, final User currentUser) {

        final List<Reservation> allReservations = new ArrayList<>(
                reservationService.findByCustomerId(currentUser.getId()));

        allReservations.sort(Comparator.comparing(Reservation::getReservationDate,
                Comparator.nullsLast(LocalDateTime::compareTo)).reversed());

        final Set<Long> auctionPackIds = new HashSet<>();
        final Set<Long> distinctPackIds = new HashSet<>();
        for (final Reservation reservation : allReservations) {
            if (reservation.getPackId() != null) {
                distinctPackIds.add(reservation.getPackId());
            }
        }
        for (final Long packId : distinctPackIds) {
            auctionService.findByPackId(packId).ifPresent(a -> auctionPackIds.add(packId));
        }

        final int itemsCount = allReservations.size();
        final int packsCount = (int) allReservations.stream()
                .filter(r -> r.getPackId() != null && !auctionPackIds.contains(r.getPackId()))
                .count();

        final List<Auction> allParticipatedAuctions = auctionService.findParticipatedAuctionsByClientId(currentUser.getId());
        final int auctionsCount = allParticipatedAuctions.size();

        final boolean hasAnyClientActivity = itemsCount > 0 || auctionsCount > 0;

        final boolean hasActiveFilters;
        if ("auctions".equals(clientTab)) {
            hasActiveFilters = !normalizedQuery.isBlank() || auctionStatusFilter != null;
        } else {
            hasActiveFilters = !normalizedQuery.isBlank() || statusFilter != null;
        }

        final List<Reservation> filteredReservations = new ArrayList<>();
        final Map<Long, Pack> allPacksByReservationId = new HashMap<>();
        final Map<Long, String> allCommerceNamesByReservationId = new HashMap<>();

        if (!"auctions".equals(clientTab)) {
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

                if ("packs".equals(clientTab)) {
                    if (pack == null || auctionPackIds.contains(pack.getId())) {
                        continue;
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
        }

        final ModelAndView mav = new ModelAndView("reservations/reservationsView");

        if ("auctions".equals(clientTab)) {
            final List<Auction> filteredAuctions = new ArrayList<>();
            for (final Auction auction : allParticipatedAuctions) {
                final Pack pack = auction.getPack();
                final String commerceName = pack == null ? "-"
                        : commerceService.findByUserId(pack.getCommerceId())
                                .map(Commerce::getCommercialName)
                                .filter(name -> name != null && !name.isBlank())
                                .orElse("-");

                final boolean matchesAuctionStatus = auctionStatusFilter == null
                        || auctionStatusFilter.equals(auction.getStatus());
                final boolean matchesQuery = normalizedQuery.isBlank()
                        || containsIgnoreCase(pack == null ? null : pack.getTitle(), normalizedQuery)
                        || containsIgnoreCase(pack == null ? null : pack.getDescription(), normalizedQuery)
                        || containsIgnoreCase(commerceName, normalizedQuery);

                if (matchesAuctionStatus && matchesQuery) {
                    filteredAuctions.add(auction);
                }
            }

            final int totalPages = Math.max(1, (int) Math.ceil((double) filteredAuctions.size() / PAGE_SIZE));
            final int safePage = Math.max(1, Math.min(page, totalPages));
            final int fromIdx = (safePage - 1) * PAGE_SIZE;
            final int toIdx = Math.min(fromIdx + PAGE_SIZE, filteredAuctions.size());
            final List<Auction> auctionsPage = filteredAuctions.subList(fromIdx, toIdx);

            final Map<Long, String> auctionCommerceNames = new HashMap<>();
            final Map<Long, String> auctionEndLabels = new HashMap<>();
            final Map<Long, Double> auctionMyMaxBid = new HashMap<>();
            final Map<Long, String> auctionParticipationBadges = new HashMap<>();

            for (final Auction auction : auctionsPage) {
                auctionEndLabels.put(auction.getId(), formatUtcDateTimeForDisplay(auction.getEndTime()));
                final Pack p = auction.getPack();
                if (p != null) {
                    final String cname = commerceService.findByUserId(p.getCommerceId())
                            .map(Commerce::getCommercialName)
                            .filter(name -> name != null && !name.isBlank())
                            .orElse("-");
                    auctionCommerceNames.put(auction.getId(), cname);
                } else {
                    auctionCommerceNames.put(auction.getId(), "-");
                }
                final double myMax = auctionService.getBidHistory(auction.getId()).stream()
                        .filter(b -> b.getClientId().equals(currentUser.getId()))
                        .mapToDouble(Bid::getAmount)
                        .max()
                        .orElse(0d);
                auctionMyMaxBid.put(auction.getId(), myMax);
                final String badge = clientParticipationAuctionBadge(auction,
                        auctionService.isClientLeading(auction.getId(), currentUser.getId()));
                if (badge != null) {
                    auctionParticipationBadges.put(auction.getId(), badge);
                }
            }

            mav.addObject("clientParticipationAuctions", auctionsPage);
            mav.addObject("auctionCommerceNames", auctionCommerceNames);
            mav.addObject("auctionEndLabels", auctionEndLabels);
            mav.addObject("auctionMyMaxBid", auctionMyMaxBid);
            mav.addObject("auctionParticipationBadges", auctionParticipationBadges);
            mav.addObject("reservations", List.of());
            mav.addObject("packsByReservationId", Map.of());
            mav.addObject("commerceNamesByReservationId", Map.of());
            mav.addObject("formattedReservationDatesById", Map.of());
            mav.addObject("currentPage", safePage);
            mav.addObject("totalPages", totalPages);
            mav.addObject("auctionVisualReservationIds", Set.of());
        } else {
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
                    commerceNamesByReservationId.put(reservation.getId(),
                            allCommerceNamesByReservationId.get(reservation.getId()));
                }
            }

            mav.addObject("reservations", reservations);
            mav.addObject("packsByReservationId", packsByReservationId);
            mav.addObject("commerceNamesByReservationId", commerceNamesByReservationId);
            mav.addObject("formattedReservationDatesById", formattedReservationDatesById);
            mav.addObject("clientParticipationAuctions", List.of());
            mav.addObject("auctionCommerceNames", Map.of());
            mav.addObject("auctionEndLabels", Map.of());
            mav.addObject("auctionMyMaxBid", Map.of());
            mav.addObject("auctionParticipationBadges", Map.of());
            mav.addObject("currentPage", safePage);
            mav.addObject("totalPages", totalPages);
            mav.addObject("auctionVisualReservationIds", reservationIdsWithAuctionPack(reservations, auctionPackIds));
        }

        mav.addObject("clientNamesByReservationId", Map.of());
        mav.addObject("searchQuery", normalizedQuery);
        mav.addObject("selectedStatus", statusFilter == null ? "" : statusFilter.name());
        mav.addObject("selectedAuctionStatus", auctionStatusFilter == null ? "" : auctionStatusFilter.name());
        mav.addObject("statusOptions", Reservation.Status.values());
        mav.addObject("auctionStatusOptions", Auction.Status.values());
        mav.addObject("hasAnyReservations", hasAnyClientActivity);
        mav.addObject("hasActiveFilters", hasActiveFilters);
        mav.addObject("paginationBaseUrl", buildClientPaginationBaseUrl(clientTab, normalizedQuery,
                statusFilter, auctionStatusFilter));
        mav.addObject("messagePrefix", "reservation.my");
        mav.addObject("clientReservationsTab", clientTab);
        mav.addObject("clientAuctionsView", "auctions".equals(clientTab));
        mav.addObject("itemsCount", itemsCount);
        mav.addObject("packsCount", packsCount);
        mav.addObject("auctionsCount", auctionsCount);
        mav.addObject("clientTabExtraQuery", normalizedQuery.isBlank() ? ""
                : "&q=" + URLEncoder.encode(normalizedQuery, StandardCharsets.UTF_8));

        return mav;
    }
}
