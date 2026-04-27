package ar.edu.itba.paw.services.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileServiceImplTest {

    private final ProfileServiceImpl profileService = new ProfileServiceImpl();

    @Test
    void getSettingsOverview_returnsPlaceholderSnapshot() {
        // 1. Setup
        // (sin dependencias)

        // 2. Ejercicio
        final ProfileSettingsOverview overview = profileService.getSettingsOverview(42L);

        // 3. Asserts
        assertEquals("Elena Rodriguez", overview.getFullName());
        assertEquals("+34 612 345 678", overview.getPhone());
        assertEquals("elena.r@pantry.org", overview.getEmail());
        assertEquals("profile-avatar-placeholder.svg", overview.getProfileImageFileName());
        assertEquals("es", overview.getSelectedLanguageCode());
        assertEquals(2, overview.getLanguageCodes().size());
        assertTrue(overview.getLanguageCodes().contains("en"));
        assertTrue(overview.getLanguageCodes().contains("es"));
    }
}
