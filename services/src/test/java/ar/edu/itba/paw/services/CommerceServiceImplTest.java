package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Commerce;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.persistence.CommerceDao;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CommerceServiceImplTest {

    static class InMemoryUserService implements UserService {
        private final Map<String, User> byEmail = new HashMap<>();
        private long nextId = 1;

        void seed(User u) { byEmail.put(u.getEmail(), u); }

        @Override
        public User createUser(String email, String password, String name) {
            return createUser(email, password, name, null, null);
        }

        @Override
        public User createUser(String email, String password, String name, String phone, User.Role role) {
            final User u = new User(nextId++, email, password, name, phone, role);
            byEmail.put(email, u);
            return u;
        }

        @Override
        public Optional<User> upgradeProvisionalUser(String email, String password, String name, User.Role role) {
            return Optional.empty();
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return Optional.ofNullable(byEmail.get(email));
        }

        @Override
        public Optional<User> findById(Long id) {
            return byEmail.values().stream().filter(u -> u.getId().equals(id)).findFirst();
        }
    }

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
    public void getOrCreateCommerce_userExistsWithDifferentRole_throws() {
        final InMemoryUserService userService = new InMemoryUserService();
        final InMemoryCommerceDao commerceDao = new InMemoryCommerceDao();

        final User existing = new User(1L, "u@ex.com", "pwd", "Name", null, User.Role.CLIENT);
        userService.seed(existing);

        final CommerceServiceImpl svc = new CommerceServiceImpl(userService, commerceDao);

        assertThrows(IllegalArgumentException.class, () -> svc.getOrCreateCommerce(existing.getEmail(), "p", "n", "cname", Commerce.Category.OTHER, null, (Integer) null, null, null, null, null, null));
    }

    @Test
    public void getOrCreateCommerce_userExistsAndCommerceExists_returnsExisting() {
        final InMemoryUserService userService = new InMemoryUserService();
        final InMemoryCommerceDao commerceDao = new InMemoryCommerceDao();

        final User existing = new User(2L, "shop@ex.com", "pwd", "Shop", null, User.Role.COMMERCE);
        userService.seed(existing);
        final Commerce stored = commerceDao.createCommerce(existing.getId(), "ShopName", Commerce.Category.BAKERY, "s", 1, "city", "prov", "pc", "09:00", "18:00");

        final CommerceServiceImpl svc = new CommerceServiceImpl(userService, commerceDao);
        final Commerce result = svc.getOrCreateCommerce(existing.getEmail(), "p", "n", "ShopName", Commerce.Category.BAKERY, null, (Integer) null, null, null, null, "09:00", "18:00");

        assertEquals(stored.getCommercialName(), result.getCommercialName());
        assertEquals(stored.getUserId(), result.getUserId());
    }

    @Test
    public void getOrCreateCommerce_userNotExists_createsUserAndCommerce() {
        final InMemoryUserService userService = new InMemoryUserService();
        final InMemoryCommerceDao commerceDao = new InMemoryCommerceDao();

        final CommerceServiceImpl svc = new CommerceServiceImpl(userService, commerceDao);
        final Commerce c = svc.getOrCreateCommerce("new@ex.com", "pw", "Name", "NewShop", Commerce.Category.RESTAURANT, "st", 10, "city", "prov", "pc", "08:00", "20:00");

        // user created and commerce stored
        final var maybeUser = userService.findByEmail("new@ex.com");
        assertEquals(true, maybeUser.isPresent());
        final var maybeCommerce = commerceDao.findByUserId(maybeUser.get().getId());
        assertEquals(true, maybeCommerce.isPresent());
        assertEquals("NewShop", maybeCommerce.get().getCommercialName());
    }
}
