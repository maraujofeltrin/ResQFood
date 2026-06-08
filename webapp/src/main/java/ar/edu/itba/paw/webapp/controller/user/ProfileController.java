package ar.edu.itba.paw.webapp.controller.user;

import ar.edu.itba.paw.models.notification.NotificationType;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.notification.NotificationService;
import ar.edu.itba.paw.services.user.ProfileAccountUpdateException;
import ar.edu.itba.paw.services.user.ProfileCommerceSection;
import ar.edu.itba.paw.services.user.ProfileService;
import ar.edu.itba.paw.services.user.ProfileSettingsOverview;
import ar.edu.itba.paw.services.user.SupportedUserLocales;
import ar.edu.itba.paw.services.user.UserService;
import ar.edu.itba.paw.webapp.auth.AuthUserLocaleSupport;
import ar.edu.itba.paw.webapp.controller.helpers.AuthenticatedUserResolver;
import ar.edu.itba.paw.webapp.form.ProfileAccountForm;
import ar.edu.itba.paw.webapp.validation.ProfileAccountFormValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

@Controller
public class ProfileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileController.class);

    private static final String NAV_PROFILE = "profile";
    private static final String NAV_SETTINGS = "settings";

    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final ProfileService profileService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final ProfileAccountFormValidator profileAccountFormValidator;

    @Autowired
    public ProfileController(
            final AuthenticatedUserResolver authenticatedUserResolver,
            final ProfileService profileService,
            final UserService userService,
            final NotificationService notificationService,
            final ProfileAccountFormValidator profileAccountFormValidator) {
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.profileService = profileService;
        this.userService = userService;
        this.notificationService = notificationService;
        this.profileAccountFormValidator = profileAccountFormValidator;
    }

    @InitBinder("profileAccountForm")
    public void initProfileAccountBinder(final WebDataBinder binder) {
        binder.addValidators(profileAccountFormValidator);
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
            @Valid @ModelAttribute("profileAccountForm") final ProfileAccountForm profileAccountForm,
            final BindingResult bindingResult,
            final Model model,
            final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return renderProfileWithAccountErrors(model, profileAccountForm, bindingResult);
        }
        final User user = authenticatedUserResolver.resolveUser();
        try {
            byte[] photo = null;
            String contentType = null;
            if (profileAccountForm.getPhoto() != null && !profileAccountForm.getPhoto().isEmpty()) {
                photo = profileAccountForm.getPhoto().getBytes();
                contentType = profileAccountForm.getPhoto().getContentType();
            }
            profileService.updateProfileAccount(
                    user.getId(),
                    user.getRole(),
                    profileAccountForm.getCategory(),
                    profileAccountForm.getStreet(),
                    profileAccountForm.getStreetNumber(),
                    profileAccountForm.getCity(),
                    Commerce.PROVINCE_BUENOS_AIRES,
                    profileAccountForm.getPostalCode(),
                    profileAccountForm.getOpeningTime(),
                    profileAccountForm.getClosingTime(),
                    photo,
                    contentType);
            if (photo != null && photo.length > 0) {
                userService.findById(user.getId())
                        .map(User::getProfileImageId)
                        .ifPresent(imageId -> AuthUserLocaleSupport.updateSessionProfileImageId(
                                SecurityContextHolder.getContext().getAuthentication(), imageId));
            }
        } catch (final IOException ex) {
            LOGGER.warn("Profile account photo upload rejected while reading multipart bytes userId={}",
                    Long.valueOf(user.getId()), ex);
            return renderProfilePhotoServiceError(model, profileAccountForm);
        } catch (final ProfileAccountUpdateException ex) {
            LOGGER.debug("Profile account update service error kind={}", ex.getKind(), ex);
            if (ex.getKind() == ProfileAccountUpdateException.Kind.COMMERCE) {
                return renderProfileCommerceServiceError(model, profileAccountForm);
            }
            return renderProfilePhotoServiceError(model, profileAccountForm);
        }
        redirectAttributes.addFlashAttribute("profileAccountUpdateSuccess", true);
        return "redirect:/profile";
    }

    @PostMapping("/profile/settings/locale")
    public String updatePreferredLocale(
            @RequestParam("lang") final String lang,
            final RedirectAttributes redirectAttributes) {
        final User user = authenticatedUserResolver.resolveUser();
        final Locale resolved;
        try {
            resolved = SupportedUserLocales.toLocaleOrThrow(lang);
        } catch (final IllegalArgumentException ex) {
            LOGGER.debug("Unsupported locale preference submitted: {}", lang, ex);
            redirectAttributes.addFlashAttribute("profileLocaleUpdateError", true);
            return "redirect:/profile/settings";
        }
        try {
            userService.updatePreferredLocale(user.getId(), resolved);
        } catch (final IllegalArgumentException | NoSuchElementException ex) {
            LOGGER.debug("Could not persist preferred locale userId={} lang={}", Long.valueOf(user.getId()),
                    resolved, ex);
            redirectAttributes.addFlashAttribute("profileLocaleUpdateError", true);
            return "redirect:/profile/settings";
        }
        redirectAttributes.addFlashAttribute("profileLocaleUpdateSuccess", true);
        return "redirect:/profile/settings";
    }

    @PostMapping("/profile/settings/mail-preferences")
    public String updateMailPreferences(
            @RequestParam final Map<String, String> allParams,
            final RedirectAttributes redirectAttributes) {
        final User user = authenticatedUserResolver.resolveUser();
        final Map<NotificationType, Boolean> parsed = new LinkedHashMap<>();
        for (final NotificationType type : notificationService.getClientConfigurableMailTypes()) {
            parsed.put(type, allParams.containsKey("mailPref_" + type.name()));
        }
        try {
            notificationService.updateClientMailPreferences(user.getId(), parsed);
            redirectAttributes.addFlashAttribute("profileNotificationsUpdateSuccess", true);
        } catch (final RuntimeException ex) {
            LOGGER.debug("Mail preferences update failed userId={}", Long.valueOf(user.getId()), ex);
            redirectAttributes.addFlashAttribute("profileNotificationsUpdateError", true);
        }
        return "redirect:/profile/settings";
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
        model.addAttribute("availableMunicipalities", Municipality.values());
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
