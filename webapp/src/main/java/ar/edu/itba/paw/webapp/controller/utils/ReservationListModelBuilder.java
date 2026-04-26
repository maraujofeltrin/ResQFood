package ar.edu.itba.paw.webapp.controller.utils;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Builds the {@link ModelAndView} for the reservation list view ({@code reservationsView.jsp}).
 * Handles both the commerce and client perspectives, including tab routing,
 * filtering, pagination, and view-model map assembly.
 */
@Component
public class ReservationListModelBuilder {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
    private static final int PAGE_SIZE = 6;
    private static final String VIEW_NAME = "reservations/reservationsView";

    private final ReservationService reservationService;
    private final PackService packService;
    private final CommerceService commerceService;
    private final ClientService clientService;
    private final AuctionService auctionService;
    private final ZoneId displayZone;

    @Autowired
    public ReservationListModelBuilder(final ReservationService reservationService,
            final PackService packService,
            final CommerceService commerceService,
            final ClientService clientService,
            final AuctionService auctionService,
            final ZoneId businessZone) {
        this.reservationService = reservationService;
        this.packService = packService;
        this.commerceService = commerceService;
        this.clientService = clientService;
        this.auctionService = auctionService;
        this.displayZone = businessZone;
    }

    // ── Public API ──────────────────────────────────────────────────────────────

