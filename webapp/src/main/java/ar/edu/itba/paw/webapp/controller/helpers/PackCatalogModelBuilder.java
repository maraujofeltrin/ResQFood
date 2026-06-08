package ar.edu.itba.paw.webapp.controller.helpers;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.auction.AuctionSortOption;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackSortOption;
import ar.edu.itba.paw.models.pack.PackTag;
import ar.edu.itba.paw.services.auction.AuctionService;
import ar.edu.itba.paw.services.commerce.CommerceService;
import ar.edu.itba.paw.services.commerce.CommerceReviewService;
import ar.edu.itba.paw.services.pack.PackFavoriteService;
import ar.edu.itba.paw.services.pack.PackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import ar.edu.itba.paw.webapp.form.CatalogFilterForm;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Builds the pack/auction catalog page model (search, filters, pagination).
 */
@Component
public class PackCatalogModelBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackCatalogModelBuilder.class);

    private static final int PAGE_SIZE = 6;
    private static final int COMMERCES_PAGE_SIZE = 8;
    private static final int AUCTION_CAROUSEL_SIZE = 6;
    private static final int FAVORITES_CAROUSEL_SIZE = 6;
    private static final int COMMERCES_CAROUSEL_SIZE = 6;
    private static final String TYPE_PACKS = "packs";
    private static final String TYPE_AUCTIONS = "auctions";
    private static final String TYPE_FAVORITES = "favorites";
    private static final String TYPE_COMMERCES = "commerces";
    private static final Set<String> ALLOWED_TIME_RANGES = Set.of("morning", "afternoon", "evening");

    private enum CatalogMode {
        ALL,
        PACKS,
        AUCTIONS,
        COMMERCES
    }

    private final PackService packService;
    private final CommerceService commerceService;
    private final AuctionService auctionService;
    private final AuthenticatedUserResolver authResolver;
    private final CommerceReviewService commerceReviewService;

    @Autowired
    public PackCatalogModelBuilder(final PackService packService, final CommerceService commerceService,
            final AuctionService auctionService,
            final AuthenticatedUserResolver authResolver,
            final CommerceReviewService commerceReviewService) {
        this.packService = packService;
        this.commerceService = commerceService;
        this.auctionService = auctionService;
        this.authResolver = authResolver;
        this.commerceReviewService = commerceReviewService;
    }

    public ModelAndView buildPackCatalog(final CatalogFilterForm form) {

        final String query = form.getQ();
        final int page = form.getPage();

        final ModelAndView mav = new ModelAndView("packs/packCatalogView");
        final PackSortOption sortOption = form.getSort() != null ? form.getSort() : PackSortOption.DATE_DESC;
        final AuctionSortOption auctionSortOption = form.getAuctionSort() != null ? form.getAuctionSort() : AuctionSortOption.TIME_REMAINING_DESC;

        final List<PackTag> selectedTags = form.getTags() != null ? new ArrayList<>(form.getTags()) : new ArrayList<>();

        final Municipality municipality = form.getLocation();
        final String cityFilter = municipality != null ? municipality.getCityName() : null;
        final Commerce.Category commerceCategory = form.getCommerceCategory();

        // types and timeRange are already normalised by the form setters
        final List<String> safeTimeRange = form.getTimeRange() != null ? form.getTimeRange() : Collections.emptyList();

        final boolean hasQuery = query != null && !query.trim().isEmpty();
        final String trimmedQuery = hasQuery ? query.trim() : null;
        final boolean hasTags = !selectedTags.isEmpty();

        final Optional<User> viewerOpt = authResolver.resolveUserOrEmpty();
        final boolean clientLoggedIn = viewerOpt.filter(u -> u.getRole() == User.Role.CLIENT).isPresent();
        final Long clientUserId = viewerOpt.filter(u -> u.getRole() == User.Role.CLIENT).map(User::getId).orElse(null);

        List<String> selectedTypes = form.getTypes() != null ? new ArrayList<>(form.getTypes()) : new ArrayList<>();
        if (!clientLoggedIn) {
            selectedTypes.removeIf(t -> TYPE_FAVORITES.equals(t));
        }

        final boolean packsSelected = selectedTypes.contains(TYPE_PACKS);
        final boolean auctionsSelected = selectedTypes.contains(TYPE_AUCTIONS);
        final boolean commercesSelected = selectedTypes.contains(TYPE_COMMERCES);

        final CatalogMode catalogMode;
        final int activeTypesCount = (packsSelected ? 1 : 0) + (auctionsSelected ? 1 : 0) + (commercesSelected ? 1 : 0);
        if (activeTypesCount == 0 || activeTypesCount > 1) {
            catalogMode = CatalogMode.ALL;
        } else if (packsSelected) {
            catalogMode = CatalogMode.PACKS;
        } else if (auctionsSelected) {
            catalogMode = CatalogMode.AUCTIONS;
        } else {
            catalogMode = CatalogMode.COMMERCES;
        }

        final boolean showAuctionsList = catalogMode == CatalogMode.AUCTIONS;
        final boolean showAuctionsCarousel = catalogMode == CatalogMode.ALL;
        final boolean showCommercesCarousel = catalogMode == CatalogMode.ALL;

        final boolean catalogDirectSaleGridRequiresPositiveStock =
                catalogMode == CatalogMode.ALL || catalogMode == CatalogMode.PACKS;
        final boolean catalogAuctionListingRequiresPositiveStock = true;



        int totalItems = 0;
        List<Pack> packs = Collections.emptyList();
        List<Auction> auctions = Collections.emptyList();
        List<Commerce> commerces = Collections.emptyList();

        if (showAuctionsList) {
            totalItems = auctionService.countFilteredAuctions(
                    trimmedQuery,
                    selectedTags.isEmpty() ? null : selectedTags,
                    cityFilter,
                    safeTimeRange.isEmpty() ? null : safeTimeRange,
                    catalogAuctionListingRequiresPositiveStock,
                    null);
        } else if (catalogMode == CatalogMode.COMMERCES) {
            totalItems = commerceService.countFilteredCommerces(trimmedQuery, cityFilter, commerceCategory);
        } else {
            totalItems = packService.countFilteredPacks(
                    trimmedQuery,
                    selectedTags.isEmpty() ? null : selectedTags,
                    cityFilter,
                    safeTimeRange.isEmpty() ? null : safeTimeRange,
                    catalogDirectSaleGridRequiresPositiveStock,
                    null);
        }

        final int activePageSize = (catalogMode == CatalogMode.COMMERCES) ? COMMERCES_PAGE_SIZE : PAGE_SIZE;
        final int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / activePageSize));
        final int safePage = Math.max(1, Math.min(page, totalPages));

        if (showAuctionsList) {
            auctions = auctionService.filterAuctions(
                    trimmedQuery,
                    selectedTags.isEmpty() ? null : selectedTags,
                    cityFilter,
                    safeTimeRange.isEmpty() ? null : safeTimeRange,
                    auctionSortOption,
                    safePage,
                    activePageSize,
                    catalogAuctionListingRequiresPositiveStock,
                    null);
        } else if (catalogMode == CatalogMode.COMMERCES) {
            commerces = commerceService.filterCommerces(trimmedQuery, cityFilter, commerceCategory, safePage, activePageSize);
        } else {
            packs = packService.filterPacks(
                    trimmedQuery,
                    selectedTags.isEmpty() ? null : selectedTags,
                    cityFilter,
                    safeTimeRange.isEmpty() ? null : safeTimeRange,
                    sortOption,
                    safePage,
                    activePageSize,
                    catalogDirectSaleGridRequiresPositiveStock,
                    null);
        }

        List<Auction> carouselAuctions = Collections.emptyList();
        if (showAuctionsCarousel) {
            carouselAuctions = auctionService.filterAuctions(
                    trimmedQuery,
                    selectedTags.isEmpty() ? null : selectedTags,
                    cityFilter,
                    safeTimeRange.isEmpty() ? null : safeTimeRange,
                    AuctionSortOption.TIME_REMAINING_ASC,
                    1,
                    AUCTION_CAROUSEL_SIZE,
                    catalogAuctionListingRequiresPositiveStock,
                    null);
        }

        List<Commerce> carouselCommerces = Collections.emptyList();
        if (showCommercesCarousel) {
            carouselCommerces = commerceService.filterCommerces(trimmedQuery, cityFilter, commerceCategory, 1, COMMERCES_CAROUSEL_SIZE);
        }

        /*
         * TECH DEBT — commerce ratings enrichment:
         * filterCommerces already computes AVG(r.rating) for sorting but discards the value;
         * we then issue a second batch query here. Future refactor: expose a service DTO
         * (e.g. CommerceWithRating) populated in the same DAO query, and drop this map.
         */
        final Map<Long, Double> commerceRatings = new HashMap<>();
        final List<Commerce> commercesForEnrichment = new ArrayList<>(commerces);
        commercesForEnrichment.addAll(carouselCommerces);

        final List<Long> commerceIds = new ArrayList<>();
        for (final Commerce c : commercesForEnrichment) {
            commerceIds.add(c.getUserId());
        }

        commerceRatings.putAll(commerceReviewService.findAverageRatingsForCommerceIds(commerceIds));

        mav.addObject("packs", packs);
        mav.addObject("auctions", auctions);
        mav.addObject("auctionsCarousel", carouselAuctions);
        mav.addObject("commerces", commerces);
        mav.addObject("commercesCarousel", carouselCommerces);
        mav.addObject("commerceRatings", commerceRatings);
        mav.addObject("catalogMode", catalogMode.name());
        mav.addObject("availableTags", PackTag.values());
        mav.addObject("selectedTags", selectedTags);
        mav.addObject("selectedTypes", selectedTypes);
        mav.addObject("availableSorts", PackSortOption.values());
        mav.addObject("currentSort", sortOption);
        mav.addObject("availableMunicipalities", Municipality.values());
        mav.addObject("selectedMunicipality", municipality);
        mav.addObject("selectedTimeRanges", safeTimeRange);
        mav.addObject("availableCommerceCategories", Commerce.Category.values());
        mav.addObject("selectedCommerceCategory", commerceCategory);
        mav.addObject("availableAuctionSorts", AuctionSortOption.values());
        mav.addObject("currentAuctionSort", auctionSortOption);
        mav.addObject("currentPage", safePage);
        mav.addObject("totalPages", totalPages);



        return mav;
    }


}
