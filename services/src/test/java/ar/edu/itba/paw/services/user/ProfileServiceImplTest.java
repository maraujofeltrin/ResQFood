package ar.edu.itba.paw.services.user;

import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.commerce.CommerceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private CommerceService commerceService;

    @InjectMocks
    private ProfileServiceImpl profileService;

    @Test
    void testGetSettingsOverviewWhenClientExistsReturnsMappedOverview() {
        // 1. Setup
        when(userService.findById(1L)).thenReturn(Optional.of(
                new User(1L, "a@b.com", "hash", "Nombre", "+99", User.Role.CLIENT, true,
                        Locale.forLanguageTag("en"), null)));

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
    }

    @Test
    void testGetSettingsOverviewWhenCommerceWithNullPhoneReturnsEmptyPhoneAndCommerceSection() {
        // 1. Setup
        when(userService.findById(2L)).thenReturn(Optional.of(
                new User(2L, "c@d.com", "h", "Solo", null, User.Role.COMMERCE, false,
                        Locale.forLanguageTag("es"), null)));
        when(commerceService.findByUserId(2L)).thenReturn(Optional.of(
                new Commerce(2L, "El Almacén", Commerce.Category.BAKERY, "Rivadavia", 100, "Morón", "BA",
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
                new User(3L, "e@f.com", "h", "Fr", null, User.Role.CLIENT, true, Locale.FRANCE, null)));

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
    void testUpdateProfileAccountWhenCommerceRoleWithoutPhotoUpdatesCommerceFieldsOnly() {
        // 1. Setup
        final AtomicLong capturedUserId = new AtomicLong();
        final AtomicReference<Commerce.Category> capturedCategory = new AtomicReference<>();
        final AtomicReference<String> capturedStreet = new AtomicReference<>();
        final AtomicReference<Integer> capturedStreetNumber = new AtomicReference<>();
        final AtomicReference<String> capturedCity = new AtomicReference<>();
        final AtomicReference<String> capturedProvince = new AtomicReference<>();
        final AtomicReference<String> capturedPostal = new AtomicReference<>();
        final AtomicReference<String> capturedOpening = new AtomicReference<>();
        final AtomicReference<String> capturedClosing = new AtomicReference<>();
        doAnswer(invocation -> {
            capturedUserId.set(invocation.getArgument(0));
            capturedCategory.set(invocation.getArgument(1));
            capturedStreet.set(invocation.getArgument(2));
            capturedStreetNumber.set(invocation.getArgument(3));
            capturedCity.set(invocation.getArgument(4));
            capturedProvince.set(invocation.getArgument(5));
            capturedPostal.set(invocation.getArgument(6));
            capturedOpening.set(invocation.getArgument(7));
            capturedClosing.set(invocation.getArgument(8));
            return null;
        }).when(commerceService).updateProfileFields(anyLong(), any(), anyString(), any(), anyString(), anyString(),
                anyString(), anyString(), anyString());
        lenient().doThrow(new AssertionError("updateProfilePhoto no debe invocarse")).when(userService)
                .updateProfilePhoto(anyLong(), any(), any());

        // 2. Ejercicio
        profileService.updateProfileAccount(5L, User.Role.COMMERCE, "RESTAURANT", "Av. Siempre Viva", "42", "Ituzaingó",
                "BA", "1714", "09:00", "18:00", null, null);

        // 3. Asserts
        assertEquals(5L, capturedUserId.get());
        assertEquals(Commerce.Category.RESTAURANT, capturedCategory.get());
        assertEquals("Av. Siempre Viva", capturedStreet.get());
        assertEquals(Integer.valueOf(42), capturedStreetNumber.get());
        assertEquals("Ituzaingó", capturedCity.get());
        assertEquals("BA", capturedProvince.get());
        assertEquals("1714", capturedPostal.get());
        assertEquals("09:00", capturedOpening.get());
        assertEquals("18:00", capturedClosing.get());
    }

    @Test
    void testUpdateProfileAccountWhenClientWithPhotoDelegatesPhotoToUserService() {
        // 1. Setup
        final byte[] bytes = { 1, 2, 3 };
        final AtomicLong capturedUserId = new AtomicLong();
        final AtomicReference<byte[]> capturedData = new AtomicReference<>();
        final AtomicReference<String> capturedContentType = new AtomicReference<>();
        doAnswer(invocation -> {
            capturedUserId.set(invocation.getArgument(0));
            capturedData.set(invocation.getArgument(1));
            capturedContentType.set(invocation.getArgument(2));
            return null;
        }).when(userService).updateProfilePhoto(anyLong(), any(), any());
        lenient().doThrow(new AssertionError("updateProfileFields no debe invocarse")).when(commerceService)
                .updateProfileFields(anyLong(), any(), anyString(), any(), anyString(), anyString(), anyString(),
                        anyString(), anyString());

        // 2. Ejercicio
        profileService.updateProfileAccount(6L, User.Role.CLIENT, null, null, null, null, null, null, null, null, bytes,
                "image/png");

        // 3. Asserts
        assertEquals(6L, capturedUserId.get());
        assertArrayEquals(bytes, capturedData.get());
        assertEquals("image/png", capturedContentType.get());
    }

    @Test
    void testUpdateProfileAccountWhenCommerceUpdateFailsThrowsProfileAccountUpdateExceptionWithCommerceKind() {
        // 1. Setup
        doThrow(new NoSuchElementException("Commerce not found for user: 7")).when(commerceService)
                .updateProfileFields(eq(7L), eq(Commerce.Category.OTHER), eq("Calle"), eq(1), eq("Ciudad"), eq("P"),
                        eq("pc"), eq("09:00"), eq("18:00"));

        // 2. Ejercicio
        final ProfileAccountUpdateException ex = assertThrows(ProfileAccountUpdateException.class,
                () -> profileService.updateProfileAccount(7L, User.Role.COMMERCE, "OTHER", "Calle", "1", "Ciudad", "P",
                        "pc", "09:00", "18:00", null, null));

        // 3. Asserts
        assertEquals(ProfileAccountUpdateException.Kind.COMMERCE, ex.getKind());
    }
}
