package ar.edu.itba.paw.webapp.controller.user;

import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.user.ProfileService;
import ar.edu.itba.paw.services.user.UserService;
import ar.edu.itba.paw.webapp.controller.utils.AuthenticatedUserResolver;
import ar.edu.itba.paw.webapp.form.ProfilePhotoForm;
import ar.edu.itba.paw.webapp.validation.ProfilePhotoFormValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import java.util.NoSuchElementException;

@Controller
public class ProfileController {

    private static final String NAV_PROFILE = "profile";
    private static final String NAV_SETTINGS = "settings";

    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final ProfileService profileService;
    private final UserService userService;
    private final ProfilePhotoFormValidator profilePhotoFormValidator;
    private final LocaleResolver localeResolver;

    @Autowired
    public ProfileController(
            final AuthenticatedUserResolver authenticatedUserResolver,
            final ProfileService profileService,
            final UserService userService,
            final ProfilePhotoFormValidator profilePhotoFormValidator,
            final LocaleResolver localeResolver) {
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.profileService = profileService;
        this.userService = userService;
        this.profilePhotoFormValidator = profilePhotoFormValidator;
        this.localeResolver = localeResolver;
    }

    @GetMapping("/profile")
    public String profileSection(final Model model) {
        return renderProfile(model, NAV_PROFILE);
    }

    @GetMapping("/profile/settings")
    public String profileSettingsSection(final Model model) {
        return renderProfile(model, NAV_SETTINGS);
    }

    @PostMapping("/profile/photo")
    public String uploadProfilePhoto(
            @ModelAttribute("profilePhotoForm") final ProfilePhotoForm profilePhotoForm,
            final BindingResult bindingResult,
            final Model model,
            final RedirectAttributes redirectAttributes) {
        profilePhotoFormValidator.validate(profilePhotoForm, bindingResult);
        if (bindingResult.hasErrors()) {
            return renderProfileWithPhotoFormErrors(model, profilePhotoForm, bindingResult);
        }
        final byte[] bytes;
        final String contentType;
        try {
            bytes = profilePhotoForm.getPhoto().getBytes();
            contentType = profilePhotoForm.getPhoto().getContentType();
        } catch (final IOException ex) {
            return renderProfilePhotoServiceError(model, profilePhotoForm);
        }
        final User user = authenticatedUserResolver.resolveUser();
        try {
            userService.updateProfilePhoto(user.getId(), bytes, contentType);
        } catch (final IllegalArgumentException | NoSuchElementException ex) {
            return renderProfilePhotoServiceError(model, profilePhotoForm);
        }
        redirectAttributes.addFlashAttribute("profilePhotoUpdateSuccess", true);
        return "redirect:/profile";
    }

    @PostMapping("/profile/settings/locale")
    public String updatePreferredLocale(
            @RequestParam("lang") final String lang,
            final HttpServletRequest request,
            final HttpServletResponse response,
            final RedirectAttributes redirectAttributes) {
        if (!"es".equalsIgnoreCase(lang) && !"en".equalsIgnoreCase(lang)) {
            redirectAttributes.addFlashAttribute("profileLocaleUpdateError", true);
            return "redirect:/profile/settings";
        }
        final User user = authenticatedUserResolver.resolveUser();
        try {
            final Locale resolved = "en".equalsIgnoreCase(lang) ? Locale.ENGLISH : Locale.forLanguageTag("es");
            userService.updatePreferredLocale(user.getId(), resolved);
            localeResolver.setLocale(request, response, resolved);
        } catch (final IllegalArgumentException | NoSuchElementException ex) {
            redirectAttributes.addFlashAttribute("profileLocaleUpdateError", true);
            return "redirect:/profile/settings";
        }
        redirectAttributes.addFlashAttribute("profileLocaleUpdateSuccess", true);
        return "redirect:/profile/settings";
    }

    private String renderProfileWithPhotoFormErrors(
            final Model model,
            final ProfilePhotoForm profilePhotoForm,
            final BindingResult bindingResult) {
        model.addAttribute("org.springframework.validation.BindingResult.profilePhotoForm", bindingResult);
        model.addAttribute("profilePhotoForm", profilePhotoForm);
        return renderProfile(model, NAV_PROFILE);
    }

    private String renderProfilePhotoServiceError(final Model model, final ProfilePhotoForm profilePhotoForm) {
        model.addAttribute("profilePhotoForm", profilePhotoForm);
        model.addAttribute("profilePhotoUpdateError", true);
        return renderProfile(model, NAV_PROFILE);
    }

    private String renderProfile(final Model model, final String profileNavSection) {
        final User user = authenticatedUserResolver.resolveUser();
        model.addAttribute("profile", profileService.getSettingsOverview(user.getId()));
        model.addAttribute("profileNavSection", profileNavSection);
        if (!model.containsAttribute("profilePhotoForm")) {
            model.addAttribute("profilePhotoForm", new ProfilePhotoForm());
        }
        return "profile/profileView";
    }
}
