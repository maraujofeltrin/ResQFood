package ar.edu.itba.paw.webapp.controller.commerce;

import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.services.auction.AuctionService;
import ar.edu.itba.paw.services.commerce.CommerceService;
import ar.edu.itba.paw.services.metrics.CommerceMetricsService;
import ar.edu.itba.paw.services.pack.PackService;
import ar.edu.itba.paw.services.reservation.ReservationService;
import ar.edu.itba.paw.services.user.ClientService;
import ar.edu.itba.paw.webapp.auth.AuthUser;
import ar.edu.itba.paw.webapp.controller.utils.AuthenticatedUserResolver;
import ar.edu.itba.paw.webapp.controller.utils.CommerceMetricsFilterHelper;
import ar.edu.itba.paw.webapp.controller.utils.ReservationHistoryViewHelper;
import ar.edu.itba.paw.models.CommerceMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final ClientService clientService;
    private final AuthenticatedUserResolver authResolver;
    private final MessageSource messageSource;
    private final CommerceMetricsFilterHelper metricsFilterHelper;

    @Autowired
    public CommerceDashboardController(final CommerceService commerceService,
                                       final PackService packService,
                                       final AuctionService auctionService,
                                       final ReservationService reservationService,
                                       final ClientService clientService,
                                       final AuthenticatedUserResolver authResolver,
                                       final MessageSource messageSource,
                                       final CommerceMetricsService commerceMetricsService,
                                       final CommerceMetricsFilterHelper metricsFilterHelper) {
        this.commerceService = commerceService;
        this.packService = packService;
        this.auctionService = auctionService;
        this.reservationService = reservationService;
        this.clientService = clientService;
        this.authResolver = authResolver;
        this.messageSource = messageSource;
        this.commerceMetricsService = commerceMetricsService;
        this.metricsFilterHelper = metricsFilterHelper;
    }

    @GetMapping(value = "")
    public ModelAndView dashboard(@AuthenticationPrincipal final AuthUser principal) {
        final long id = authResolver.resolveUser(principal).getId();

        final Commerce commerce = commerceService.findByUserId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        final ModelAndView mav = new ModelAndView("commerce/dashboard");
        mav.addObject("commerce", commerce);

        // -- Stats --
        final int totalPublications = packService.countCommercePacks(id, null);
        final int soldToday = reservationService.countSoldToday(id);
        mav.addObject("totalPublications", totalPublications);
        mav.addObject("soldToday", soldToday);

        // -- Recent reservations (last 3) using the same history row format as pack detail --
        final List<Reservation> recentReservations = reservationService.filterReservations(
                id, null, null, null, 1, DASHBOARD_RECENT_LIMIT);
        final Locale locale = LocaleContextHolder.getLocale();
        final List<ReservationHistoryViewHelper.ReservationHistoryRow> recentHistoryItems =
                ReservationHistoryViewHelper.buildRows(recentReservations, clientService, messageSource, locale);
        mav.addObject("dashboardReservationHistoryItems", recentHistoryItems);

        return mav;
    }

    @GetMapping(value = "/products")
    public ModelAndView products(@AuthenticationPrincipal final AuthUser principal,
            @RequestParam(value = "page", defaultValue = "1") final int page,
            @RequestParam(value = "tab", defaultValue = "items") final String tab) {
        final long id = authResolver.resolveUser(principal).getId();

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

        final List<ar.edu.itba.paw.models.auction.Auction> commerceAuctions = auctionService.findByCommerceId(id);
        final Set<Long> auctionPackIds = commerceAuctions.stream()
                .map(a -> a.getPack().getId())
                .collect(Collectors.toSet());

        mav.addObject("commerce", commerce);
        mav.addObject("packs", displayedPacks);
        mav.addObject("currentPage", safePage);
        mav.addObject("totalPages", totalPages);
        mav.addObject("commerceId", id);
        mav.addObject("auctionPackIds", auctionPackIds);
        mav.addObject("currentTab", tab);
        mav.addObject("paginationBaseUrl", "/commerce/products?tab=" + tab);
        mav.addObject("itemsCount", itemsCount);
        mav.addObject("packsCount", packsCount);
        mav.addObject("auctionsCount", auctionsCount);
        return mav;
    }

    @GetMapping(value = "/metrics")
    public ModelAndView metrics(@AuthenticationPrincipal final AuthUser principal,
        @RequestParam(value = "from", required = false) final String fromStr,
        @RequestParam(value = "to", required = false) final String toStr,
        @RequestParam(value = "days", required = false) final Integer days) {
        final long userId = authResolver.resolveUser(principal).getId();
        final Commerce commerce = commerceService.findByUserId(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        final CommerceMetricsFilterHelper.MetricsFilterResolution resolution = metricsFilterHelper
            .resolve(fromStr, toStr, days);
        final CommerceMetrics metrics = commerceMetricsService.getCommerceMetrics(
            commerce.getUserId(), resolution.getFrom(), resolution.getTo());
        final ModelAndView mav = new ModelAndView("commerce/metrics");
        mav.addObject("commerce", commerce);
        mav.addObject("days", resolution.getDaysValue());
        mav.addObject("from", resolution.getFromValue());
        mav.addObject("to", resolution.getToValue());
        final String salesChartJson = buildSalesChartJson(metrics.getDailySales());
        mav.addObject("salesChartJson", salesChartJson);
        mav.addObject("totalRevenue", metrics.getTotalRevenue());
        mav.addObject("totalReservations", metrics.getTotalReservations());
        mav.addObject("bestSellingPackTitle", metrics.getBestSellingPackTitle());
        mav.addObject("acceptanceRatePercent", metrics.getAcceptanceRatePercent());
        return mav;
    }

    private static String buildSalesChartJson(final List<CommerceMetrics.DailySalesPoint> points) {
        final StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < points.size(); i++) {
            final CommerceMetrics.DailySalesPoint point = points.get(i);
            if (i > 0) {
                sb.append(',');
            }
            sb.append("{\"date\":\"")
                    .append(point.getDate())
                    .append("\",\"count\":")
                    .append(point.getCount())
                    .append('}');
        }
        sb.append(']');
        return sb.toString();
    }
}
