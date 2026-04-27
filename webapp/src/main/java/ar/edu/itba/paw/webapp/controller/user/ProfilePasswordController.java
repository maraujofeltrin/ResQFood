package ar.edu.itba.paw.webapp.controller.user;

import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.user.ChangePasswordResult;
import ar.edu.itba.paw.services.user.UserService;
import ar.edu.itba.paw.webapp.controller.utils.AuthenticatedUserResolver;
import ar.edu.itba.paw.webapp.form.ProfileChangePasswordForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/profile")
public class ProfilePasswordController {

    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final UserService userService;

    @Autowired
    public ProfilePasswordController(
            final AuthenticatedUserResolver authenticatedUserResolver,
            final UserService userService) {
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.userService = userService;
    }

    @GetMapping("/change-password")
    public String changePasswordForm(final Model model) {
        authenticatedUserResolver.resolveUser();
        model.addAttribute("profileChangePasswordForm", new ProfileChangePasswordForm());
        return "profile/changePasswordView";
    }

    @PostMapping("/change-password")
    public String submitChangePassword(
            @Valid @ModelAttribute("profileChangePasswordForm") final ProfileChangePasswordForm form,
            final BindingResult errors,
            final RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            return "profile/changePasswordView";
        }
        final User user = authenticatedUserResolver.resolveUser();
        final ChangePasswordResult result = userService.changePassword(user.getId(), form.getCurrentPassword(),
                form.getNewPassword());
        if (result.getStatus() == ChangePasswordResult.Status.CURRENT_PASSWORD_INCORRECT) {
            errors.rejectValue("currentPassword", "profile.changePassword.error.currentIncorrect",
                    "{profile.changePassword.error.currentIncorrect}");
            return "profile/changePasswordView";
        }
        redirectAttributes.addFlashAttribute("profilePasswordChangeSuccess", true);
        return "redirect:/profile";
    }
}
