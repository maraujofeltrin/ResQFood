package ar.edu.itba.paw.webapp.controller.utils;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.auction.AuctionSortOption;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackSortOption;
import ar.edu.itba.paw.models.pack.PackTag;
import ar.edu.itba.paw.services.auction.AuctionService;
import ar.edu.itba.paw.services.commerce.CommerceService;
import ar.edu.itba.paw.services.pack.PackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Builds the pack/auction catalog page model (search, filters, pagination).
 */
@Service
public class PackCatalogUtils {

    private static final int PAGE_SIZE = 6;
    private static final int AUCTION_CAROUSEL_SIZE = 6;
    private static final String TYPE_PACKS = "packs";
    private static final String TYPE_AUCTIONS = "auctions";

    private enum CatalogMode {
        ALL,
        PACKS,
        AUCTIONS
    }

    private final PackService packService;
    private final CommerceService commerceService;
    private final AuctionService auctionService;

    @Autowired
    public PackCatalogUtils(final PackService packService, final CommerceService commerceService,
            final AuctionService auctionService) {
        this.packService = packService;
        this.commerceService = commerceService;
        this.auctionService = auctionService;
    }

    public ModelAndView buildPackCatalog(
            final String query,
            final List<String> tagNames,
            final String sort,
            final List<String> types,
            final String auctionSort,
            final String locationParam,
            final List<String> timeRange,
            final int page) {

        final ModelAndView mav = new ModelAndView("packs/packCatalogView");
        final PackSortOption sortOption = PackSortOption.fromString(sort);
        final AuctionSortOption auctionSortOption = AuctionSortOption.fromString(auctionSort);

        final List<PackTag> selectedTags = new ArrayList<>();
        if (tagNames != null) {
            for (final String name : tagNames) {
                try {
                    selectedTags.add(PackTag.valueOf(name));
                } catch (final IllegalArgumentException ignored) {
                }
            }
        }

        final Municipality municipality = Municipality.fromString(locationParam);
        final String cityFilter = municipality != null ? municipality.getCityName() : null;

        final List<String> safeTimeRange = timeRange != null ? timeRange : new ArrayList<>();

        final boolean hasQuery = query != null && !query.trim().isEmpty();
        final String trimmedQuery = hasQuery ? query.trim() : null;
        final boolean hasTags = !selectedTags.isEmpty();

        final List<String> selectedTypes = normalizeTypes(types);
        final boolean packsSelected = selectedTypes.contains(TYPE_PACKS);
        final boolean auctionsSelected = selectedTypes.contains(TYPE_AUCTIONS);

        final CatalogMode catalogMode;
        if ((packsSelected && auctionsSelected) || (!packsSelected && !auctionsSelected)) {
            catalogMode = CatalogMode.ALL;
        } else if (packsSelected) {
            catalogMode = CatalogMode.PACKS;
        } else {
            catalogMode = CatalogMode.AUCTIONS;
        }

        final boolean showPacks = catalogMode != CatalogMode.AUCTIONS;
        final boolean showAuctionsList = catalogMode == CatalogMode.AUCTIONS;
        final boolean showAuctionsCarousel = catalogMode == CatalogMode.ALL;

        int totalItems = 0;
        List<Pack> packs = Collections.emptyList();
        List<Auction> auctions = Collections.emptyList();

        if (showAuctionsList) {
            totalItems = auctionService.countFilteredAuctions(
                    trimmedQuery,
                    selectedTags.isEmpty() ? null : selectedTags,
                    cityFilter,
                    safeTimeRange.isEmpty() ? null : safeTimeRange);
            int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));
            int safePage = Math.max(1, Math.min(page, totalPages));
            
            auctions = auctionService.filterAuctions(
                    trimmedQuery,
                    selectedTags.isEmpty() ? null : selectedTags,
                    cityFilter,
                    safeTimeRange.isEmpty() ? null : safeTimeRange,
                    auctionSortOption,
                    safePage,
                    PAGE_SIZE);
        } else {
            totalItems = packService.countFilteredPacks(
                    trimmedQuery,
                    selectedTags.isEmpty() ? null : selectedTags,
                    cityFilter,
                    safeTimeRange.isEmpty() ? null : safeTimeRange);
            int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));
            int safePage = Math.max(1, Math.min(page, totalPages));

            packs = packService.filterPacks(
                    trimmedQuery,
                    selectedTags.isEmpty() ? null : selectedTags,
                    cityFilter,
                    safeTimeRange.isEmpty() ? null : safeTimeRange,
                    sortOption,
                    safePage,
                    PAGE_SIZE);
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
                    AUCTION_CAROUSEL_SIZE);
        }

        final int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));
        final int safePage = Math.max(1, Math.min(page, totalPages));

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
        return mav;
    }

    private static List<String> normalizeTypes(final List<String> rawTypes) {
        if (rawTypes == null || rawTypes.isEmpty()) {
            return Collections.emptyList();
        }
        final Set<String> values = new LinkedHashSet<>();
        for (final String raw : rawTypes) {
            if (raw == null) {
                continue;
            }
            final String type = raw.trim().toLowerCase(Locale.ROOT);
            if (TYPE_PACKS.equals(type) || TYPE_AUCTIONS.equals(type)) {
                values.add(type);
            }
        }
        return new ArrayList<>(values);
    }

}
