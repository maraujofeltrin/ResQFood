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
import ar.edu.itba.paw.services.pack.PackFavoriteService;
import ar.edu.itba.paw.services.pack.PackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import ar.edu.itba.paw.webapp.form.CatalogFilterForm;

import java.nio.charset.StandardCharsets;
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
    private static final int AUCTION_CAROUSEL_SIZE = 6;
    private static final int FAVORITES_CAROUSEL_SIZE = 6;
    private static final String TYPE_PACKS = "packs";
    private static final String TYPE_AUCTIONS = "auctions";
    private static final String TYPE_FAVORITES = "favorites";
    private static final Set<String> ALLOWED_TIME_RANGES = Set.of("morning", "afternoon", "evening");

    private enum CatalogMode {
        ALL,
        PACKS,
        AUCTIONS,
        FAVORITES
    }

    private final PackService packService;
    private final CommerceService commerceService;
    private final AuctionService auctionService;
    private final PackFavoriteService packFavoriteService;
    private final AuthenticatedUserResolver authResolver;

    @Autowired
    public PackCatalogModelBuilder(final PackService packService, final CommerceService commerceService,
            final AuctionService auctionService,
            final PackFavoriteService packFavoriteService,
            final AuthenticatedUserResolver authResolver) {
        this.packService = packService;
        this.commerceService = commerceService;
        this.auctionService = auctionService;
        this.packFavoriteService = packFavoriteService;
        this.authResolver = authResolver;
    }

    public ModelAndView buildPackCatalog(final CatalogFilterForm form) {

        final String query = form.getQ();
        final List<String> tagNames = form.getTags();
        final String sort = form.getSort();
        final String auctionSort = form.getAuctionSort();
        final String locationParam = form.getLocation();
        final int page = form.getPage();

        final ModelAndView mav = new ModelAndView("packs/packCatalogView");
        final PackSortOption sortOption = PackSortOption.fromString(sort);
        final AuctionSortOption auctionSortOption = AuctionSortOption.fromString(auctionSort);

        final List<PackTag> selectedTags = new ArrayList<>();
        if (tagNames != null) {
            for (final String name : tagNames) {
                try {
                    selectedTags.add(PackTag.valueOf(name));
                } catch (final IllegalArgumentException ex) {
                    LOGGER.debug("Unknown pack tag in catalog filter query: {}", name, ex);
                }
            }
        }

        final Municipality municipality = Municipality.fromString(locationParam);
        final String cityFilter = municipality != null ? municipality.getCityName() : null;

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

        final boolean favoritesSelected = clientLoggedIn && selectedTypes.contains(TYPE_FAVORITES);
        final boolean packsSelected = selectedTypes.contains(TYPE_PACKS);
        final boolean auctionsSelected = selectedTypes.contains(TYPE_AUCTIONS);

        final CatalogMode catalogMode;
        if (favoritesSelected) {
            catalogMode = CatalogMode.FAVORITES;
        } else if ((packsSelected && auctionsSelected) || (!packsSelected && !auctionsSelected)) {
            catalogMode = CatalogMode.ALL;
        } else if (packsSelected) {
            catalogMode = CatalogMode.PACKS;
        } else {
            catalogMode = CatalogMode.AUCTIONS;
        }

        final boolean showAuctionsList = catalogMode == CatalogMode.AUCTIONS;
        final boolean showAuctionsCarousel = catalogMode == CatalogMode.ALL;

        final boolean catalogDirectSaleGridRequiresPositiveStock =
                catalogMode == CatalogMode.ALL || catalogMode == CatalogMode.PACKS;
        final boolean catalogAuctionListingRequiresPositiveStock = true;

        List<Pack> favoritesCarouselPacks = Collections.emptyList();
        if (showAuctionsCarousel && clientUserId != null) {
            favoritesCarouselPacks = packFavoriteService.listActiveFavoritePacks(
                    clientUserId, FAVORITES_CAROUSEL_SIZE);
        }

        int totalItems = 0;
        List<Pack> packs = Collections.emptyList();
        List<Auction> auctions = Collections.emptyList();

        if (catalogMode == CatalogMode.FAVORITES) {
            totalItems = clientUserId != null ? packFavoriteService.countActiveFavoritePacks(clientUserId) : 0;
        } else if (showAuctionsList) {
            totalItems = auctionService.countFilteredAuctions(
                    trimmedQuery,
                    selectedTags.isEmpty() ? null : selectedTags,
                    cityFilter,
                    safeTimeRange.isEmpty() ? null : safeTimeRange,
                    catalogAuctionListingRequiresPositiveStock);
        } else {
            totalItems = packService.countFilteredPacks(
                    trimmedQuery,
                    selectedTags.isEmpty() ? null : selectedTags,
                    cityFilter,
                    safeTimeRange.isEmpty() ? null : safeTimeRange,
                    catalogDirectSaleGridRequiresPositiveStock);
        }

        final int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));
        final int safePage = Math.max(1, Math.min(page, totalPages));

        if (catalogMode == CatalogMode.FAVORITES) {
            if (clientUserId != null) {
                packs = packFavoriteService.listActiveFavoritePacks(clientUserId, safePage, PAGE_SIZE);
            }
        } else if (showAuctionsList) {
            auctions = auctionService.filterAuctions(
                    trimmedQuery,
                    selectedTags.isEmpty() ? null : selectedTags,
                    cityFilter,
                    safeTimeRange.isEmpty() ? null : safeTimeRange,
                    auctionSortOption,
                    safePage,
                    PAGE_SIZE,
                    catalogAuctionListingRequiresPositiveStock);
        } else {
            packs = packService.filterPacks(
                    trimmedQuery,
                    selectedTags.isEmpty() ? null : selectedTags,
                    cityFilter,
                    safeTimeRange.isEmpty() ? null : safeTimeRange,
                    sortOption,
                    safePage,
                    PAGE_SIZE,
                    catalogDirectSaleGridRequiresPositiveStock);
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
                    catalogAuctionListingRequiresPositiveStock);
        }

        final Map<Long, String> commerceNames = new HashMap<>();
        for (final Pack pack : packs) {
            commerceNames.putIfAbsent(
                    pack.getId(),
                    commerceService.findByUserId(pack.getCommerceId())
                            .map(Commerce::getCommercialName)
                            .orElse("—"));
        }
        for (final Auction auctionEntity : auctions) {
            if (auctionEntity.getPack() == null) {
                continue;
            }
            final Pack auctionPack = auctionEntity.getPack();
            commerceNames.putIfAbsent(
                    auctionPack.getId(),
                    commerceService.findByUserId(auctionPack.getCommerceId())
                            .map(Commerce::getCommercialName)
                            .orElse("—"));
        }
        for (final Auction auctionEntity : carouselAuctions) {
            if (auctionEntity.getPack() == null) {
                continue;
            }
            final Pack auctionPack = auctionEntity.getPack();
            commerceNames.putIfAbsent(
                    auctionPack.getId(),
                    commerceService.findByUserId(auctionPack.getCommerceId())
                            .map(Commerce::getCommercialName)
                            .orElse("—"));
        }
        for (final Pack fp : favoritesCarouselPacks) {
            commerceNames.putIfAbsent(
                    fp.getId(),
                    commerceService.findByUserId(fp.getCommerceId())
                            .map(Commerce::getCommercialName)
                            .orElse("—"));
        }

        final StringBuilder baseUrlBuilder = new StringBuilder("/packs");
        boolean firstParam = true;
        if (hasQuery) {
            baseUrlBuilder.append(firstParam ? "?" : "&").append("q=")
                    .append(java.net.URLEncoder.encode(trimmedQuery, StandardCharsets.UTF_8));
            firstParam = false;
        }
        if (!selectedTags.isEmpty()) {
            for (final PackTag tag : selectedTags) {
                baseUrlBuilder.append(firstParam ? "?" : "&").append("tags=").append(tag.name());
                firstParam = false;
            }
        }
        for (final String selectedType : selectedTypes) {
            baseUrlBuilder.append(firstParam ? "?" : "&").append("types=").append(selectedType);
            firstParam = false;
        }
        if (catalogMode != CatalogMode.AUCTIONS && sort != null && !sort.isBlank()) {
            baseUrlBuilder.append(firstParam ? "?" : "&").append("sort=").append(sortOption.name());
            firstParam = false;
        }
        if (municipality != null) {
            baseUrlBuilder.append(firstParam ? "?" : "&").append("location=").append(municipality.name());
            firstParam = false;
        }
        if (!safeTimeRange.isEmpty()) {
            for (final String tr : safeTimeRange) {
                baseUrlBuilder.append(firstParam ? "?" : "&").append("timeRange=").append(tr);
                firstParam = false;
            }
        }
        if (catalogMode == CatalogMode.AUCTIONS) {
            baseUrlBuilder.append(firstParam ? "?" : "&").append("auctionSort=").append(auctionSortOption.name());
            firstParam = false;
        }

        final StringBuilder auctionsViewAllBuilder = new StringBuilder("/packs");
        boolean viewAllFirstParam = true;
        if (hasQuery) {
            auctionsViewAllBuilder.append(viewAllFirstParam ? "?" : "&").append("q=")
                    .append(java.net.URLEncoder.encode(trimmedQuery, StandardCharsets.UTF_8));
            viewAllFirstParam = false;
        }
        if (hasTags) {
            for (final PackTag tag : selectedTags) {
                auctionsViewAllBuilder.append(viewAllFirstParam ? "?" : "&").append("tags=").append(tag.name());
                viewAllFirstParam = false;
            }
        }
        if (municipality != null) {
            auctionsViewAllBuilder.append(viewAllFirstParam ? "?" : "&").append("location=").append(municipality.name());
            viewAllFirstParam = false;
        }
        if (!safeTimeRange.isEmpty()) {
            for (final String tr : safeTimeRange) {
                auctionsViewAllBuilder.append(viewAllFirstParam ? "?" : "&").append("timeRange=").append(tr);
                viewAllFirstParam = false;
            }
        }
        auctionsViewAllBuilder.append(viewAllFirstParam ? "?" : "&").append("types=").append(TYPE_AUCTIONS);
        auctionsViewAllBuilder.append("&auctionSort=").append(auctionSortOption.name());

        final StringBuilder favoritesViewAllBuilder = new StringBuilder("/packs");
        boolean favViewFirst = true;
        if (hasQuery) {
            favoritesViewAllBuilder.append(favViewFirst ? "?" : "&").append("q=")
                    .append(java.net.URLEncoder.encode(trimmedQuery, StandardCharsets.UTF_8));
            favViewFirst = false;
        }
        if (hasTags) {
            for (final PackTag tag : selectedTags) {
                favoritesViewAllBuilder.append(favViewFirst ? "?" : "&").append("tags=").append(tag.name());
                favViewFirst = false;
            }
        }
        if (municipality != null) {
            favoritesViewAllBuilder.append(favViewFirst ? "?" : "&").append("location=").append(municipality.name());
            favViewFirst = false;
        }
        if (!safeTimeRange.isEmpty()) {
            for (final String tr : safeTimeRange) {
                favoritesViewAllBuilder.append(favViewFirst ? "?" : "&").append("timeRange=").append(tr);
                favViewFirst = false;
            }
        }
        if (sort != null && !sort.isBlank()) {
            favoritesViewAllBuilder.append(favViewFirst ? "?" : "&").append("sort=").append(sortOption.name());
            favViewFirst = false;
        }
        favoritesViewAllBuilder.append(favViewFirst ? "?" : "&").append("types=").append(TYPE_FAVORITES);

        mav.addObject("packs", packs);
        mav.addObject("auctions", auctions);
        mav.addObject("auctionsCarousel", carouselAuctions);
        mav.addObject("catalogMode", catalogMode.name());
        mav.addObject("commerceNames", commerceNames);
        mav.addObject("availableTags", PackTag.values());
        mav.addObject("selectedTags", selectedTags);
        mav.addObject("selectedTypes", selectedTypes);
        mav.addObject("availableSorts", PackSortOption.values());
        mav.addObject("currentSort", sortOption);
        mav.addObject("availableMunicipalities", Municipality.values());
        mav.addObject("selectedMunicipality", municipality);
        mav.addObject("selectedTimeRanges", safeTimeRange);
        mav.addObject("availableAuctionSorts", AuctionSortOption.values());
        mav.addObject("currentAuctionSort", auctionSortOption);
        mav.addObject("currentPage", safePage);
        mav.addObject("totalPages", totalPages);
        mav.addObject("paginationBaseUrl", baseUrlBuilder.toString());
        mav.addObject("auctionsViewAllUrl", auctionsViewAllBuilder.toString());
        mav.addObject("favoritesViewAllUrl", favoritesViewAllBuilder.toString());

        final Map<Long, Auction> favoritePackActiveAuctions = new HashMap<>();
        final List<Pack> favoritePacksForAuctionEnrichment = new ArrayList<>();
        if (catalogMode == CatalogMode.FAVORITES) {
            favoritePacksForAuctionEnrichment.addAll(packs);
        } else {
            favoritePacksForAuctionEnrichment.addAll(favoritesCarouselPacks);
        }
        for (final Pack p : favoritePacksForAuctionEnrichment) {
            auctionService.findByPackId(p.getId())
                    .filter(Auction::isActive)
                    .ifPresent(a -> favoritePackActiveAuctions.put(p.getId(), a));
        }
        mav.addObject("favoritesCarouselPacks", favoritesCarouselPacks);
        mav.addObject("favoritePackActiveAuctions", favoritePackActiveAuctions);

        return mav;
    }


}
