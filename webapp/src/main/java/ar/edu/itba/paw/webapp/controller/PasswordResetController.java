package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.services.PasswordResetTokenService;
import ar.edu.itba.paw.webapp.form.PasswordResetChangeForm;
import ar.edu.itba.paw.webapp.form.PasswordResetRequestForm;
import ar.edu.itba.paw.webapp.util.RequestUrlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/password-reset")
public class PasswordResetController {

    private final PasswordResetTokenService passwordResetTokenService;

    @Autowired
    public PasswordResetController(final PasswordResetTokenService passwordResetTokenService) {
        this.passwordResetTokenService = passwordResetTokenService;
    }

    @GetMapping("/request")
    public String requestForm(final Model model) {
        model.addAttribute("passwordResetRequestForm", new PasswordResetRequestForm());
        return "password-reset/request";
    }

    @PostMapping("/request")
    public String submitRequest(@Valid @ModelAttribute("passwordResetRequestForm") final PasswordResetRequestForm form,
            final BindingResult errors,
            final HttpServletRequest request) {
        if (errors.hasErrors()) {
            return "password-reset/request";
        }

        passwordResetTokenService.requestPasswordReset(form.getEmail(), RequestUrlUtils.buildBaseUrl(request));
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
    public String changePassword(@RequestParam final String token,
            @Valid @ModelAttribute("passwordResetChangeForm") final PasswordResetChangeForm form,
            final BindingResult errors,
            final Model model) {
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

        passwordResetTokenService.resetPassword(token, form.getNewPassword());
        return "redirect:/login?passwordReset=true";
    }

}