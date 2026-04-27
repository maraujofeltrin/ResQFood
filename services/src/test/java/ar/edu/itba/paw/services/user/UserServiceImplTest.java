package ar.edu.itba.paw.services.user;

import ar.edu.itba.paw.models.image.Image;
import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.persistence.ClientDao;
import ar.edu.itba.paw.persistence.CommerceDao;
import ar.edu.itba.paw.persistence.UserDao;
import ar.edu.itba.paw.services.image.ImageService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
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

    static class StubImageService implements ImageService {
        private long nextId = 100L;

        @Override
        public Image saveImage(final byte[] data, final String contentType) {
            return new Image(nextId++, data, contentType);
        }

        @Override
        public java.util.Optional<Image> getImage(final long id) {
            return java.util.Optional.empty();
        }
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
                    current.getLocale(), current.getProfileImageId());
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
                    current.getRole(), current.isVerified(), current.getLocale(), current.getProfileImageId());
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
                    current.getName(), current.getPhone(), current.getRole(), true, current.getLocale(),
                    current.getProfileImageId());
            byId.put(userId, verifiedUser);
            byEmail.put(verifiedUser.getEmail(), verifiedUser);
        }

        @Override
        public void updateProfileImage(final long userId, final Long imageId) {
            final User current = byId.get(userId);
            if (current == null) {
                throw new IllegalStateException("User not found: " + userId);
            }
            final User updated = new User(current.getId(), current.getEmail(), current.getPassword(),
                    current.getName(), current.getPhone(), current.getRole(), current.isVerified(), current.getLocale(),
                    imageId);
            byId.put(userId, updated);
            byEmail.put(updated.getEmail(), updated);
        }

        @Override
        public void updateLocale(final long userId, final String languageTag) {
            final User current = byId.get(userId);
            if (current == null) {
                throw new IllegalStateException("User not found: " + userId);
            }
            final Locale newLocale = Locale.forLanguageTag(languageTag);
            final User updated = new User(current.getId(), current.getEmail(), current.getPassword(),
                    current.getName(), current.getPhone(), current.getRole(), current.isVerified(), newLocale,
                    current.getProfileImageId());
            byId.put(userId, updated);
            byEmail.put(updated.getEmail(), updated);
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
            new NoOpPasswordEncoder(), new NoOpEmailVerificationTokenService(), new StubImageService());

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
            new NoOpPasswordEncoder(), new NoOpEmailVerificationTokenService(), new StubImageService());

        final User userToCreate = new User(null, "c@d.com", "pw", "N", "123", User.Role.COMMERCE, false);
        final Commerce commerceProfile = new Commerce(null, "Shop", Commerce.Category.OTHER, "st", 1, "city", "prov", "pc", "09:00", "18:00");
        svc.createUser(userToCreate, null, commerceProfile);
        final Optional<User> maybe = svc.findByEmail("c@d.com");
        assertTrue(maybe.isPresent());
        assertEquals(User.Role.COMMERCE, maybe.get().getRole());
        assertEquals("123", maybe.get().getPhone());
    }

    @Test
    public void changePassword_success_updatesPassword() {
        // 1. Setup
        final InMemoryUserDao dao = new InMemoryUserDao();
        final UserServiceImpl svc = new UserServiceImpl(dao, new InMemoryClientDao(), new InMemoryCommerceDao(),
                new NoOpPasswordEncoder(), new NoOpEmailVerificationTokenService(), new StubImageService());
        final User created = svc.createUser(new User(null, "u@u.com", "secret1", "User", null, null, false), null,
                null);

        // 2. Ejercicio
        final ChangePasswordResult result = svc.changePassword(created.getId(), "secret1", "secret2xx");

        // 3. Asserts
        assertTrue(result.isSuccess());
        assertEquals("secret2xx", dao.findById(created.getId()).orElseThrow().getPassword());
    }

    @Test
    public void changePassword_wrongCurrent_returnsIncorrect() {
        // 1. Setup
        final InMemoryUserDao dao = new InMemoryUserDao();
        final UserServiceImpl svc = new UserServiceImpl(dao, new InMemoryClientDao(), new InMemoryCommerceDao(),
                new NoOpPasswordEncoder(), new NoOpEmailVerificationTokenService(), new StubImageService());
        final User created = svc.createUser(new User(null, "v@v.com", "good", "V", null, null, false), null, null);

        // 2. Ejercicio
        final ChangePasswordResult result = svc.changePassword(created.getId(), "bad", "newpassxx");

        // 3. Asserts
        assertEquals(ChangePasswordResult.Status.CURRENT_PASSWORD_INCORRECT, result.getStatus());
        assertEquals("good", dao.findById(created.getId()).orElseThrow().getPassword());
    }

    @Test
    public void changePassword_unknownUser_throws() {
        // 1. Setup
        final InMemoryUserDao dao = new InMemoryUserDao();
        final UserServiceImpl svc = new UserServiceImpl(dao, new InMemoryClientDao(), new InMemoryCommerceDao(),
                new NoOpPasswordEncoder(), new NoOpEmailVerificationTokenService(), new StubImageService());

        // 2. Ejercicio / 3. Asserts
        assertThrows(NoSuchElementException.class, () -> svc.changePassword(999L, "a", "bxxxxx"));
    }

    @Test
    public void updateProfilePhoto_success_linksImageId() {
        // 1. Setup
        final InMemoryUserDao dao = new InMemoryUserDao();
        final UserServiceImpl svc = new UserServiceImpl(dao, new InMemoryClientDao(), new InMemoryCommerceDao(),
                new NoOpPasswordEncoder(), new NoOpEmailVerificationTokenService(), new StubImageService());
        final User created = svc.createUser(new User(null, "photo@x.com", "p", "P", null, null, false), null, null);

        // 2. Ejercicio
        svc.updateProfilePhoto(created.getId(), new byte[] { 1, 2 }, "image/png");

        // 3. Asserts
        assertEquals(Long.valueOf(100L), dao.findById(created.getId()).orElseThrow().getProfileImageId());
    }

    @Test
    public void updateProfilePhoto_invalidContentType_throws() {
        // 1. Setup
        final InMemoryUserDao dao = new InMemoryUserDao();
        final UserServiceImpl svc = new UserServiceImpl(dao, new InMemoryClientDao(), new InMemoryCommerceDao(),
                new NoOpPasswordEncoder(), new NoOpEmailVerificationTokenService(), new StubImageService());
        final User created = svc.createUser(new User(null, "bad@x.com", "p", "B", null, null, false), null, null);

        // 2. Ejercicio / 3. Asserts
        assertThrows(IllegalArgumentException.class,
                () -> svc.updateProfilePhoto(created.getId(), new byte[] { 1 }, "application/pdf"));
    }

    @Test
    public void updatePreferredLocale_success_persistsTag() {
        // 1. Setup
        final InMemoryUserDao dao = new InMemoryUserDao();
        final UserServiceImpl svc = new UserServiceImpl(dao, new InMemoryClientDao(), new InMemoryCommerceDao(),
                new NoOpPasswordEncoder(), new NoOpEmailVerificationTokenService(), new StubImageService());
        final User created = svc.createUser(new User(null, "loc@x.com", "p", "L", null, null, false), null, null);

        // 2. Ejercicio
        svc.updatePreferredLocale(created.getId(), Locale.ENGLISH);

        // 3. Asserts
        assertEquals("en", dao.findById(created.getId()).orElseThrow().getLocale().getLanguage());
    }

    @Test
    public void updatePreferredLocale_unsupported_throws() {
        // 1. Setup
        final InMemoryUserDao dao = new InMemoryUserDao();
        final UserServiceImpl svc = new UserServiceImpl(dao, new InMemoryClientDao(), new InMemoryCommerceDao(),
                new NoOpPasswordEncoder(), new NoOpEmailVerificationTokenService(), new StubImageService());
        final User created = svc.createUser(new User(null, "loc2@x.com", "p", "L", null, null, false), null, null);

        // 2. Ejercicio / 3. Asserts
        assertThrows(IllegalArgumentException.class,
                () -> svc.updatePreferredLocale(created.getId(), Locale.FRANCE));
    }

}
