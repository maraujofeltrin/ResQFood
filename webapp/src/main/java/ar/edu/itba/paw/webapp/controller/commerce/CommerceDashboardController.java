package ar.edu.itba.paw.webapp.controller.commerce;

import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.models.user.CommerceReview;
import ar.edu.itba.paw.services.auction.AuctionService;
import ar.edu.itba.paw.services.auction.CancelAuctionResult;
import ar.edu.itba.paw.services.commerce.CommerceReviewService;
import ar.edu.itba.paw.services.commerce.CommerceService;
import ar.edu.itba.paw.services.metrics.CommerceMetrics;
import ar.edu.itba.paw.services.metrics.CommerceMetricsService;
import ar.edu.itba.paw.services.pack.PackService;
import ar.edu.itba.paw.services.reservation.ReservationService;
import ar.edu.itba.paw.webapp.auth.AuthUser;
import ar.edu.itba.paw.webapp.controller.helpers.CommerceMetricsFilterHelper;
import ar.edu.itba.paw.webapp.controller.helpers.CommerceReviewViewHelper;
import ar.edu.itba.paw.webapp.controller.helpers.ReservationHistoryViewHelper;
import ar.edu.itba.paw.webapp.controller.helpers.ViewFormatUtils;
import ar.edu.itba.paw.webapp.form.MetricsFilterForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Controller
@RequestMapping("/commerce")
public class CommerceDashboardController {

    private static final int PAGE_SIZE = 6;
    private static final int DASHBOARD_RECENT_LIMIT = 3;

    private final CommerceService commerceService;
    private final PackService packService;
    private final AuctionService auctionService;
    private final ReservationService reservationService;
    private final CommerceMetricsService commerceMetricsService;
    private final CommerceReviewService commerceReviewService;
    private final MessageSource messageSource;
    private final CommerceMetricsFilterHelper metricsFilterHelper;
    private final ZoneId businessZone;

    @Autowired
    public CommerceDashboardController(final CommerceService commerceService,
                                       final PackService packService,
                                       final AuctionService auctionService,
                                       final ReservationService reservationService,
                                       final MessageSource messageSource,
                                       final CommerceMetricsService commerceMetricsService,
                                       final CommerceMetricsFilterHelper metricsFilterHelper,
                                       final CommerceReviewService commerceReviewService,
                                       final ZoneId businessZone) {
        this.commerceService = commerceService;
        this.packService = packService;
        this.auctionService = auctionService;
        this.reservationService = reservationService;
        this.messageSource = messageSource;
        this.commerceMetricsService = commerceMetricsService;
        this.metricsFilterHelper = metricsFilterHelper;
        this.commerceReviewService = commerceReviewService;
        this.businessZone = businessZone;
    }

    @GetMapping(value = "")
    public ModelAndView dashboard(@AuthenticationPrincipal final AuthUser principal) {
        final long id = principal.getId();

        final Commerce commerce = commerceService.findByUserId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        final ModelAndView mav = new ModelAndView("commerce/dashboard");
        mav.addObject("commerce", commerce);

        // -- Stats --
        final int totalPublications = packService.countCommercePacks(id, null);
        final int soldToday = commerceMetricsService.countSoldToday(id);
        mav.addObject("totalPublications", totalPublications);
        mav.addObject("soldToday", soldToday);

        // -- Recent reservations (last 3) using the same history row format as pack detail --
        final List<Reservation> recentReservations = reservationService.filterReservations(
                id, null, null, null, false, 1, DASHBOARD_RECENT_LIMIT);
        final Locale locale = LocaleContextHolder.getLocale();
        final List<ReservationHistoryViewHelper.ReservationHistoryRow> recentHistoryItems =
                ReservationHistoryViewHelper.buildRows(recentReservations, messageSource, locale);
        mav.addObject("dashboardReservationHistoryItems", recentHistoryItems);

        // -- Recent reviews (last 3) --
        final List<CommerceReview> recentReviews = commerceReviewService.findReviewsForCommerce(id, 1, DASHBOARD_RECENT_LIMIT);
        final List<CommerceReviewViewHelper.CommerceReviewRow> recentReviewItems =
                CommerceReviewViewHelper.buildRows(recentReviews, businessZone, locale);
        mav.addObject("dashboardRecentReviews", recentReviewItems);
        mav.addObject("commerceReviewCount", commerceReviewService.countReviewsForCommerce(id));
        mav.addObject("commerceReviewAverageRating",
                commerceReviewService.averageRatingForCommerce(id).orElse(null));

        return mav;
    }

    @GetMapping(value = "/reviews")
    public ModelAndView reviews(@AuthenticationPrincipal final AuthUser principal,
                                @RequestParam(value = "page", defaultValue = "1") final int page) {
        final long id = principal.getId();
        final Commerce commerce = commerceService.findByUserId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        final Locale locale = LocaleContextHolder.getLocale();

        final int totalReviews = commerceReviewService.countReviewsForCommerce(id);
        final int totalPages = Math.max(1, (int) Math.ceil((double) totalReviews / PAGE_SIZE));
        final int safePage = Math.max(1, Math.min(page, totalPages));

        final List<CommerceReview> reviews = commerceReviewService.findReviewsForCommerce(id, safePage, PAGE_SIZE);
        final List<CommerceReviewViewHelper.CommerceReviewRow> reviewItems =
                CommerceReviewViewHelper.buildRows(reviews, businessZone, locale);

        final ModelAndView mav = new ModelAndView("commerce/reviews");
        mav.addObject("commerce", commerce);
        mav.addObject("reviewItems", reviewItems);
        mav.addObject("reviewCount", totalReviews);
        mav.addObject("averageRating", commerceReviewService.averageRatingForCommerce(id).orElse(null));
        mav.addObject("currentPage", safePage);
        mav.addObject("totalPages", totalPages);
        mav.addObject("paginationBaseUrl", "/commerce/reviews");
        return mav;
    }

