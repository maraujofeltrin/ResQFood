package ar.edu.itba.paw.services.commerce;

import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.persistence.CommerceDao;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommerceServiceImplTest {

    static class InMemoryCommerceDao implements CommerceDao {
        private final Map<Long, Commerce> store = new HashMap<>();

        @Override
        public Commerce createCommerce(Long userId, String commercialName, Commerce.Category category, String street, Integer streetNumber, String city, String province, String postalCode, String openingTime, String closingTime) {
            final Commerce c = new Commerce(userId, commercialName, category, street, streetNumber, city, province, postalCode, openingTime, closingTime);
            store.put(userId, c);
            return c;
        }

        @Override
        public Optional<Commerce> findByUserId(Long userId) {
            return Optional.ofNullable(store.get(userId));
        }

        @Override
        public Commerce update(Commerce commerce) {
            store.put(commerce.getUserId(), commerce);
            return commerce;
        }
    }

    @Test
    public void findByUserId_existing_returnsCommerce() {
        final InMemoryCommerceDao commerceDao = new InMemoryCommerceDao();
        final Commerce stored = commerceDao.createCommerce(2L, "ShopName", Commerce.Category.BAKERY, "s", 1, "city", "prov", "pc", "09:00", "18:00");

        final CommerceServiceImpl svc = new CommerceServiceImpl(commerceDao);
        final Optional<Commerce> result = svc.findByUserId(2L);

        assertTrue(result.isPresent());
        assertEquals(stored.getCommercialName(), result.get().getCommercialName());
        assertEquals(stored.getUserId(), result.get().getUserId());
    }

    @Test
    public void findByUserId_nonExistent_returnsEmpty() {
        final InMemoryCommerceDao commerceDao = new InMemoryCommerceDao();
        final CommerceServiceImpl svc = new CommerceServiceImpl(commerceDao);

        final Optional<Commerce> result = svc.findByUserId(999L);

        assertTrue(result.isEmpty());
    }
}
