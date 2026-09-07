package ar.edu.itba.paw.webapp.controller.auth;

import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.security.VerificationTokenService;
import ar.edu.itba.paw.webapp.auth.AuthenticationHelper;
import ar.edu.itba.paw.webapp.form.EmailVerificationResendForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
@RequestMapping("/verify-email")
public class EmailVerificationController {

    private final VerificationTokenService verificationTokenService;
    private final AuthenticationHelper authenticationHelper;

    @Autowired
    public EmailVerificationController(final VerificationTokenService verificationTokenService,
            final AuthenticationHelper authenticationHelper) {
        this.verificationTokenService = verificationTokenService;
        this.authenticationHelper = authenticationHelper;
    }

    @GetMapping
    public String verify(@RequestParam final String token, final HttpServletRequest request) {
        final User verifiedUser = verificationTokenService.verifyEmailAndGetUser(token)
                .orElse(null);
        if (verifiedUser == null) {
            return "redirect:/verify-email/resend?expired=true";
        }

        authenticationHelper.autoLogin(verifiedUser.getEmail(), request);

        return "redirect:/?verified=true";
    }

    @GetMapping("/resend")
    public String resendView(final Model model) {
        model.addAttribute("resendForm", new EmailVerificationResendForm());
        return "verify-email/resend";
    }

    @PostMapping("/resend")
    public String resend(@Valid @ModelAttribute("resendForm") final EmailVerificationResendForm form,
            final BindingResult errors) {
        if (errors.hasErrors()) {
            return "verify-email/resend";
        }

        verificationTokenService.resendVerificationMail(form.getEmail());

        return "redirect:/login?pendingVerification=true";
    }
}
