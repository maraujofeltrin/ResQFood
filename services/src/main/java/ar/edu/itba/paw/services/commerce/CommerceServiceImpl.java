package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.CommerceProfileException;
import ar.edu.itba.paw.persistence.CommerceDao;
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
    private final CommerceDao commerceDao;
    private final PackService packService;

    @Autowired
    public CommerceServiceImpl(final CommerceDao commerceDao, final PackService packService) {
        this.commerceDao = commerceDao;
        this.packService = packService;
    }

    @Transactional(readOnly = true)
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
            throw new CommerceProfileException(CommerceProfileException.Reason.MISSING_CATEGORY);
        }
        final String open = openingTime == null ? "" : openingTime.trim();
        final String close = closingTime == null ? "" : closingTime.trim();
        if (open.isEmpty() || close.isEmpty()) {
            throw new CommerceProfileException(CommerceProfileException.Reason.MISSING_TIMES);
        }
        final String st = street == null ? "" : street.trim();
        if (st.isEmpty() || city == null) {
            throw new CommerceProfileException(CommerceProfileException.Reason.MISSING_ADDRESS);
        }
        final Commerce current = commerceDao.findByUserId(userId)
                .orElseThrow(() -> {
                    LOGGER.warn("updateProfileFields: commerce not found userId={}", userId);
                    return new NoSuchElementException("Commerce not found for user: " + userId);
                });
        if (province == null || !Commerce.PROVINCE_BUENOS_AIRES.equalsIgnoreCase(province.trim())) {
            throw new CommerceProfileException(CommerceProfileException.Reason.INVALID_PROVINCE);
        }
        current.setCategory(category);
        current.setStreet(st);
        current.setStreetNumber(streetNumber);
        current.setCity(city);
        current.setProvince(Commerce.PROVINCE_BUENOS_AIRES);
        current.setPostalCode(postalCode == null || postalCode.isBlank() ? null : postalCode.trim());
        current.setOpeningTime(open);
        current.setClosingTime(close);
        commerceDao.update(current);
        LOGGER.info("Commerce profile updated for userId={}", userId);
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    @Override
    public int countFilteredCommerces(String query, String cityFilter, Commerce.Category categoryFilter) {
        return commerceDao.countFilteredCommerces(query, cityFilter, categoryFilter);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Pack> getPublicOffers(final long commerceUserId, final int page, final int pageSize) {
        return packService.getPublicOffersByCommerce(Long.valueOf(commerceUserId), page, pageSize);
    }

    @Transactional(readOnly = true)
    @Override
    public int countPublicOffers(final long commerceUserId) {
        return packService.countPublicOffersByCommerce(Long.valueOf(commerceUserId));
    }

    @Transactional
    @Override
    public Commerce createCommerce(final Long userId, final String commercialName, final Commerce.Category category,
            final String street, final Integer streetNumber, final Municipality city, final String province,
            final String postalCode, final String openingTime, final String closingTime) {
        return commerceDao.createCommerce(userId, commercialName, category, street, streetNumber, city, province,
                postalCode, openingTime, closingTime);
    }

    @Transactional
    @Override
    public Commerce update(final Commerce commerce) {
        return commerceDao.update(commerce);
    }
}
