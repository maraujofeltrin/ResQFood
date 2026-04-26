package ar.edu.itba.paw.webapp.controller.commerce;

import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.services.auction.AuctionService;
import ar.edu.itba.paw.services.commerce.CommerceService;
import ar.edu.itba.paw.services.pack.PackService;
import ar.edu.itba.paw.webapp.auth.AuthUser;
import ar.edu.itba.paw.webapp.controller.utils.AuthenticatedUserResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/commerce")
public class CommerceDashboardController {

    private static final int PAGE_SIZE = 6;

    private final CommerceService commerceService;
    private final PackService packService;
    private final AuctionService auctionService;
    private final AuthenticatedUserResolver authResolver;

    @Autowired
    public CommerceDashboardController(final CommerceService commerceService,
                                       final PackService packService,
                                       final AuctionService auctionService,
                                       final AuthenticatedUserResolver authResolver) {
        this.commerceService = commerceService;
        this.packService = packService;
        this.auctionService = auctionService;
        this.authResolver = authResolver;
    }

    @GetMapping(value = "")
    public ModelAndView dashboard(@AuthenticationPrincipal final AuthUser principal,
            @RequestParam(value = "page", defaultValue = "1") final int page,
            @RequestParam(value = "tab", defaultValue = "items") final String tab) {
        final long id = authResolver.resolveUser(principal).getId();

        final java.util.Optional<Commerce> commerceOpt = commerceService.findByUserId(id);
        if (!commerceOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final Commerce commerce = commerceOpt.get();
        final ModelAndView mav = new ModelAndView("commerce/dashboard");

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
        mav.addObject("paginationBaseUrl", "/commerce?tab=" + tab);
        mav.addObject("itemsCount", itemsCount);
        mav.addObject("packsCount", packsCount);
        mav.addObject("auctionsCount", auctionsCount);
        return mav;
    }
}
