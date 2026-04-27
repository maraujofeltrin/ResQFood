package ar.edu.itba.paw.webapp.controller.user;

import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.commerce.CommerceService;
import ar.edu.itba.paw.services.user.ProfileCommerceSection;
import ar.edu.itba.paw.services.user.ProfileService;
import ar.edu.itba.paw.services.user.ProfileSettingsOverview;
import ar.edu.itba.paw.services.user.UserService;
import ar.edu.itba.paw.webapp.controller.utils.AuthenticatedUserResolver;
import ar.edu.itba.paw.webapp.form.ProfileAccountForm;
import ar.edu.itba.paw.webapp.validation.ProfileAccountFormValidator;
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
    private final CommerceService commerceService;
    private final ProfileAccountFormValidator profileAccountFormValidator;
    private final LocaleResolver localeResolver;

    @Autowired
    public ProfileController(
            final AuthenticatedUserResolver authenticatedUserResolver,
            final ProfileService profileService,
            final UserService userService,
            final CommerceService commerceService,
            final ProfileAccountFormValidator profileAccountFormValidator,
            final LocaleResolver localeResolver) {
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.profileService = profileService;
        this.userService = userService;
        this.commerceService = commerceService;
        this.profileAccountFormValidator = profileAccountFormValidator;
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

    @PostMapping("/profile/account")
    public String saveProfileAccount(
            @ModelAttribute("profileAccountForm") final ProfileAccountForm profileAccountForm,
            final BindingResult bindingResult,
            final Model model,
            final RedirectAttributes redirectAttributes) {
        final User user = authenticatedUserResolver.resolveUser();
        profileAccountFormValidator.validate(profileAccountForm, bindingResult, user.getRole());
        if (bindingResult.hasErrors()) {
            return renderProfileWithAccountErrors(model, profileAccountForm, bindingResult);
        }
        if (user.getRole() == User.Role.COMMERCE) {
            final Integer streetNumber = parseStreetNumberOrNull(profileAccountForm.getStreetNumber());
            try {
                commerceService.updateProfileFields(
                        user.getId(),
                        Commerce.Category.valueOf(profileAccountForm.getCategory().trim()),
                        profileAccountForm.getStreet().trim(),
                        streetNumber,
                        profileAccountForm.getCity().trim(),
                        profileAccountForm.getProvince(),
                        profileAccountForm.getPostalCode(),
                        profileAccountForm.getOpeningTime().trim(),
                        profileAccountForm.getClosingTime().trim());
            } catch (final IllegalArgumentException | NoSuchElementException ex) {
                return renderProfileCommerceServiceError(model, profileAccountForm);
            }
        }
        if (profileAccountForm.getPhoto() != null && !profileAccountForm.getPhoto().isEmpty()) {
            try {
                userService.updateProfilePhoto(
                        user.getId(),
                        profileAccountForm.getPhoto().getBytes(),
                        profileAccountForm.getPhoto().getContentType());
            } catch (final IOException ex) {
                return renderProfilePhotoServiceError(model, profileAccountForm);
            } catch (final IllegalArgumentException | NoSuchElementException ex) {
                return renderProfilePhotoServiceError(model, profileAccountForm);
            }
        }
        redirectAttributes.addFlashAttribute("profileAccountUpdateSuccess", true);
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

    private static Integer parseStreetNumberOrNull(final String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return Integer.parseInt(raw.trim());
    }

    private String renderProfileWithAccountErrors(
            final Model model,
            final ProfileAccountForm profileAccountForm,
            final BindingResult bindingResult) {
        model.addAttribute("org.springframework.validation.BindingResult.profileAccountForm", bindingResult);
        model.addAttribute("profileAccountForm", profileAccountForm);
        return renderProfile(model, NAV_PROFILE);
    }

    private String renderProfilePhotoServiceError(final Model model, final ProfileAccountForm profileAccountForm) {
        model.addAttribute("profileAccountForm", profileAccountForm);
        model.addAttribute("profilePhotoUpdateError", true);
        return renderProfile(model, NAV_PROFILE);
    }

    private String renderProfileCommerceServiceError(final Model model, final ProfileAccountForm profileAccountForm) {
        model.addAttribute("profileAccountForm", profileAccountForm);
        model.addAttribute("profileCommerceUpdateError", true);
        return renderProfile(model, NAV_PROFILE);
    }

    private String renderProfile(final Model model, final String profileNavSection) {
        final User user = authenticatedUserResolver.resolveUser();
        final ProfileSettingsOverview overview = profileService.getSettingsOverview(user.getId());
        model.addAttribute("profile", overview);
        model.addAttribute("commerceCategories", Commerce.Category.values());
        model.addAttribute("profileNavSection", profileNavSection);
        if (!model.containsAttribute("profileAccountForm")) {
            final ProfileAccountForm form = new ProfileAccountForm();
            populateAccountFormDefaults(form, overview);
            model.addAttribute("profileAccountForm", form);
        }
        return "profile/profileView";
    }

    private static void populateAccountFormDefaults(final ProfileAccountForm form, final ProfileSettingsOverview overview) {
        final ProfileCommerceSection c = overview.getCommerce();
        if (c == null) {
            return;
        }
        form.setStreet(c.getStreet());
        form.setStreetNumber(c.getStreetNumber() != null ? String.valueOf(c.getStreetNumber()) : "");
        form.setCity(c.getCity());
        form.setProvince(c.getProvince());
        form.setPostalCode(c.getPostalCode());
        form.setCategory(c.getCategory());
        form.setOpeningTime(c.getOpeningTime());
        form.setClosingTime(c.getClosingTime());
    }
}
