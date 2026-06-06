package ar.edu.itba.paw.services.user;

import ar.edu.itba.paw.models.image.ProfileImageException;
import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.models.user.UserRegistrationException;
import ar.edu.itba.paw.persistence.ClientDao;
import ar.edu.itba.paw.persistence.CommerceDao;
import ar.edu.itba.paw.persistence.UserDao;
import ar.edu.itba.paw.services.image.ImageService;
import ar.edu.itba.paw.services.security.VerificationTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private static final long MAX_PROFILE_IMAGE_BYTES = 5L * 1024L * 1024L;
    private static final Set<String> ALLOWED_PROFILE_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif");

    private final UserDao userDao;
    private final ClientDao clientDao;
    private final CommerceDao commerceDao;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenService verificationTokenService;
    private final ImageService imageService;

    @Autowired
    public UserServiceImpl(final UserDao userDao, final ClientDao clientDao, final CommerceDao commerceDao,
            final PasswordEncoder passwordEncoder,
            final VerificationTokenService verificationTokenService,
            final ImageService imageService) {
        this.userDao = userDao;
        this.clientDao = clientDao;
        this.commerceDao = commerceDao;
        this.passwordEncoder = passwordEncoder;
        this.verificationTokenService = verificationTokenService;
        this.imageService = imageService;
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



    private void persistProfileByRole(final User user, final Client clientProfile, final Commerce commerceProfile) {
        if (user.getRole() == User.Role.CLIENT) {
            if (clientProfile == null) {
                throw new UserRegistrationException(UserRegistrationException.Reason.MISSING_CLIENT_PROFILE);
            }
            final Optional<Client> existingClient = clientDao.findByUserId(user.getId());
            if (existingClient.isPresent()) {
                final Client client = existingClient.get();
                client.setName(clientProfile.getName());
                client.setLastName(clientProfile.getLastName());
                client.setNotificationsVisibilityPreferences(clientProfile.getNotificationsVisibilityPreferences());
                clientDao.update(client);
            } else {
                clientDao.createClient(
                        user.getId(),
                        clientProfile.getName(),
                        clientProfile.getLastName(),
                        clientProfile.getNotificationsVisibilityPreferences());
            }
            return;
        }

        if (user.getRole() == User.Role.COMMERCE) {
            if (commerceProfile == null) {
                throw new UserRegistrationException(UserRegistrationException.Reason.MISSING_COMMERCE_PROFILE);
            }
            if (!Commerce.PROVINCE_BUENOS_AIRES.equalsIgnoreCase(commerceProfile.getProvince())) {
                throw new UserRegistrationException(UserRegistrationException.Reason.INVALID_PROVINCE);
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

    @Transactional(readOnly = true)
    @Override
    public Optional<User> findByEmail(final String email) {
        return userDao.findByEmail(email);
    }

    @Transactional(readOnly = true)
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
    public RegisterResult tryRegister(final User user, final Client clientProfile, final Commerce commerceProfile) {
        LOGGER.debug("Registration initiated for email={}", user.getEmail());
        final Optional<User> existingUser = findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            LOGGER.warn("Registration rejected: duplicate email={}", user.getEmail());
            return RegisterResult.duplicateEmail();
        }
        final User created = createUser(user, clientProfile, commerceProfile);
        verificationTokenService.sendVerificationMail(created.getId(), created.getEmail(),
                created.getLocale());
        return RegisterResult.createdPendingVerification(created);
    }

    @Transactional
    @Override
    public void updateProfilePhoto(final long userId, final byte[] data, final String contentType) {
        if (data == null || data.length == 0) {
            throw new ProfileImageException(ProfileImageException.Reason.DATA_EMPTY);
        }
        if (data.length > MAX_PROFILE_IMAGE_BYTES) {
            throw new ProfileImageException(ProfileImageException.Reason.SIZE_EXCEEDED);
        }
        if (contentType == null || contentType.isEmpty() || !ALLOWED_PROFILE_IMAGE_TYPES.contains(contentType)) {
            throw new ProfileImageException(ProfileImageException.Reason.INVALID_TYPE);
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
