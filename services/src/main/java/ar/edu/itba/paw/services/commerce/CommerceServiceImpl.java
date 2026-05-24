package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.auction.AuctionSortOption;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackSortOption;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.persistence.CommerceDao;
import ar.edu.itba.paw.services.auction.AuctionService;
import ar.edu.itba.paw.services.pack.PackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class CommerceServiceImpl implements CommerceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommerceServiceImpl.class);
    private static final int PUBLIC_PROFILE_AUCTION_CAP = 50;

    private final CommerceDao commerceDao;
    private final PackService packService;
    private final AuctionService auctionService;

    @Autowired
    public CommerceServiceImpl(final CommerceDao commerceDao, final PackService packService,
            final AuctionService auctionService) {
        this.commerceDao = commerceDao;
        this.packService = packService;
        this.auctionService = auctionService;
    }

    @Override
    public Optional<Commerce> findByUserId(final Long userId) {
        return commerceDao.findByUserId(userId);
    }

    @Transactional
    @Override
    public void updateProfileFields(final long userId, final Commerce.Category category, final String street,
            final Integer streetNumber, final Municipality city, final String province, final String postalCode,
            final String openingTime, final String closingTime) {
        if (category == null) {
            throw new IllegalArgumentException("Category is required");
        }
        final String open = openingTime == null ? "" : openingTime.trim();
        final String close = closingTime == null ? "" : closingTime.trim();
        if (open.isEmpty() || close.isEmpty()) {
            throw new IllegalArgumentException("Opening and closing times are required");
        }
        final String st = street == null ? "" : street.trim();
        if (st.isEmpty() || city == null) {
            throw new IllegalArgumentException("Street and city are required");
        }
        final Commerce current = commerceDao.findByUserId(userId)
                .orElseThrow(() -> {
                    LOGGER.warn("updateProfileFields: commerce not found userId={}", userId);
                    return new NoSuchElementException("Commerce not found for user: " + userId);
                });
        if (province == null || !Commerce.PROVINCE_BUENOS_AIRES.equalsIgnoreCase(province.trim())) {
            throw new IllegalArgumentException("Commerce province must be " + Commerce.PROVINCE_BUENOS_AIRES);
        }
        final Commerce updated = new Commerce(
                userId,
                current.getCommercialName(),
                category,
                st,
                streetNumber,
                city,
                Commerce.PROVINCE_BUENOS_AIRES,
                postalCode == null || postalCode.isBlank() ? null : postalCode.trim(),
                open,
                close);
        commerceDao.update(updated);
        LOGGER.info("Commerce profile updated for userId={}", userId);
    }

    @Override
    public java.util.List<Commerce> filterCommerces(String query, String cityFilter, Commerce.Category categoryFilter, int page, int pageSize) {
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 12;
        }
        return commerceDao.filterCommerces(query, cityFilter, categoryFilter, page, pageSize);
    }

    @Override
    public int countFilteredCommerces(String query, String cityFilter, Commerce.Category categoryFilter) {
        return commerceDao.countFilteredCommerces(query, cityFilter, categoryFilter);
    }

    @Override
    public CommercePublicOffers getPublicOffers(final long commerceUserId, final int packPage, final int packPageSize) {
        final Long commerceFilter = Long.valueOf(commerceUserId);
        final int directPacksTotal = packService.countFilteredPacks(null, null, null, null, true, commerceFilter);
        final int normalizedPageSize = packPageSize < 1 ? 12 : packPageSize;
        final int totalPages = Math.max(1, (int) Math.ceil((double) directPacksTotal / normalizedPageSize));
        final int safePackPage = Math.max(1, Math.min(packPage < 1 ? 1 : packPage, totalPages));
        final List<Pack> directPacks = packService.filterPacks(null, null, null, null, PackSortOption.DATE_DESC,
                safePackPage, normalizedPageSize, true, commerceFilter);
        final List<Auction> activeAuctions = auctionService.filterAuctions(null, null, null, null,
                AuctionSortOption.TIME_REMAINING_ASC, 1, PUBLIC_PROFILE_AUCTION_CAP, true, commerceFilter);
        final int activeAuctionsTotal = auctionService.countFilteredAuctions(null, null, null, null, true,
                commerceFilter);

        return new CommercePublicOffers(directPacks, directPacksTotal, activeAuctions, activeAuctionsTotal);
    }
}
