package ar.edu.itba.paw.services.user;

import ar.edu.itba.paw.models.user.User;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileServiceImplTest {

    static final class StubUserService implements UserService {
        private final Map<Long, User> byId = new ConcurrentHashMap<>();

        void put(final User user) {
            byId.put(user.getId(), user);
        }

        @Override
        public User createUser(final User user, final ar.edu.itba.paw.models.user.Client clientProfile,
                final ar.edu.itba.paw.models.user.Commerce commerceProfile) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<User> findByEmail(final String email) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<User> findById(final Long id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public void updatePassword(final Long userId, final String encodedPassword) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ChangePasswordResult changePassword(final long userId, final String currentPassword,
                final String newPassword) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void markVerified(final Long userId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public RegisterResult tryRegister(final User user, final ar.edu.itba.paw.models.user.Client clientProfile,
                final ar.edu.itba.paw.models.user.Commerce commerceProfile, final String appBaseUrl) {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    void getSettingsOverview_mapsPersistedUser() {
        // 1. Setup
        final StubUserService userService = new StubUserService();
        userService.put(new User(1L, "a@b.com", "hash", "Nombre", "+99", User.Role.CLIENT, true,
                Locale.forLanguageTag("en")));
        final ProfileServiceImpl profileService = new ProfileServiceImpl(userService);

        // 2. Ejercicio
        final ProfileSettingsOverview overview = profileService.getSettingsOverview(1L);

        // 3. Asserts
        assertEquals("Nombre", overview.getFullName());
        assertEquals("+99", overview.getPhone());
        assertEquals("a@b.com", overview.getEmail());
        assertEquals("profile-avatar-placeholder.svg", overview.getProfileImageFileName());
        assertEquals("en", overview.getSelectedLanguageCode());
        assertEquals(2, overview.getLanguageCodes().size());
        assertTrue(overview.getLanguageCodes().contains("en"));
        assertTrue(overview.getLanguageCodes().contains("es"));
    }

    @Test
    void getSettingsOverview_nullPhone_mapsToEmptyString() {
        // 1. Setup
        final StubUserService userService = new StubUserService();
        userService.put(new User(2L, "c@d.com", "h", "Solo", null, User.Role.COMMERCE, false,
                Locale.forLanguageTag("es")));
        final ProfileServiceImpl profileService = new ProfileServiceImpl(userService);

        // 2. Ejercicio
        final ProfileSettingsOverview overview = profileService.getSettingsOverview(2L);

        // 3. Asserts
        assertEquals("", overview.getPhone());
        assertEquals("es", overview.getSelectedLanguageCode());
    }

    @Test
    void getSettingsOverview_nonEnglishLocale_mapsSelectedLanguageToEs() {
        // 1. Setup
        final StubUserService userService = new StubUserService();
        userService.put(new User(3L, "e@f.com", "h", "Fr", null, User.Role.CLIENT, true, Locale.FRANCE));
        final ProfileServiceImpl profileService = new ProfileServiceImpl(userService);

        // 2. Ejercicio
        final ProfileSettingsOverview overview = profileService.getSettingsOverview(3L);

        // 3. Asserts
        assertEquals("es", overview.getSelectedLanguageCode());
    }

    @Test
    void getSettingsOverview_userMissing_throws() {
        // 1. Setup
        final ProfileServiceImpl profileService = new ProfileServiceImpl(new StubUserService());

        // 2. Ejercicio / 3. Asserts
        assertThrows(NoSuchElementException.class, () -> profileService.getSettingsOverview(99L));
    }
}
