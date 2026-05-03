package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.persistence.CommerceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class CommerceServiceImpl implements CommerceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommerceServiceImpl.class);

    private final CommerceDao commerceDao;

    @Autowired
    public CommerceServiceImpl(final CommerceDao commerceDao) {
        this.commerceDao = commerceDao;
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
}
