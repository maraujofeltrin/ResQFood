package ar.edu.itba.paw.services.user;

import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.commerce.CommerceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ProfileServiceImpl implements ProfileService {

    private static final String PLACEHOLDER_PROFILE_IMAGE = "profile-avatar-placeholder.svg";
    private static final List<String> LANGUAGE_CODES = List.of("es", "en");

    private final UserService userService;
    private final CommerceService commerceService;

    @Autowired
    public ProfileServiceImpl(final UserService userService, final CommerceService commerceService) {
        this.userService = userService;
        this.commerceService = commerceService;
    }

    @Override
    public ProfileSettingsOverview getSettingsOverview(final long userId) {
        final User user = userService.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        final String phone = user.getPhone() == null ? "" : user.getPhone();
        final String selectedLang = "en".equalsIgnoreCase(user.getLocale().getLanguage()) ? "en" : "es";
        ProfileCommerceSection commerceSection = null;
        String displayName = user.getName();
        if (user.getRole() == User.Role.COMMERCE) {
            commerceSection = commerceService.findByUserId(userId)
                    .map(ProfileServiceImpl::toProfileCommerceSection)
                    .orElse(null);
            if (commerceSection != null) {
                displayName = commerceSection.getCommercialName();
            }
        }
        return new ProfileSettingsOverview(
                displayName,
                phone,
                user.getEmail(),
                user.getProfileImageId(),
                PLACEHOLDER_PROFILE_IMAGE,
                selectedLang,
                LANGUAGE_CODES,
                commerceSection);
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
