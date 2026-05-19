package ar.edu.itba.paw.webapp.controller.helpers;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.CommerceReview;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.commerce.CommercePublicOffers;
import ar.edu.itba.paw.services.commerce.CommerceReviewService;
import ar.edu.itba.paw.services.commerce.CommerceService;
import ar.edu.itba.paw.services.user.ClientService;
import ar.edu.itba.paw.services.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CommerceProfileModelBuilder {

    private static final int PAGE_SIZE = 12;
    private static final int REVIEW_PAGE_SIZE = 5;

    private final CommerceService commerceService;
    private final CommerceReviewService commerceReviewService;
    private final UserService userService;
    private final ClientService clientService;
    private final MessageSource messageSource;
    private final CommerceDetailAttributesHelper commerceDetailAttributesHelper;
    private final ZoneId businessZone;

    @Autowired
    public CommerceProfileModelBuilder(final CommerceService commerceService,
            final CommerceReviewService commerceReviewService, final UserService userService,
            final ClientService clientService, final MessageSource messageSource,
            final CommerceDetailAttributesHelper commerceDetailAttributesHelper, final ZoneId businessZone) {
        this.commerceService = commerceService;
        this.commerceReviewService = commerceReviewService;
        this.userService = userService;
        this.clientService = clientService;
        this.messageSource = messageSource;
        this.commerceDetailAttributesHelper = commerceDetailAttributesHelper;
        this.businessZone = businessZone;
    }

    public Optional<ModelAndView> buildProfileModel(final long commerceUserId, final int page) {
        final Optional<Commerce> commerceOpt = commerceService.findByUserId(commerceUserId);
        if (commerceOpt.isEmpty()) {
            return Optional.empty();
        }

        final Commerce commerce = commerceOpt.get();
        final Locale locale = LocaleContextHolder.getLocale();
        final String commercialName = commerce.getCommercialName() != null && !commerce.getCommercialName().isBlank()
                ? commerce.getCommercialName().trim()
                : messageSource.getMessage("commerce.profile.defaultName", null, locale);
        final String brand = messageSource.getMessage("app.brand", null, locale);
        final String pageTitle = messageSource.getMessage("commerce.profile.pageTitle",
                new Object[] { commercialName, brand }, locale);

        final CommercePublicOffers offers = commerceService.getPublicOffers(commerceUserId, page, PAGE_SIZE);
        final int totalPages = Math.max(1,
                (int) Math.ceil((double) offers.getDirectPacksTotal() / PAGE_SIZE));
        final int safePage = Math.max(1, Math.min(page, totalPages));

        final ModelAndView mav = new ModelAndView("commerce/commerceProfileView");
        mav.addObject("pageTitle", pageTitle);
        mav.addObject("commerceUserId", Long.valueOf(commerceUserId));
        mav.addObject("commerceCategory", commerce.getCategory());
        mav.addObject("profileImageId", userService.findById(commerceUserId).map(User::getProfileImageId).orElse(null));
        commerceDetailAttributesHelper.addCommerceDetailAttributes(mav, commerceOpt);

        final List<CommerceReview> reviews = commerceReviewService.findReviewsForCommerce(commerceUserId, 1,
                REVIEW_PAGE_SIZE);
        final Map<Long, Client> reviewClients = prefetchClients(reviews.stream()
                .map(CommerceReview::getClientUserId).distinct().collect(Collectors.toList()));
        mav.addObject("commerceReviewItems",
                CommerceReviewViewHelper.buildRows(reviews, reviewClients, businessZone, locale));
        mav.addObject("commerceReviewCount", commerceReviewService.countReviewsForCommerce(commerceUserId));
        mav.addObject("commerceReviewAverageRating",
                commerceReviewService.averageRatingForCommerce(commerceUserId).orElse(null));

        mav.addObject("directPacks", offers.getDirectPacks());
        mav.addObject("activeAuctions", offers.getActiveAuctions());
        mav.addObject("directPacksTotal", offers.getDirectPacksTotal());
        mav.addObject("activeAuctionsTotal", offers.getActiveAuctionsTotal());

        final Map<Long, String> commerceNames = new HashMap<>();
        for (final Pack pack : offers.getDirectPacks()) {
            commerceNames.put(pack.getId(), commercialName);
        }
        for (final Auction auction : offers.getActiveAuctions()) {
            if (auction.getPack() != null && auction.getPack().getId() != null) {
                commerceNames.put(auction.getPack().getId(), commercialName);
            }
        }
        mav.addObject("commerceNames", commerceNames);

        mav.addObject("currentPage", safePage);
        mav.addObject("totalPages", totalPages);
        mav.addObject("paginationBaseUrl", "/commerces/" + commerceUserId);

        return Optional.of(mav);
    }

    private Map<Long, Client> prefetchClients(final List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        final Map<Long, Client> map = new HashMap<>();
        for (final Long userId : userIds) {
            if (userId != null) {
                clientService.findByUserId(userId).ifPresent(c -> map.put(userId, c));
            }
        }
        return map;
    }
}
