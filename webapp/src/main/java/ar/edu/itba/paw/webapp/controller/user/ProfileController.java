package ar.edu.itba.paw.webapp.controller.user;

import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.user.ProfileService;
import ar.edu.itba.paw.webapp.controller.utils.AuthenticatedUserResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    private static final String NAV_PROFILE = "profile";
    private static final String NAV_SETTINGS = "settings";

    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final ProfileService profileService;

    @Autowired
    public ProfileController(
            final AuthenticatedUserResolver authenticatedUserResolver,
            final ProfileService profileService) {
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.profileService = profileService;
    }

    @GetMapping("/profile")
    public String profileSection(final Model model) {
        return renderProfile(model, NAV_PROFILE);
    }

    @GetMapping("/profile/settings")
    public String profileSettingsSection(final Model model) {
        return renderProfile(model, NAV_SETTINGS);
    }

    private String renderProfile(final Model model, final String profileNavSection) {
        final User user = authenticatedUserResolver.resolveUser();
        model.addAttribute("profile", profileService.getSettingsOverview(user.getId()));
        model.addAttribute("profileNavSection", profileNavSection);
        return "profile/profileView";
    }
}
