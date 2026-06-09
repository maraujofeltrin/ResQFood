package ar.edu.itba.paw.services.user;

import ar.edu.itba.paw.models.image.ProfileImageException;
import ar.edu.itba.paw.models.notification.NotificationType;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.commerce.CommerceService;
import ar.edu.itba.paw.services.notification.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private CommerceService commerceService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ProfileServiceImpl profileService;

    @Test
    void testGetSettingsOverviewWhenClientExistsReturnsMappedOverview() {
        // 1. Setup
        when(userService.findById(1L)).thenReturn(Optional.of(
                new User(1L, "a@b.com", "hash", "Nombre", "+99", User.Role.CLIENT, true,
                        Locale.forLanguageTag("en"))));
        final Map<NotificationType, Boolean> prefs = Map.of(
                NotificationType.RESERVATION_CODE_CLIENT, true);
        when(notificationService.getClientMailPreferences(1L)).thenReturn(prefs);

        // 2. Ejercicio
        final ProfileSettingsOverview overview = profileService.getSettingsOverview(1L);

        // 3. Asserts
        assertEquals("Nombre", overview.getFullName());
        assertEquals("+99", overview.getPhone());
        assertEquals("a@b.com", overview.getEmail());
        assertEquals("profile-avatar-placeholder.svg", overview.getProfileImageFileName());
        assertNull(overview.getProfileImageId());
        assertNull(overview.getCommerce());
        assertEquals("en", overview.getSelectedLanguageCode());
        assertEquals(2, overview.getLanguageCodes().size());
        assertTrue(overview.getLanguageCodes().contains("en"));
        assertTrue(overview.getLanguageCodes().contains("es"));
        assertEquals(1, overview.getMailPreferences().size());
        assertTrue(overview.getMailPreferences().get(NotificationType.RESERVATION_CODE_CLIENT));
    }

    @Test
    void testGetSettingsOverviewWhenCommerceWithNullPhoneReturnsEmptyPhoneAndCommerceSection() {
        // 1. Setup
        when(userService.findById(2L)).thenReturn(Optional.of(
                new User(2L, "c@d.com", "h", "Solo", null, User.Role.COMMERCE, false,
                        Locale.forLanguageTag("es"))));
        when(commerceService.findByUserId(2L)).thenReturn(Optional.of(
                new Commerce(2L, "El Almacén", Commerce.Category.BAKERY, "Rivadavia", 100, Municipality.MORON, "BA",
                        "1708", "08:30", "20:00")));

        // 2. Ejercicio
        final ProfileSettingsOverview overview = profileService.getSettingsOverview(2L);

        // 3. Asserts
        assertEquals("", overview.getPhone());
        assertEquals("es", overview.getSelectedLanguageCode());
        assertEquals("El Almacén", overview.getFullName());
        assertNotNull(overview.getCommerce());
        assertEquals("El Almacén", overview.getCommerce().getCommercialName());
        assertEquals("BAKERY", overview.getCommerce().getCategory());
        assertEquals("Rivadavia", overview.getCommerce().getStreet());
    }

    @Test
    void testGetSettingsOverviewWhenLocaleNotEnglishReturnsSpanishSelectedCode() {
        // 1. Setup
        when(userService.findById(3L)).thenReturn(Optional.of(
                new User(3L, "e@f.com", "h", "Fr", null, User.Role.CLIENT, true, Locale.FRANCE)));
        when(notificationService.getClientMailPreferences(3L)).thenReturn(Map.of());

        // 2. Ejercicio
        final ProfileSettingsOverview overview = profileService.getSettingsOverview(3L);

        // 3. Asserts
        assertEquals("es", overview.getSelectedLanguageCode());
    }

    @Test
    void testGetSettingsOverviewWhenProfileImageIdPresentReturnsIdInOverview() {
        // 1. Setup
        when(userService.findById(4L)).thenReturn(Optional.of(
                new User(4L, "img@test.com", "h", "Con foto", null, User.Role.CLIENT, true,
                        Locale.forLanguageTag("es"), 99L)));
        when(notificationService.getClientMailPreferences(4L)).thenReturn(Map.of());

        // 2. Ejercicio
        final ProfileSettingsOverview overview = profileService.getSettingsOverview(4L);

        // 3. Asserts
        assertEquals(Long.valueOf(99L), overview.getProfileImageId());
    }

    @Test
    void testGetSettingsOverviewWhenUserMissingThrowsNoSuchElementException() {
        // 1. Setup
        when(userService.findById(99L)).thenReturn(Optional.empty());

        // 2. Ejercicio
        final NoSuchElementException thrown = assertThrows(NoSuchElementException.class,
                () -> profileService.getSettingsOverview(99L));

        // 3. Asserts
        assertTrue(thrown.getMessage().contains("99"));
    }

    @Test
    void testUpdateProfileAccountWhenCommerceUpdateFailsThrowsProfileAccountUpdateExceptionWithCommerceKind() {
        // 1. Setup
        doThrow(new NoSuchElementException("Commerce not found for user: 7")).when(commerceService)
                .updateProfileFields(eq(7L), eq(Commerce.Category.BAKERY), eq("Calle"), eq(1), eq(Municipality.AVELLANEDA), eq("P"),
                        eq("pc"), eq("09:00"), eq("18:00"));

        // 2. Ejercicio
        final ProfileAccountUpdateException ex = assertThrows(ProfileAccountUpdateException.class,
                () -> profileService.updateProfileAccount(7L, User.Role.COMMERCE, "BAKERY", "Calle", "1", Municipality.AVELLANEDA, "P",
                        "pc", "09:00", "18:00", null, null));

        // 3. Asserts
        assertEquals(ProfileAccountUpdateException.Kind.COMMERCE, ex.getKind());
    }

    @Test
    void testGetSettingsOverviewWhenCommerceMissingUsesUserNameAsDisplayName() {
        // 1. Setup
        when(userService.findById(8L)).thenReturn(Optional.of(
                new User(8L, "shop@test.com", "h", "Nombre Usuario", null, User.Role.COMMERCE, false,
                        Locale.forLanguageTag("es"))));
        when(commerceService.findByUserId(8L)).thenReturn(Optional.empty());

        // 2. Ejercicio
        final ProfileSettingsOverview overview = profileService.getSettingsOverview(8L);

        // 3. Asserts
        assertEquals("Nombre Usuario", overview.getFullName());
        assertNull(overview.getCommerce());
    }

    @Test
    void testUpdateProfileAccountWhenInvalidCategoryThrowsProfileAccountUpdateExceptionWithCommerceKind() {
        // 1. Setup

        // 2. Ejercicio
        final ProfileAccountUpdateException ex = assertThrows(ProfileAccountUpdateException.class,
                () -> profileService.updateProfileAccount(7L, User.Role.COMMERCE, "NOT_A_CATEGORY", "Calle", "1",
                        Municipality.AVELLANEDA, "P", "pc", "09:00", "18:00", null, null));

        // 3. Asserts
        assertEquals(ProfileAccountUpdateException.Kind.COMMERCE, ex.getKind());
    }

    @Test
    void testUpdateProfileAccountWhenInvalidStreetNumberThrowsProfileAccountUpdateExceptionWithCommerceKind() {
        // 1. Setup

        // 2. Ejercicio
        final ProfileAccountUpdateException ex = assertThrows(ProfileAccountUpdateException.class,
                () -> profileService.updateProfileAccount(7L, User.Role.COMMERCE, "BAKERY", "Calle", "abc",
                        Municipality.AVELLANEDA, "P", "pc", "09:00", "18:00", null, null));

        // 3. Asserts
        assertEquals(ProfileAccountUpdateException.Kind.COMMERCE, ex.getKind());
    }

    @Test
    void testUpdateProfileAccountWhenPhotoUpdateFailsThrowsProfileAccountUpdateExceptionWithPhotoKind() {
        // 1. Setup
        final byte[] photo = new byte[] { 1, 2, 3 };
        doThrow(new ProfileImageException(ProfileImageException.Reason.INVALID_TYPE)).when(userService)
                .updateProfilePhoto(eq(7L), any(), eq("image/png"));

        // 2. Ejercicio
        final ProfileAccountUpdateException ex = assertThrows(ProfileAccountUpdateException.class,
                () -> profileService.updateProfileAccount(7L, User.Role.CLIENT, null, null, null, null, null,
                        null, null, null, photo, "image/png"));

        // 3. Asserts
        assertEquals(ProfileAccountUpdateException.Kind.PHOTO, ex.getKind());
    }
}
