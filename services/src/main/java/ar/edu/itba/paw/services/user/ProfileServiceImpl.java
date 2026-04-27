package ar.edu.itba.paw.services.user;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileServiceImpl implements ProfileService {

    private static final List<String> LANGUAGE_CODES = List.of("es", "en");

    @Override
    public ProfileSettingsOverview getSettingsOverview(final long userId) {
        return new ProfileSettingsOverview(
                "Elena Rodriguez",
                "+34 612 345 678",
                "elena.r@pantry.org",
                "profile-avatar-placeholder.svg",
                "es",
                LANGUAGE_CODES);
    }
}