    /**
     * Builds the reservation list view for a commerce user, using DB-level pagination.
     */
    public ModelAndView buildCommerceView(final int page, final String query, final String status,
            final User currentUser) {

        final Commerce commerce = commerceService.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        final Long commerceId = commerce.getUserId();

        final String normalizedQuery = normalizeQuery(query);
        final Reservation.Status statusFilter = parseReservationStatus(status);

        final int totalItems = reservationService.countFilteredReservations(commerceId, null, normalizedQuery, statusFilter);
        final int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));
        final int safePage = Math.max(1, Math.min(page, totalPages));

        final List<Reservation> reservations = reservationService.filterReservations(
                commerceId, null, normalizedQuery, statusFilter, safePage, PAGE_SIZE);
        final boolean hasAnyReservations = reservationService.countFilteredReservations(
                commerceId, null, null, null) > 0;

        final Map<Long, Pack> packsByReservationId = new HashMap<>();
        final Map<Long, String> clientNamesByReservationId = new HashMap<>();
        final Map<Long, String> formattedDates = new HashMap<>();

        for (final Reservation reservation : reservations) {
            populateFormattedDate(formattedDates, reservation);
            if (reservation.getPackId() != null) {
                packService.findById(reservation.getPackId()).ifPresent(
                        pack -> packsByReservationId.put(reservation.getId(), pack));
            }
            if (reservation.getCustomerId() != null) {
                final String clientName = clientService.findByUserId(reservation.getCustomerId())
                        .map(Client::getFullName)
                        .orElse("-");
                clientNamesByReservationId.put(reservation.getId(), clientName);
            }
        }

        final ModelAndView mav = new ModelAndView(VIEW_NAME);
        mav.addObject("reservations", reservations);
        mav.addObject("packsByReservationId", packsByReservationId);
        mav.addObject("commerceNamesByReservationId", new HashMap<Long, String>());
        mav.addObject("clientNamesByReservationId", clientNamesByReservationId);
        mav.addObject("formattedReservationDatesById", formattedDates);
        mav.addObject("searchQuery", normalizedQuery == null ? "" : normalizedQuery);
        mav.addObject("selectedStatus", statusFilter == null ? "" : statusFilter.name());
        mav.addObject("statusOptions", Reservation.Status.values());
        mav.addObject("hasAnyReservations", hasAnyReservations);
        mav.addObject("hasActiveFilters", normalizedQuery != null || statusFilter != null);
        mav.addObject("currentPage", safePage);
        mav.addObject("totalPages", totalPages);
        mav.addObject("paginationBaseUrl", buildCommercePaginationBaseUrl(normalizedQuery, statusFilter));
        mav.addObject("messagePrefix", "commerce.reservations");
        // Client-only attributes (empty defaults so JSP doesn't break)
        mav.addObject("clientReservationsTab", null);
        mav.addObject("clientAuctionsView", Boolean.FALSE);
        mav.addObject("selectedAuctionStatus", "");
        mav.addObject("auctionStatusOptions", Auction.Status.values());
        mav.addObject("clientParticipationAuctions", List.of());
        mav.addObject("auctionParticipationBadges", Map.of());
        mav.addObject("auctionVisualReservationIds",
                resolveAuctionReservationIds(reservations, resolveAuctionPackIds(reservations)));
        return mav;
    }

    /**
     * Builds the reservation list view for a client user, with tabs (items/packs/auctions)
     * and in-memory filtering for auction participation.
     */
    public ModelAndView buildClientView(final int page, final String query, final String status,
            final String auctionStatusParam, final String tabParam, final User currentUser) {

        final String normalizedQuery = normalizeQuery(query);
        final Reservation.Status statusFilter = parseReservationStatus(status);
        final Auction.Status auctionStatusFilter = parseAuctionStatus(auctionStatusParam);
        final String clientTab = normalizeClientTab(tabParam);
        final String safeQuery = normalizedQuery == null ? "" : normalizedQuery;

        final List<Reservation> allReservations = new ArrayList<>(
                reservationService.findByCustomerId(currentUser.getId()));

        allReservations.sort(Comparator.comparing(Reservation::getReservationDate,
                Comparator.nullsLast(LocalDateTime::compareTo)).reversed());

        final Set<Long> auctionPackIds = resolveAuctionPackIds(allReservations);

        final int itemsCount = allReservations.size();
        final int packsCount = (int) allReservations.stream()
                .filter(r -> r.getPackId() != null && !auctionPackIds.contains(r.getPackId()))
                .count();

        final List<Auction> allParticipatedAuctions = auctionService.findParticipatedAuctionsByClientId(currentUser.getId());
        final int auctionsCount = allParticipatedAuctions.size();

        final boolean hasAnyClientActivity = itemsCount > 0 || auctionsCount > 0;

        final boolean hasActiveFilters;
        if ("auctions".equals(clientTab)) {
            hasActiveFilters = !safeQuery.isBlank() || auctionStatusFilter != null;
        } else {
            hasActiveFilters = !safeQuery.isBlank() || statusFilter != null;
        }

        final ModelAndView mav = new ModelAndView(VIEW_NAME);

        if ("auctions".equals(clientTab)) {
            buildClientAuctionsTab(mav, allParticipatedAuctions, auctionStatusFilter, safeQuery,
                    page, currentUser);
        } else {
            buildClientReservationsTab(mav, allReservations, auctionPackIds, statusFilter, safeQuery,
                    clientTab, page);
        }

        // Shared client attributes
        mav.addObject("clientNamesByReservationId", Map.of());
        mav.addObject("searchQuery", safeQuery);
        mav.addObject("selectedStatus", statusFilter == null ? "" : statusFilter.name());
        mav.addObject("selectedAuctionStatus", auctionStatusFilter == null ? "" : auctionStatusFilter.name());
        mav.addObject("statusOptions", Reservation.Status.values());
        mav.addObject("auctionStatusOptions", Auction.Status.values());
        mav.addObject("hasAnyReservations", hasAnyClientActivity);
        mav.addObject("hasActiveFilters", hasActiveFilters);
        mav.addObject("paginationBaseUrl", buildClientPaginationBaseUrl(clientTab, safeQuery,
                statusFilter, auctionStatusFilter));
        mav.addObject("messagePrefix", "reservation.my");
        mav.addObject("clientReservationsTab", clientTab);
        mav.addObject("clientAuctionsView", "auctions".equals(clientTab));
        mav.addObject("itemsCount", itemsCount);
        mav.addObject("packsCount", packsCount);
        mav.addObject("auctionsCount", auctionsCount);
        mav.addObject("clientTabExtraQuery", safeQuery.isBlank() ? ""
                : "&q=" + URLEncoder.encode(safeQuery, StandardCharsets.UTF_8));

        return mav;
    }

    // ── Client tab builders ─────────────────────────────────────────────────────

    private void buildClientAuctionsTab(final ModelAndView mav, final List<Auction> allParticipatedAuctions,
            final Auction.Status auctionStatusFilter, final String safeQuery,
            final int page, final User currentUser) {

        final List<Auction> filteredAuctions = new ArrayList<>();
        for (final Auction auction : allParticipatedAuctions) {
            final Pack pack = auction.getPack();
            final String commerceName = resolveCommerceName(pack);

            final boolean matchesAuctionStatus = auctionStatusFilter == null
                    || auctionStatusFilter.equals(auction.getStatus());
            final boolean matchesQuery = safeQuery.isBlank()
                    || containsIgnoreCase(pack == null ? null : pack.getTitle(), safeQuery)
                    || containsIgnoreCase(pack == null ? null : pack.getDescription(), safeQuery)
                    || containsIgnoreCase(commerceName, safeQuery);

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
            auctionCommerceNames.put(auction.getId(), resolveCommerceName(p));

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
    }

    private void buildClientReservationsTab(final ModelAndView mav, final List<Reservation> allReservations,
            final Set<Long> auctionPackIds, final Reservation.Status statusFilter,
            final String safeQuery, final String clientTab, final int page) {

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
                    commerceName = resolveCommerceName(pack);
                }
            }

            if ("packs".equals(clientTab)) {
                if (pack == null || auctionPackIds.contains(pack.getId())) {
                    continue;
                }
            }

            final boolean matchesStatus = statusFilter == null || statusFilter.equals(reservation.getStatus());
            final boolean matchesQuery = safeQuery.isBlank()
                    || containsIgnoreCase(pack == null ? null : pack.getTitle(), safeQuery)
                    || containsIgnoreCase(pack == null ? null : pack.getDescription(), safeQuery)
                    || containsIgnoreCase(commerceName, safeQuery);

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
        final Map<Long, String> formattedDates = new HashMap<>();

        for (final Reservation reservation : reservations) {
            populateFormattedDate(formattedDates, reservation);
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
        mav.addObject("formattedReservationDatesById", formattedDates);
        mav.addObject("clientParticipationAuctions", List.of());
        mav.addObject("auctionCommerceNames", Map.of());
        mav.addObject("auctionEndLabels", Map.of());
        mav.addObject("auctionMyMaxBid", Map.of());
        mav.addObject("auctionParticipationBadges", Map.of());
        mav.addObject("currentPage", safePage);
        mav.addObject("totalPages", totalPages);
        mav.addObject("auctionVisualReservationIds", resolveAuctionReservationIds(reservations, auctionPackIds));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private String formatUtcDateTimeForDisplay(final LocalDateTime utc) {
        if (utc == null) {
            return null;
        }
        return ZonedDateTime.of(utc, ZoneOffset.UTC).withZoneSameInstant(displayZone).format(DATE_FORMATTER);
    }

    private void populateFormattedDate(final Map<Long, String> map, final Reservation reservation) {
        if (reservation.getReservationDate() != null) {
            map.put(reservation.getId(), formatUtcDateTimeForDisplay(reservation.getReservationDate()));
        }
    }

    private String resolveCommerceName(final Pack pack) {
        if (pack == null) {
            return "-";
        }
        return commerceService.findByUserId(pack.getCommerceId())
                .map(Commerce::getCommercialName)
                .filter(name -> name != null && !name.isBlank())
                .orElse("-");
    }

    private Set<Long> resolveAuctionPackIds(final List<Reservation> reservations) {
        final Set<Long> distinctPackIds = new HashSet<>();
        for (final Reservation r : reservations) {
            if (r.getPackId() != null) {
                distinctPackIds.add(r.getPackId());
            }
        }
        final Set<Long> auctionPacks = new HashSet<>();
        for (final Long packId : distinctPackIds) {
            auctionService.findByPackId(packId).ifPresent(a -> auctionPacks.add(packId));
        }
        return auctionPacks;
    }

    private static Set<Long> resolveAuctionReservationIds(final List<Reservation> reservations,
            final Set<Long> auctionPackIds) {
        final Set<Long> ids = new HashSet<>();
        for (final Reservation r : reservations) {
            if (r.getPackId() != null && auctionPackIds.contains(r.getPackId())) {
                ids.add(r.getId());
            }
        }
        return ids;
    }

    private static boolean containsIgnoreCase(final String value, final String needle) {
        return value != null && needle != null
                && value.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }

    /**
     * @return badge code for the participation card, or {@code null} when no badge should be shown
     *         (cancelled, or finished and the client won).
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

    // ── Parsing & URL helpers ───────────────────────────────────────────────────

    private static String normalizeQuery(final String query) {
        return (query == null || query.isBlank()) ? null : query.trim();
    }

    private static Reservation.Status parseReservationStatus(final String statusValue) {
        if (statusValue == null || statusValue.isBlank()) {
            return null;
        }
        try {
            return Reservation.Status.valueOf(statusValue.trim().toUpperCase(Locale.ROOT));
        } catch (final IllegalArgumentException ex) {
            return null;
        }
    }

    private static Auction.Status parseAuctionStatus(final String statusValue) {
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

    private static String buildCommercePaginationBaseUrl(final String query, final Reservation.Status statusFilter) {
        final StringBuilder baseUrl = new StringBuilder("/reservations");
        boolean firstParam = true;
        if (query != null && !query.isBlank()) {
            baseUrl.append(firstParam ? "?" : "&").append("q=")
                    .append(URLEncoder.encode(query.trim(), StandardCharsets.UTF_8));
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
}
