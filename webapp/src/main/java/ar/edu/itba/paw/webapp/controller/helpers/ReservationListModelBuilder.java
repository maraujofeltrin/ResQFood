package ar.edu.itba.paw.webapp.controller.helpers;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import ar.edu.itba.paw.webapp.form.ReservationListFilterForm;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservationListModelBuilder.class);

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

    // -- Public API -----------------------------------------------------------

    public ModelAndView buildCommerceView(final ReservationListFilterForm form, final User currentUser) {

        final Commerce commerce = commerceService.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        final Long commerceId = commerce.getUserId();

        final String normalizedQuery = form.getQ();
        final Reservation.Status statusFilter = form.getStatus();
        final int page = form.getPage();

        final int totalItems = reservationService.countFilteredReservations(commerceId, null, normalizedQuery, statusFilter, false);
        final int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));
        final int safePage = Math.max(1, Math.min(page, totalPages));

        final List<Reservation> reservations = reservationService.filterReservations(
                commerceId, null, normalizedQuery, statusFilter, false, safePage, PAGE_SIZE);
        final boolean hasAnyReservations = reservationService.countFilteredReservations(
                commerceId, null, null, null, false) > 0;

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
        mav.addObject("clientReservationsTab", null);
        mav.addObject("clientAuctionsView", Boolean.FALSE);
        mav.addObject("selectedAuctionStatus", "");
        mav.addObject("auctionStatusOptions", Auction.Status.values());
        mav.addObject("clientParticipationAuctions", List.of());
        mav.addObject("auctionParticipationBadges", Map.of());
        mav.addObject("auctionVisualReservationIds",
                resolveAuctionReservationIds(reservations));
        return mav;
    }

    public ModelAndView buildClientView(final ReservationListFilterForm form, final User currentUser) {

        final String normalizedQuery = form.getQ();
        final Reservation.Status statusFilter = form.getStatus();
        final Auction.Status auctionStatusFilter = form.getAuctionStatus();
        final String clientTab = form.getTab();
        final int page = form.getPage();
        final String safeQuery = normalizedQuery == null ? "" : normalizedQuery;

        final int itemsCount = reservationService.countFilteredReservations(null, currentUser.getId(), null, null, false);
        final int packsCount = reservationService.countFilteredReservations(null, currentUser.getId(), null, null, true);

        final int auctionsCount = auctionService.countParticipatedAuctions(currentUser.getId(), null, null);

        final boolean hasAnyClientActivity = itemsCount > 0 || auctionsCount > 0;

        final boolean hasActiveFilters;
        if ("auctions".equals(clientTab)) {
            hasActiveFilters = !safeQuery.isBlank() || auctionStatusFilter != null;
        } else {
            hasActiveFilters = !safeQuery.isBlank() || statusFilter != null;
        }

        final ModelAndView mav = new ModelAndView(VIEW_NAME);

        if ("auctions".equals(clientTab)) {
            buildClientAuctionsTab(mav, auctionStatusFilter, safeQuery,
                    page, currentUser);
        } else {
            final boolean excludeAuctions = "packs".equals(clientTab);
            buildClientReservationsTab(mav, currentUser, statusFilter, safeQuery,
                    excludeAuctions, page);
        }

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

    // -- Client tab builders --------------------------------------------------

    private void buildClientAuctionsTab(final ModelAndView mav,
            final Auction.Status auctionStatusFilter, final String safeQuery,
            final int page, final User currentUser) {

        final String queryParam = safeQuery.isBlank() ? null : safeQuery;

        final int totalItems = auctionService.countParticipatedAuctions(
                currentUser.getId(), auctionStatusFilter, queryParam);
        final int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));
        final int safePage = Math.max(1, Math.min(page, totalPages));

        final List<Auction> auctionsPage = auctionService.filterParticipatedAuctions(
                currentUser.getId(), auctionStatusFilter, queryParam, safePage, PAGE_SIZE);

        final Map<Long, String> auctionCommerceNames = new HashMap<>();
        final Map<Long, String> auctionEndLabels = new HashMap<>();
        final Map<Long, Double> auctionMyMaxBid = new HashMap<>();
        final Map<Long, String> auctionParticipationBadges = new HashMap<>();

        for (final Auction auction : auctionsPage) {
            auctionEndLabels.put(auction.getId(), formatUtcDateTimeForDisplay(auction.getEndTime()));
            final Pack p = auction.getPack();
            auctionCommerceNames.put(auction.getId(), resolveCommerceName(p));

            final double myMax = auctionService.getBidHistory(auction.getId()).stream()
                    .filter(b -> b.getClient().getUserId().equals(currentUser.getId()))
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

    private void buildClientReservationsTab(final ModelAndView mav, final User currentUser,
            final Reservation.Status statusFilter, final String safeQuery,
            final boolean excludeAuctionPacks, final int page) {

        final Long customerId = currentUser.getId();
        final String queryParam = safeQuery.isBlank() ? null : safeQuery;

        final int totalItems = reservationService.countFilteredReservations(
                null, customerId, queryParam, statusFilter, excludeAuctionPacks);
        final int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));
        final int safePage = Math.max(1, Math.min(page, totalPages));

        final List<Reservation> reservations = reservationService.filterReservations(
                null, customerId, queryParam, statusFilter, excludeAuctionPacks, safePage, PAGE_SIZE);

        final Map<Long, Pack> packsByReservationId = new HashMap<>();
        final Map<Long, String> commerceNamesByReservationId = new HashMap<>();
        final Map<Long, String> formattedDates = new HashMap<>();

        for (final Reservation reservation : reservations) {
            populateFormattedDate(formattedDates, reservation);
            if (reservation.getPackId() != null) {
                final Optional<Pack> packOpt = packService.findById(reservation.getPackId());
                if (packOpt.isPresent()) {
                    final Pack pack = packOpt.get();
                    packsByReservationId.put(reservation.getId(), pack);
                    commerceNamesByReservationId.put(reservation.getId(), resolveCommerceName(pack));
                }
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
        mav.addObject("auctionVisualReservationIds", resolveAuctionReservationIds(reservations));
    }

    // -- Helpers --------------------------------------------------------------

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

    private Set<Long> resolveAuctionReservationIds(final List<Reservation> reservations) {
        final Set<Long> distinctPackIds = new HashSet<>();
        for (final Reservation r : reservations) {
            if (r.getPackId() != null) {
                distinctPackIds.add(r.getPackId());
            }
        }
        final Set<Long> auctionPackIds = new HashSet<>();
        for (final Long packId : distinctPackIds) {
            auctionService.findByPackId(packId).ifPresent(a -> auctionPackIds.add(packId));
        }
        final Set<Long> ids = new HashSet<>();
        for (final Reservation r : reservations) {
            if (r.getPackId() != null && auctionPackIds.contains(r.getPackId())) {
                ids.add(r.getId());
            }
        }
        return ids;
    }




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

    // -- Parsing & URL helpers ------------------------------------------------

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
