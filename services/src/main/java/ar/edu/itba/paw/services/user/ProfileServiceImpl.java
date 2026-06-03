package ar.edu.itba.paw.services.user;

import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.commerce.CommerceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class ProfileServiceImpl implements ProfileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileServiceImpl.class);

    private static final String PLACEHOLDER_PROFILE_IMAGE = "profile-avatar-placeholder.svg";

    private final UserService userService;
    private final CommerceService commerceService;
    private final ClientService clientService;

    @Autowired
    public ProfileServiceImpl(final UserService userService, final CommerceService commerceService, final ClientService clientService) {
        this.userService = userService;
        this.commerceService = commerceService;
        this.clientService = clientService;
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
        Boolean notificationsPref = null;
        String displayName = user.getName();
        if (user.getRole() == User.Role.COMMERCE) {
            commerceSection = commerceService.findByUserId(userId)
                    .map(ProfileServiceImpl::toProfileCommerceSection)
                    .orElse(null);
            if (commerceSection != null) {
                displayName = commerceSection.getCommercialName();
            }
        } else if (user.getRole() == User.Role.CLIENT) {
            notificationsPref = clientService.findByUserId(userId)
                    .map(Client::getNotificationsVisibilityPreferences)
                    .orElse(null);
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
                notificationsPref);
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
            final String profilePhotoContentType) {
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
            } catch (final IllegalArgumentException | NoSuchElementException e) {
                LOGGER.debug("Commerce profile update rejected userId={}", userId, e);
                throw new ProfileAccountUpdateException(ProfileAccountUpdateException.Kind.COMMERCE, e);
            }
        }
        if (profilePhoto != null && profilePhoto.length > 0) {
            try {
                userService.updateProfilePhoto(userId, profilePhoto, profilePhotoContentType);
            } catch (final IllegalArgumentException | NoSuchElementException e) {
                LOGGER.debug("Profile photo update rejected userId={}", userId, e);
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

    @Transactional
    @Override
    public void updateNotificationsPreference(long userId, boolean wantsNotifications) {
        final Client client = clientService.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("Client not found: " + userId));
        client.setNotificationsVisibilityPreferences(wantsNotifications);
        clientService.update(client);
    }
}
