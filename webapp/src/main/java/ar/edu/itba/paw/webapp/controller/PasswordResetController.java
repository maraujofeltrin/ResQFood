package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.services.security.PasswordResetTokenService;
import ar.edu.itba.paw.webapp.auth.AuthenticationHelper;
import ar.edu.itba.paw.webapp.form.PasswordResetChangeForm;
import ar.edu.itba.paw.webapp.form.PasswordResetRequestForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;

@Controller
@RequestMapping("/password-reset")
public class PasswordResetController {

    private final PasswordResetTokenService passwordResetTokenService;
    private final AuthenticationHelper authenticationHelper;

    @Autowired
    public PasswordResetController(
            final PasswordResetTokenService passwordResetTokenService,
            final AuthenticationHelper authenticationHelper) {
        this.passwordResetTokenService = passwordResetTokenService;
        this.authenticationHelper = authenticationHelper;
    }

    @GetMapping("/request")
    public String requestForm(final Model model) {
        model.addAttribute("passwordResetRequestForm", new PasswordResetRequestForm());
        return "password-reset/request";
    }

    @PostMapping("/request")
    public String submitRequest(
            @Valid @ModelAttribute("passwordResetRequestForm") final PasswordResetRequestForm form,
            final BindingResult errors) {
        if (errors.hasErrors()) {
            return "password-reset/request";
        }

        final String appBaseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();
        passwordResetTokenService.requestPasswordReset(form.getEmail(), appBaseUrl);
        return "redirect:/password-reset/request?sent=true";
    }

    @GetMapping("/change")
    public String changeForm(@RequestParam final String token, final Model model) {
        if (!passwordResetTokenService.isPasswordResetTokenValid(token)) {
            return "redirect:/password-reset/request?expired=true";
        }
        model.addAttribute("passwordResetChangeForm", new PasswordResetChangeForm());
        model.addAttribute("token", token);
        return "password-reset/change";
    }

    @PostMapping("/change")
    public String changePassword(
            @RequestParam final String token,
            @Valid @ModelAttribute("passwordResetChangeForm") final PasswordResetChangeForm form,
            final BindingResult errors,
            final Model model,
            final RedirectAttributes redirectAttributes) {
        if (!passwordResetTokenService.isPasswordResetTokenValid(token)) {
            return "redirect:/password-reset/request?expired=true";
        }
        if (errors.hasErrors()) {
            model.addAttribute("token", token);
            return "password-reset/change";
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            errors.rejectValue("confirmPassword", "passwordReset.validation.passwords.mismatch",
                "{passwordReset.validation.passwords.mismatch}");
            model.addAttribute("token", token);
            return "password-reset/change";
        }
        final String email = passwordResetTokenService.getEmailByToken(token)
                .orElseThrow(IllegalStateException::new);
        passwordResetTokenService.resetPassword(token, form.getNewPassword());
        authenticationHelper.autoLogin(email, form.getNewPassword());
        redirectAttributes.addFlashAttribute("passwordResetSuccess", true);
        return "redirect:/";
    }
}