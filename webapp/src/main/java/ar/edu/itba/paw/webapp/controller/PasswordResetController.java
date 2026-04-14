package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.models.PasswordResetToken;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.services.PasswordResetMailService;
import ar.edu.itba.paw.services.PasswordResetTokenService;
import ar.edu.itba.paw.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String requestForm() {
        return "password-reset/request";
    }

    @PostMapping("/request")
    public String submitRequest(@RequestParam final String email, final HttpServletRequest request) {
        final Optional<User> user = userService.findByEmail(email);
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
        model.addAttribute("token", token);
        return "password-reset/change";
    }

    @PostMapping("/change")
    public String changePassword(@RequestParam final String token,
            @RequestParam final String newPassword,
            @RequestParam final String confirmPassword,
            final Model model) {
        final Optional<PasswordResetToken> resetToken = passwordResetTokenService.findByToken(token);
        if (resetToken.isEmpty() || !passwordResetTokenService.isValid(resetToken.get())) {
            return "redirect:/password-reset/request?expired=true";
        }
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("error", "passwords.mismatch");
            return "password-reset/change";
        }
        final String encodedPassword = passwordEncoder.encode(newPassword);
        userService.updatePassword(resetToken.get().getUserId(), encodedPassword);
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