package ar.edu.itba.paw.services.user;

import ar.edu.itba.paw.models.notification.NotificationType;
import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.persistence.ClientNotificationPreferenceDao;
import ar.edu.itba.paw.persistence.ClientDao;
import ar.edu.itba.paw.persistence.CommerceDao;
import ar.edu.itba.paw.persistence.UserDao;
import ar.edu.itba.paw.services.image.ImageService;
import ar.edu.itba.paw.services.security.EmailVerificationTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private static final long MAX_PROFILE_IMAGE_BYTES = 5L * 1024L * 1024L;
    private static final Set<String> ALLOWED_PROFILE_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif");

    private static final List<NotificationType> CLIENT_MAIL_TYPES = List.of(
            NotificationType.RESERVATION_CODE_CLIENT,
            NotificationType.AUCTION_WINNER_CLIENT,
            NotificationType.RESERVATION_REJECTED_CLIENT,
            NotificationType.AUCTION_OUTBID_CLIENT);

    private final UserDao userDao;
    private final ClientDao clientDao;
    private final CommerceDao commerceDao;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationTokenService emailVerificationTokenService;
    private final ImageService imageService;
    private final ClientNotificationPreferenceDao clientNotificationPreferenceDao;

    @Autowired
    public UserServiceImpl(final UserDao userDao, final ClientDao clientDao, final CommerceDao commerceDao,
            final PasswordEncoder passwordEncoder,
            final EmailVerificationTokenService emailVerificationTokenService,
            final ImageService imageService,
            final ClientNotificationPreferenceDao clientNotificationPreferenceDao) {
        this.userDao = userDao;
        this.clientDao = clientDao;
        this.commerceDao = commerceDao;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationTokenService = emailVerificationTokenService;
        this.imageService = imageService;
        this.clientNotificationPreferenceDao = clientNotificationPreferenceDao;
    }

    @Transactional
    @Override
    public User createUser(final User user, final Client clientProfile, final Commerce commerceProfile) {
        final User createdUser = userDao.createUser(
                user.getEmail(),
                passwordEncoder.encode(user.getPassword()),
                user.getName(),
                user.getPhone(),
                user.getRole(),
                user.getLocale());

        persistProfileByRole(createdUser, clientProfile, commerceProfile);
        LOGGER.info("User created: userId={}, role={}", createdUser.getId(), createdUser.getRole());
        return createdUser;
    }



    private void syncClientMailPreferencesFromRegister(final long userId,
            final Boolean notificationsVisibilityPreferences) {
        if (!Boolean.FALSE.equals(notificationsVisibilityPreferences)) {
            return;
        }
        final Map<NotificationType, Boolean> disabled = new EnumMap<>(NotificationType.class);
        for (final NotificationType type : CLIENT_MAIL_TYPES) {
            disabled.put(type, false);
        }
        for (final Map.Entry<NotificationType, Boolean> entry : disabled.entrySet()) {
            clientNotificationPreferenceDao.upsert(userId, entry.getKey(), entry.getValue());
        }
    }

    private void persistProfileByRole(final User user, final Client clientProfile, final Commerce commerceProfile) {
        if (user.getRole() == User.Role.CLIENT) {
            if (clientProfile == null) {
                throw new IllegalArgumentException("Client profile data is required for CLIENT users");
            }
            final Optional<Client> existingClient = clientDao.findByUserId(user.getId());
            if (existingClient.isPresent()) {
                final Client client = existingClient.get();
                client.setName(clientProfile.getName());
                client.setLastName(clientProfile.getLastName());
                client.setNotificationsVisibilityPreferences(clientProfile.getNotificationsVisibilityPreferences());
                clientDao.update(client);
                syncClientMailPreferencesFromRegister(user.getId(),
                        clientProfile.getNotificationsVisibilityPreferences());
            } else {
                clientDao.createClient(
                        user.getId(),
                        clientProfile.getName(),
                        clientProfile.getLastName(),
                        clientProfile.getNotificationsVisibilityPreferences());
                syncClientMailPreferencesFromRegister(user.getId(),
                        clientProfile.getNotificationsVisibilityPreferences());
            }
            return;
        }

        if (user.getRole() == User.Role.COMMERCE) {
            if (commerceProfile == null) {
                throw new IllegalArgumentException("Commerce profile data is required for COMMERCE users");
            }
            if (!Commerce.PROVINCE_BUENOS_AIRES.equalsIgnoreCase(commerceProfile.getProvince())) {
                throw new IllegalArgumentException("Commerce province must be " + Commerce.PROVINCE_BUENOS_AIRES);
            }
            final Optional<Commerce> existingCommerce = commerceDao.findByUserId(user.getId());
            if (existingCommerce.isPresent()) {
                final Commerce commerce = existingCommerce.get();
                commerce.setCommercialName(commerceProfile.getCommercialName());
                commerce.setCategory(commerceProfile.getCategory());
                commerce.setStreet(commerceProfile.getStreet());
                commerce.setStreetNumber(commerceProfile.getStreetNumber());
                commerce.setCity(commerceProfile.getCity());
                commerce.setProvince(commerceProfile.getProvince());
                commerce.setPostalCode(commerceProfile.getPostalCode());
                commerce.setOpeningTime(commerceProfile.getOpeningTime());
                commerce.setClosingTime(commerceProfile.getClosingTime());
                commerceDao.update(commerce);
            } else {
                commerceDao.createCommerce(
                        user.getId(),
                        commerceProfile.getCommercialName(),
                        commerceProfile.getCategory(),
                        commerceProfile.getStreet(),
                        commerceProfile.getStreetNumber(),
                        commerceProfile.getCity(),
                        commerceProfile.getProvince(),
                        commerceProfile.getPostalCode(),
                        commerceProfile.getOpeningTime(),
                        commerceProfile.getClosingTime());
            }
        }
    }

    @Override
    public Optional<User> findByEmail(final String email) {
        return userDao.findByEmail(email);
    }

    @Override
    public Optional<User> findById(final Long id) {
        return userDao.findById(id);
    }

    @Transactional
    @Override
    public void updatePassword(final Long userId, final String encodedPassword) {
        userDao.updatePassword(userId, encodedPassword);
    }

    @Transactional
    @Override
    public ChangePasswordResult changePassword(final long userId, final String currentPassword, final String newPassword) {
        final User user = userDao.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            LOGGER.warn("Password change rejected: incorrect current password for userId={}", userId);
            return ChangePasswordResult.currentPasswordIncorrect();
        }
        userDao.updatePassword(userId, passwordEncoder.encode(newPassword));
        LOGGER.info("Password changed for userId={}", userId);
        return ChangePasswordResult.success();
    }

    @Transactional
    @Override
    public void markVerified(final Long userId) {
        userDao.markVerified(userId);
    }

    @Transactional
    @Override
    public RegisterResult tryRegister(final User user, final Client clientProfile, final Commerce commerceProfile,
            final String appBaseUrl) {
        LOGGER.debug("Registration initiated for email={}", user.getEmail());
        final Optional<User> existingUser = findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            LOGGER.warn("Registration rejected: duplicate email={}", user.getEmail());
            return RegisterResult.duplicateEmail();
        }
        final User created = createUser(user, clientProfile, commerceProfile);
        emailVerificationTokenService.sendVerificationMail(created.getId(), created.getEmail(), appBaseUrl,
                created.getLocale());
        return RegisterResult.createdPendingVerification(created);
    }

    @Transactional
    @Override
    public void updateProfilePhoto(final long userId, final byte[] data, final String contentType) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Image data cannot be null or empty");
        }
        if (data.length > MAX_PROFILE_IMAGE_BYTES) {
            throw new IllegalArgumentException("Image exceeds maximum size");
        }
        if (contentType == null || contentType.isEmpty() || !ALLOWED_PROFILE_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Invalid or unsupported image content type");
        }
        userDao.findById(userId).orElseThrow(() -> {
            LOGGER.warn("updateProfilePhoto: user not found userId={}", userId);
            return new NoSuchElementException("User not found: " + userId);
        });
        final long imageId = imageService.saveImage(data, contentType).getId();
        userDao.updateProfileImage(userId, imageId);
    }

    @Transactional
    @Override
    public void updatePreferredLocale(final long userId, final Locale locale) {
        SupportedUserLocales.assertSupported(locale);
        final String lang = locale.getLanguage().toLowerCase(Locale.ROOT);
        userDao.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        userDao.updateLocale(userId, lang);
        LOGGER.debug("Updated preferred locale to {} for userId={}", lang, userId);
    }
}