    @GetMapping(value = "/products")
    public ModelAndView products(@AuthenticationPrincipal final AuthUser principal,
            @RequestParam(value = "page", defaultValue = "1") final int page,
            @RequestParam(value = "tab", defaultValue = "items") final String tab) {
        final long id = principal.getId();

        final Optional<Commerce> commerceOpt = commerceService.findByUserId(id);
        if (!commerceOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final Commerce commerce = commerceOpt.get();
        final ModelAndView mav = new ModelAndView("commerce/products");

        Boolean hasAuction = null;
        if ("auctions".equalsIgnoreCase(tab)) {
            hasAuction = true;
        } else if ("packs".equalsIgnoreCase(tab)) {
            hasAuction = false;
        }

        final int totalItems = packService.countCommercePacks(id, hasAuction);
        final int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));
        final int safePage = Math.max(1, Math.min(page, totalPages));

        final List<Pack> displayedPacks = packService.filterCommercePacks(id, hasAuction, safePage, PAGE_SIZE);

        final int itemsCount = packService.countCommercePacks(id, null);
        final int auctionsCount = packService.countCommercePacks(id, true);
        final int packsCount = packService.countCommercePacks(id, false);

        mav.addObject("commerce", commerce);
        mav.addObject("packs", displayedPacks);
        mav.addObject("currentPage", safePage);
        mav.addObject("totalPages", totalPages);
        mav.addObject("commerceId", id);
        mav.addObject("currentTab", tab);
        mav.addObject("paginationBaseUrl", "/commerce/products?tab=" + tab);
        mav.addObject("itemsCount", itemsCount);
        mav.addObject("packsCount", packsCount);
        mav.addObject("auctionsCount", auctionsCount);
        return mav;
    }

    @GetMapping(value = "/metrics")
    public ModelAndView metrics(@AuthenticationPrincipal final AuthUser principal,
        @Valid @ModelAttribute("metricsFilterForm") final MetricsFilterForm filter,
        final BindingResult bindingResult,
        @RequestParam(value = "days", required = false) final Integer days) {
        final long userId = principal.getId();
        final Commerce commerce = commerceService.findByUserId(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (bindingResult.hasErrors()) {
            final ModelAndView mav = new ModelAndView("commerce/metrics");
            mav.addObject("commerce", commerce);
            mav.addObject("metricsFilterForm", filter);
            mav.addObject("days", days);
            mav.addObject("from", filter.getFromDate());
            mav.addObject("to", filter.getToDate());
            mav.addObject("salesChartJson", "[]");
            mav.addObject("totalRevenue", 0);
            mav.addObject("totalReservations", 0);
            mav.addObject("bestSellingPackTitle", null);
            mav.addObject("acceptanceRatePercent", 0);
            mav.addObject("canceledReservations", 0);
            mav.addObject("averageTicket", 0);
            mav.addObject("uniqueClients", 0);
            mav.addObject("topPacks", new ArrayList<>());
            mav.addObject("topClients", new ArrayList<>());
            mav.addObject("clientRetention", null);
            return mav;
        }
        final CommerceMetricsFilterHelper.MetricsFilterResolution resolution = metricsFilterHelper
            .resolve(filter.getFromDate(), filter.getToDate(), days);
        final CommerceMetrics metrics = commerceMetricsService.getCommerceMetrics(
            userId, resolution.getFrom(), resolution.getTo());
        final ModelAndView mav = new ModelAndView("commerce/metrics");
        mav.addObject("commerce", commerce);
        mav.addObject("metricsFilterForm", filter);
        mav.addObject("days", resolution.getDaysValue());
        mav.addObject("from", resolution.getFromValue());
        mav.addObject("to", resolution.getToValue());
        final String salesChartJson = ViewFormatUtils.formatSalesChartJson(metrics.getDailySales());
        mav.addObject("salesChartJson", salesChartJson);
        mav.addObject("totalRevenue", metrics.getTotalRevenue());
        mav.addObject("totalReservations", metrics.getTotalReservations());
        mav.addObject("bestSellingPackTitle", metrics.getBestSellingPackTitle());
        mav.addObject("acceptanceRatePercent", metrics.getAcceptanceRatePercent());
        mav.addObject("canceledReservations", metrics.getCanceledReservations());
        mav.addObject("averageTicket", metrics.getAverageTicket());
        mav.addObject("uniqueClients", metrics.getUniqueClients());
        mav.addObject("topPacks", metrics.getTopPacks());
        mav.addObject("topClients", metrics.getTopClients());
        mav.addObject("clientRetention", metrics.getClientRetention());
        return mav;
    }

    @PostMapping(value = "/auctions/{auctionId}/cancel")
    @PreAuthorize("@own.canWriteAuction(#auctionId, authentication.principal.id)")
    public ModelAndView cancelAuction(@PathVariable("auctionId") final long auctionId) {
        final CancelAuctionResult result = auctionService.cancelAuction(auctionId);

        switch (result.getOutcome()) {
            case SUCCESS:
                return new ModelAndView("redirect:/commerce/products?cancelled=true");
            case NOT_FOUND:
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            case HAS_BIDS:
                return new ModelAndView("redirect:/commerce/products?cancelFailed=true");
            case NOT_ACTIVE:
                throw new ResponseStatusException(HttpStatus.CONFLICT);
            default:
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
