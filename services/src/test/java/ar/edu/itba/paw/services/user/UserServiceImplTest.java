package ar.edu.itba.paw.services.user;

import ar.edu.itba.paw.models.image.Image;
import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.persistence.ClientDao;
import ar.edu.itba.paw.persistence.CommerceDao;
import ar.edu.itba.paw.persistence.UserDao;
import ar.edu.itba.paw.services.image.ImageService;
import ar.edu.itba.paw.services.security.EmailVerificationTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDao userDao;

    @Mock
    private ClientDao clientDao;

    @Mock
    private CommerceDao commerceDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailVerificationTokenService emailVerificationTokenService;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testCreateUserWhenRoleAndPhoneAreNullReturnsDaoUser() {
        // 1. Setup
        final User toCreate = new User(null, "a@b.com", "pw", "Name", null, null, false);
        final User persisted = new User(1L, "a@b.com", "ENC:pw", "Name", null, null, false);
        when(passwordEncoder.encode("pw")).thenReturn("ENC:pw");
        when(userDao.createUser(eq("a@b.com"), eq("ENC:pw"), eq("Name"), isNull(), isNull(),
                eq(Locale.forLanguageTag("es")))).thenReturn(persisted);

        // 2. Ejercicio
        final User result = userService.createUser(toCreate, null, null);

        // 3. Asserts
        assertEquals(1L, result.getId());
        assertEquals("a@b.com", result.getEmail());
        assertEquals("Name", result.getName());
    }

    @Test
    void testCreateUserWhenCommerceRoleCreatesCommerceProfile() {
        // 1. Setup
        final User toCreate = new User(null, "c@d.com", "pw", "N", "123", User.Role.COMMERCE, false);
        final User persisted = new User(1L, "c@d.com", "ENC:pw", "N", "123", User.Role.COMMERCE, false);
        final Commerce commerceProfile = new Commerce(null, "Shop", Commerce.Category.OTHER, "st", 1, Municipality.AVELLANEDA, Commerce.PROVINCE_BUENOS_AIRES,
                "pc", "09:00", "18:00");
        when(passwordEncoder.encode("pw")).thenReturn("ENC:pw");
        when(userDao.createUser(eq("c@d.com"), eq("ENC:pw"), eq("N"), eq("123"), eq(User.Role.COMMERCE),
                eq(Locale.forLanguageTag("es")))).thenReturn(persisted);
        when(commerceDao.findByUserId(1L)).thenReturn(Optional.empty());
        when(commerceDao.createCommerce(eq(1L), eq("Shop"), eq(Commerce.Category.OTHER), eq("st"), eq(1), eq(Municipality.AVELLANEDA),
                eq(Commerce.PROVINCE_BUENOS_AIRES), eq("pc"), eq("09:00"), eq("18:00")))
                .thenReturn(new Commerce(1L, "Shop", Commerce.Category.OTHER, "st", 1, Municipality.AVELLANEDA, Commerce.PROVINCE_BUENOS_AIRES, "pc", "09:00",
                        "18:00"));

        // 2. Ejercicio
        final User result = userService.createUser(toCreate, null, commerceProfile);

        // 3. Asserts
        assertEquals(User.Role.COMMERCE, result.getRole());
        assertEquals("123", result.getPhone());
    }

    @Test
    void testFindByEmailWhenUserExistsReturnsUser() {
        // 1. Setup
        final User u = new User(2L, "x@y.com", "h", "X", null, null, false);
        when(userDao.findByEmail("x@y.com")).thenReturn(Optional.of(u));

        // 2. Ejercicio
        final Optional<User> found = userService.findByEmail("x@y.com");

        // 3. Asserts
        assertTrue(found.isPresent());
        assertEquals(2L, found.get().getId());
    }

    @Test
    void testFindByIdWhenUserExistsReturnsUser() {
        // 1. Setup
        final User u = new User(3L, "id@test.com", "h", "IdUser", null, null, false);
        when(userDao.findById(3L)).thenReturn(Optional.of(u));

        // 2. Ejercicio
        final Optional<User> found = userService.findById(3L);

        // 3. Asserts
        assertTrue(found.isPresent());
        assertEquals("IdUser", found.get().getName());
    }

    @Test
    void testChangePasswordWhenCurrentPasswordMatchesReturnsSuccess() {
        // 1. Setup
        final User stored = new User(1L, "u@u.com", "ENC:secret1", "User", null, null, false);
        final AtomicReference<String> newEncodedPassword = new AtomicReference<>();
        when(userDao.findById(1L)).thenReturn(Optional.of(stored));
        when(passwordEncoder.matches("secret1", "ENC:secret1")).thenReturn(true);
        when(passwordEncoder.encode("secret2xx")).thenReturn("ENC:secret2xx");
        doAnswer(invocation -> {
            newEncodedPassword.set(invocation.getArgument(1));
            return null;
        }).when(userDao).updatePassword(anyLong(), anyString());

        // 2. Ejercicio
        final ChangePasswordResult result = userService.changePassword(1L, "secret1", "secret2xx");

        // 3. Asserts
        assertTrue(result.isSuccess());
        assertEquals("ENC:secret2xx", newEncodedPassword.get());
    }

    @Test
    void testChangePasswordWhenCurrentPasswordWrongReturnsIncorrect() {
        // 1. Setup
        final User stored = new User(1L, "v@v.com", "ENC:good", "V", null, null, false);
        when(userDao.findById(1L)).thenReturn(Optional.of(stored));
        when(passwordEncoder.matches("bad", "ENC:good")).thenReturn(false);

        // 2. Ejercicio
        final ChangePasswordResult result = userService.changePassword(1L, "bad", "newpassxx");

        // 3. Asserts
        assertEquals(ChangePasswordResult.Status.CURRENT_PASSWORD_INCORRECT, result.getStatus());
    }

    @Test
    void testChangePasswordWhenUserMissingThrowsNoSuchElementException() {
        // 1. Setup
        when(userDao.findById(999L)).thenReturn(Optional.empty());

        // 2. Ejercicio
        final NoSuchElementException thrown = assertThrows(NoSuchElementException.class,
                () -> userService.changePassword(999L, "a", "bxxxxx"));

        // 3. Asserts
        assertTrue(thrown.getMessage().contains("999"));
    }

    @Test
    void testUpdateProfilePhotoWhenValidSavesImageAndUpdatesProfile() {
        // 1. Setup
        final User stored = new User(1L, "photo@x.com", "p", "P", null, null, false);
        final AtomicReference<Long> persistedImageId = new AtomicReference<>();
        when(userDao.findById(1L)).thenReturn(Optional.of(stored));
        when(imageService.saveImage(any(byte[].class), eq("image/png")))
                .thenReturn(new Image(100L, new byte[] { 1, 2 }, "image/png"));
        doAnswer(invocation -> {
            persistedImageId.set(invocation.getArgument(1));
            return null;
        }).when(userDao).updateProfileImage(anyLong(), anyLong());

        // 2. Ejercicio
        userService.updateProfilePhoto(1L, new byte[] { 1, 2 }, "image/png");

        // 3. Asserts
        assertEquals(Long.valueOf(100L), persistedImageId.get());
    }

    @Test
    void testUpdateProfilePhotoWhenInvalidContentTypeThrowsIllegalArgumentException() {
        // 1. Setup
        // sin stub de userDao: falla validación antes de consultar

        // 2. Ejercicio
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> userService.updateProfilePhoto(1L, new byte[] { 1 }, "application/pdf"));

        // 3. Asserts
        assertEquals("Invalid or unsupported image content type", thrown.getMessage());
    }

    @Test
    void testUpdatePreferredLocaleWhenSupportedPersistsLanguageTag() {
        // 1. Setup
        final User stored = new User(1L, "loc@x.com", "p", "L", null, null, false);
        final AtomicReference<String> persistedLang = new AtomicReference<>();
        when(userDao.findById(1L)).thenReturn(Optional.of(stored));
        doAnswer(invocation -> {
            persistedLang.set(invocation.getArgument(1));
            return null;
        }).when(userDao).updateLocale(anyLong(), anyString());

        // 2. Ejercicio
        userService.updatePreferredLocale(1L, Locale.ENGLISH);

        // 3. Asserts
        assertEquals("en", persistedLang.get());
    }

    @Test
    void testUpdatePreferredLocaleWhenUnsupportedThrowsIllegalArgumentException() {
        // 1. Setup

        // 2. Ejercicio
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> userService.updatePreferredLocale(1L, Locale.FRANCE));

        // 3. Asserts
        assertEquals("Unsupported locale", thrown.getMessage());
    }

    @Test
    void testCreateUserWhenClientRoleWithoutProfileThrowsIllegalArgumentException() {
        // 1. Setup
        final User toCreate = new User(null, "cl@required.com", "pw", "N", null, User.Role.CLIENT, false);
        final User persisted =
                new User(1L, "cl@required.com", "ENC:pw", "N", null, User.Role.CLIENT, false);
        when(passwordEncoder.encode("pw")).thenReturn("ENC:pw");
        when(userDao.createUser(eq("cl@required.com"), eq("ENC:pw"), eq("N"), isNull(), eq(User.Role.CLIENT),
                eq(Locale.forLanguageTag("es")))).thenReturn(persisted);

        // 2. Ejercicio
        final IllegalArgumentException thrown =
                assertThrows(IllegalArgumentException.class, () -> userService.createUser(toCreate, null, null));

        // 3. Asserts
        assertEquals("Client profile data is required for CLIENT users", thrown.getMessage());
    }

    @Test
    void testCreateUserWhenCommerceRoleWithoutProfileThrowsIllegalArgumentException() {
        // 1. Setup
        final User toCreate =
                new User(null, "co@required.com", "pw", "N", "111", User.Role.COMMERCE, false);
        final User persisted =
                new User(1L, "co@required.com", "ENC:pw", "N", "111", User.Role.COMMERCE, false);
        when(passwordEncoder.encode("pw")).thenReturn("ENC:pw");
        when(userDao.createUser(eq("co@required.com"), eq("ENC:pw"), eq("N"), eq("111"), eq(User.Role.COMMERCE),
                eq(Locale.forLanguageTag("es")))).thenReturn(persisted);

        // 2. Ejercicio
        final IllegalArgumentException thrown =
                assertThrows(IllegalArgumentException.class, () -> userService.createUser(toCreate, null, null));

        // 3. Asserts
        assertEquals("Commerce profile data is required for COMMERCE users", thrown.getMessage());
    }

    @Test
    void testUpdateProfilePhotoWhenDataNullThrowsIllegalArgumentException() {
        // 1. Setup

        // 2. Ejercicio
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> userService.updateProfilePhoto(1L, null, "image/png"));

        // 3. Asserts
        assertEquals("Image data cannot be null or empty", thrown.getMessage());
    }

    @Test
    void testUpdateProfilePhotoWhenDataEmptyThrowsIllegalArgumentException() {
        // 1. Setup

        // 2. Ejercicio
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> userService.updateProfilePhoto(1L, new byte[0], "image/png"));

        // 3. Asserts
        assertEquals("Image data cannot be null or empty", thrown.getMessage());
    }

    @Test
    void testUpdateProfilePhotoWhenExceedsMaxSizeThrowsIllegalArgumentException() {
        // 1. Setup
        final byte[] huge = new byte[5 * 1024 * 1024 + 1];
        Arrays.fill(huge, (byte) 7);

        // 2. Ejercicio
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> userService.updateProfilePhoto(1L, huge, "image/png"));

        // 3. Asserts
        assertEquals("Image exceeds maximum size", thrown.getMessage());
    }

    @Test
    void testUpdateProfilePhotoWhenUserMissingThrowsNoSuchElementException() {
        // 1. Setup
        when(userDao.findById(404L)).thenReturn(Optional.empty());

        // 2. Ejercicio
        final NoSuchElementException thrown = assertThrows(NoSuchElementException.class,
                () -> userService.updateProfilePhoto(404L, new byte[] { 1 }, "image/png"));

        // 3. Asserts
        assertTrue(thrown.getMessage().contains("404"));
    }

    @Test
    void testTryRegisterWhenEmailAlreadyRegisteredReturnsDuplicateEmail() {
        // 1. Setup
        final User existing = new User(50L, "dup@example.com", "h", "Existing", null, User.Role.CLIENT, false);
        when(userDao.findByEmail("dup@example.com")).thenReturn(Optional.of(existing));
        final User registering =
                new User(null, "dup@example.com", "pw", "New", null, User.Role.CLIENT, false);
        final Client clientProfile = new Client(null, "N", "L", true);

        // 2. Ejercicio
        final RegisterResult result =
                userService.tryRegister(registering, clientProfile, null, "https://app.example");

        // 3. Asserts
        assertEquals(RegisterResult.Outcome.DUPLICATE_EMAIL, result.getOutcome());
        assertTrue(result.getUser().isEmpty());
    }
}
