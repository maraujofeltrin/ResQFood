package ar.edu.itba.paw.services.user;

import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.persistence.ClientDao;
import ar.edu.itba.paw.persistence.CommerceDao;
import ar.edu.itba.paw.persistence.UserDao;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceImplTest {

    /** Stores whatever password string it receives — no hashing in unit tests. */
    static class NoOpPasswordEncoder implements PasswordEncoder {
        @Override
        public String encode(final CharSequence rawPassword) {
            return rawPassword.toString();
        }

        @Override
        public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
            return rawPassword.toString().equals(encodedPassword);
        }
    }

    /** No-op: tests that exercise tryRegister do not need email-sending side-effects. */
    static class NoOpEmailVerificationTokenService
            implements ar.edu.itba.paw.services.security.EmailVerificationTokenService {
        @Override
        public void sendVerificationMail(final Long userId, final String email, final String baseUrl,
            final Locale locale) { }
        @Override
        public java.util.Optional<ar.edu.itba.paw.models.user.User> verifyEmailAndGetUser(final String token) {
            return java.util.Optional.empty();
        }
        @Override
        public boolean verifyEmail(final String token) { return false; }
        @Override
        public void resendVerificationMail(final String email, final String baseUrl) { }
    }

    static class InMemoryUserDao implements UserDao {
        private final Map<Long, User> byId = new HashMap<>();
        private final Map<String, User> byEmail = new HashMap<>();
        private long nextId = 1L;

        @Override
        public User createUser(final String email, final String password, final String name, final String phone,
                final User.Role role, final Locale locale) {
            final User u = new User(nextId++, email, password, name, phone, role, false, locale);
            byId.put(u.getId(), u);
            byEmail.put(email, u);
            return u;
        }

        @Override
        public User updateUser(final Long id, final String password, final String name, final String phone, final User.Role role) {
            final User current = byId.get(id);
            if (current == null) {
                throw new IllegalStateException("User not found: " + id);
            }
            final User updated = new User(id, current.getEmail(), password, name, phone, role, current.isVerified(),
                    current.getLocale());
            byId.put(id, updated);
            byEmail.put(updated.getEmail(), updated);
            return updated;
        }

        @Override
        public Optional<User> findByEmail(final String email) {
            return Optional.ofNullable(byEmail.get(email));
        }

        @Override
        public Optional<User> findById(final Long id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public void updatePassword(final Long id, final String password) {
            final User current = byId.get(id);
            if (current == null) {
                throw new IllegalStateException("User not found: " + id);
            }
            final User updated = new User(id, current.getEmail(), password, current.getName(), current.getPhone(),
                    current.getRole(), current.isVerified(), current.getLocale());
            byId.put(id, updated);
            byEmail.put(updated.getEmail(), updated);
        }

        @Override
        public void markVerified(final Long userId) {
            final User current = byId.get(userId);
            if (current == null) {
                throw new IllegalStateException("User not found: " + userId);
            }
            final User verifiedUser = new User(current.getId(), current.getEmail(), current.getPassword(),
                    current.getName(), current.getPhone(), current.getRole(), true, current.getLocale());
            byId.put(userId, verifiedUser);
            byEmail.put(verifiedUser.getEmail(), verifiedUser);
        }
    }

    static class InMemoryClientDao implements ClientDao {
        private final Map<Long, Client> byUserId = new HashMap<>();

        @Override
        public Client createClient(final Long userId, final String name, final String lastName,
                final Boolean notificationsVisibilityPreferences) {
            final Client client = new Client(userId, name, lastName, notificationsVisibilityPreferences);
            byUserId.put(userId, client);
            return client;
        }

        @Override
        public Optional<Client> findByUserId(final Long userId) {
            return Optional.ofNullable(byUserId.get(userId));
        }

        @Override
        public Client update(final Client client) {
            byUserId.put(client.getUserId(), client);
            return client;
        }
    }

    static class InMemoryCommerceDao implements CommerceDao {
        private final Map<Long, Commerce> byUserId = new HashMap<>();

        @Override
        public Commerce createCommerce(final Long userId, final String commercialName, final Commerce.Category category,
                final String street, final Integer streetNumber, final String city, final String province,
                final String postalCode, final String openingTime, final String closingTime) {
            final Commerce commerce = new Commerce(userId, commercialName, category, street, streetNumber, city,
                    province, postalCode, openingTime, closingTime);
            byUserId.put(userId, commerce);
            return commerce;
        }

        @Override
        public Optional<Commerce> findByUserId(final Long userId) {
            return Optional.ofNullable(byUserId.get(userId));
        }

        @Override
        public Commerce update(final Commerce commerce) {
            byUserId.put(commerce.getUserId(), commerce);
            return commerce;
        }
    }

    @Test
    public void createUser_and_findByEmailAndId() {
        final InMemoryUserDao dao = new InMemoryUserDao();
        final UserServiceImpl svc = new UserServiceImpl(dao, new InMemoryClientDao(), new InMemoryCommerceDao(),
            new NoOpPasswordEncoder(), new NoOpEmailVerificationTokenService());

        final User u = svc.createUser(new User(null, "a@b.com", "pw", "Name", null, null, false), null, null);
        assertNotNull(u.getId());
        final Optional<User> byEmail = svc.findByEmail("a@b.com");
        assertTrue(byEmail.isPresent());
        assertEquals(u.getId(), byEmail.get().getId());

        final Optional<User> byId = svc.findById(u.getId());
        assertTrue(byId.isPresent());
        assertEquals("Name", byId.get().getName());
    }

    @Test
    public void createUser_withRole_storesRole() {
        final InMemoryUserDao dao = new InMemoryUserDao();
        final UserServiceImpl svc = new UserServiceImpl(dao, new InMemoryClientDao(), new InMemoryCommerceDao(),
            new NoOpPasswordEncoder(), new NoOpEmailVerificationTokenService());

        final User userToCreate = new User(null, "c@d.com", "pw", "N", "123", User.Role.COMMERCE, false);
        final Commerce commerceProfile = new Commerce(null, "Shop", Commerce.Category.OTHER, "st", 1, "city", "prov", "pc", "09:00", "18:00");
        svc.createUser(userToCreate, null, commerceProfile);
        final Optional<User> maybe = svc.findByEmail("c@d.com");
        assertTrue(maybe.isPresent());
        assertEquals(User.Role.COMMERCE, maybe.get().getRole());
        assertEquals("123", maybe.get().getPhone());
    }

}
