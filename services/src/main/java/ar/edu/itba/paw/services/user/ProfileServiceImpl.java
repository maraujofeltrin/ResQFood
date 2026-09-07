package ar.edu.itba.paw.services.user;

import ar.edu.itba.paw.models.notification.NotificationType;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.CommerceProfileException;
import ar.edu.itba.paw.models.image.ProfileImageException;
import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.commerce.CommerceService;
import ar.edu.itba.paw.services.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class ProfileServiceImpl implements ProfileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileServiceImpl.class);

    private static final String PLACEHOLDER_PROFILE_IMAGE = "profile-avatar-placeholder.svg";

    private final UserService userService;
    private final CommerceService commerceService;
    private final NotificationService notificationService;

    @Autowired
    public ProfileServiceImpl(final UserService userService, final CommerceService commerceService,
                              final NotificationService notificationService) {
        this.userService = userService;
        this.commerceService = commerceService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    @Override
    public ProfileSettingsOverview getSettingsOverview(final long userId) {
        final User user = userService.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        final String phone = user.getPhone() == null ? "" : user.getPhone();
        final String selectedLang = SupportedUserLocales.CODE_EN.equalsIgnoreCase(user.getLocale().getLanguage())
                ? SupportedUserLocales.CODE_EN
                : SupportedUserLocales.CODE_ES;
        ProfileCommerceSection commerceSection = null;
        Map<NotificationType, Boolean> mailPreferences = Map.of();
        String displayName = user.getName();
        if (user.getRole() == User.Role.COMMERCE) {
            commerceSection = commerceService.findByUserId(userId)
                    .map(ProfileServiceImpl::toProfileCommerceSection)
                    .orElse(null);
            if (commerceSection != null) {
                displayName = commerceSection.getCommercialName();
            }
        } else if (user.getRole() == User.Role.CLIENT) {
            mailPreferences = notificationService.getClientMailPreferences(userId);
        }
        return new ProfileSettingsOverview(
                displayName,
                phone,
                user.getEmail(),
                user.getProfileImageId(),
                PLACEHOLDER_PROFILE_IMAGE,
                selectedLang,
                SupportedUserLocales.languageCodes(),
                commerceSection,
                mailPreferences);
    }

    @Transactional
    @Override
    public void updateProfileAccount(
            final long userId,
            final User.Role role,
            final String category,
            final String street,
            final String streetNumber,
            final Municipality city,
            final String province,
            final String postalCode,
            final String openingTime,
            final String closingTime,
            final byte[] profilePhoto,
            final String profilePhotoContentType,
            final boolean removePhoto) {
        if (role == User.Role.COMMERCE) {
            try {
                final Commerce.Category cat = Commerce.Category.valueOf(category.trim());
                final Integer streetNum = parseStreetNumberOrNull(streetNumber);
                commerceService.updateProfileFields(
                        userId,
                        cat,
                        street != null ? street.trim() : "",
                        streetNum,
                        city,
                        province,
                        postalCode,
                        openingTime != null ? openingTime.trim() : "",
                        closingTime != null ? closingTime.trim() : "");
            } catch (final IllegalArgumentException | CommerceProfileException | NoSuchElementException e) {
                LOGGER.debug("Commerce profile update rejected userId={}", userId, e);
                throw new ProfileAccountUpdateException(ProfileAccountUpdateException.Kind.COMMERCE, e);
            }
        }
        if (profilePhoto != null && profilePhoto.length > 0) {
            try {
                userService.updateProfilePhoto(userId, profilePhoto, profilePhotoContentType);
            } catch (final IllegalArgumentException | ProfileImageException | NoSuchElementException e) {
                LOGGER.debug("Profile photo update rejected userId={}", userId, e);
                throw new ProfileAccountUpdateException(ProfileAccountUpdateException.Kind.PHOTO, e);
            }
        } else if (removePhoto) {
            try {
                userService.removeProfilePhoto(userId);
            } catch (final NoSuchElementException e) {
                LOGGER.debug("Profile photo removal failed: user not found userId={}", userId, e);
                throw new ProfileAccountUpdateException(ProfileAccountUpdateException.Kind.PHOTO, e);
            }
        }
    }

    private static Integer parseStreetNumberOrNull(final String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return Integer.parseInt(raw.trim());
    }

    private static ProfileCommerceSection toProfileCommerceSection(final Commerce c) {
        return new ProfileCommerceSection(
                c.getCommercialName(),
                c.getCategory() == null ? null : c.getCategory().name(),
                c.getStreet(),
                c.getStreetNumber(),
                c.getCity(),
                c.getProvince(),
                c.getPostalCode(),
                c.getOpeningTime(),
                c.getClosingTime());
    }

}
