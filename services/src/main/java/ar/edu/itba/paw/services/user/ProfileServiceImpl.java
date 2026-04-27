package ar.edu.itba.paw.services.user;

import ar.edu.itba.paw.models.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ProfileServiceImpl implements ProfileService {

    private static final String PLACEHOLDER_PROFILE_IMAGE = "profile-avatar-placeholder.svg";
    private static final List<String> LANGUAGE_CODES = List.of("es", "en");

    private final UserService userService;

    @Autowired
    public ProfileServiceImpl(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public ProfileSettingsOverview getSettingsOverview(final long userId) {
        final User user = userService.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        final String phone = user.getPhone() == null ? "" : user.getPhone();
        final String selectedLang = "en".equalsIgnoreCase(user.getLocale().getLanguage()) ? "en" : "es";
        return new ProfileSettingsOverview(
                user.getName(),
                phone,
                user.getEmail(),
                PLACEHOLDER_PROFILE_IMAGE,
                selectedLang,
                LANGUAGE_CODES);
    }
}
