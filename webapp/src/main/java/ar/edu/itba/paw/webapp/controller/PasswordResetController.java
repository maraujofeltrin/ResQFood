package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.models.PasswordResetToken;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.services.PasswordResetMailService;
import ar.edu.itba.paw.services.PasswordResetTokenService;
import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.webapp.form.PasswordResetChangeForm;
import ar.edu.itba.paw.webapp.form.PasswordResetRequestForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import java.util.Optional;

@Controller
@RequestMapping("/password-reset")
public class PasswordResetController {

    private final UserService userService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final PasswordResetMailService passwordResetMailService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordResetController(final UserService userService,
            final PasswordResetTokenService passwordResetTokenService,
            final PasswordResetMailService passwordResetMailService,
            final PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordResetTokenService = passwordResetTokenService;
        this.passwordResetMailService = passwordResetMailService;
        this.passwordEncoder = passwordEncoder;
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

        final Optional<User> user = userService.findByEmail(form.getEmail());
        if (user.isPresent()) {
            final PasswordResetToken token = passwordResetTokenService.createForUser(user.get().getId());
            final String resetUrl = buildBaseUrl(request) + "/password-reset/change?token=" + token.getToken();
            passwordResetMailService.sendPasswordResetMail(user.get().getEmail(), resetUrl);
        }
        return "redirect:/password-reset/request?sent=true";
    }

    @GetMapping("/change")
    public String changeForm(@RequestParam final String token, final Model model) {
        final Optional<PasswordResetToken> resetToken = passwordResetTokenService.findByToken(token);
        if (resetToken.isEmpty() || !passwordResetTokenService.isValid(resetToken.get())) {
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
        final Optional<PasswordResetToken> resetToken = passwordResetTokenService.findByToken(token);
        if (resetToken.isEmpty() || !passwordResetTokenService.isValid(resetToken.get())) {
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

        final String encodedPassword = passwordEncoder.encode(form.getNewPassword());
        final User user = userService.findById(resetToken.get().getUserId())
            .orElseThrow(() -> new IllegalStateException("Password reset failed"));
        userService.updatePassword(user.getId(), encodedPassword);
        passwordResetTokenService.markAsUsed(token);
        return "redirect:/login?passwordReset=true";
    }

    private String buildBaseUrl(final HttpServletRequest request) {
        final String scheme = request.getScheme();
        final String serverName = request.getServerName();
        final int port = request.getServerPort();
        final String contextPath = request.getContextPath();
        final String portPart = (port == 80 || port == 443) ? "" : ":" + port;
        return scheme + "://" + serverName + portPart + contextPath;
    }
}