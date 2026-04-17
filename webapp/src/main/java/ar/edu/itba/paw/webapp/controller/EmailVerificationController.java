package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.services.EmailVerificationTokenService;
import ar.edu.itba.paw.webapp.form.EmailVerificationResendForm;
import ar.edu.itba.paw.webapp.util.RequestUrlUtils;
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

    private final EmailVerificationTokenService emailVerificationTokenService;

    @Autowired
    public EmailVerificationController(final EmailVerificationTokenService emailVerificationTokenService) {
        this.emailVerificationTokenService = emailVerificationTokenService;
    }

    @GetMapping
    public String verify(@RequestParam final String token) {
        if (!emailVerificationTokenService.verifyEmail(token)) {
            return "redirect:/verify-email/expired";
        }
        return "redirect:/login?verified=true";
    }

    @GetMapping("/expired")
    public String expired(final Model model) {
        model.addAttribute("resendForm", new EmailVerificationResendForm());
        return "verify-email/expired";
    }

    @PostMapping("/resend")
    public String resend(@Valid @ModelAttribute("resendForm") final EmailVerificationResendForm form,
            final BindingResult errors,
            final HttpServletRequest request) {
        if (errors.hasErrors()) {
            return "verify-email/expired";
        }

        emailVerificationTokenService.resendVerificationMail(form.getEmail(), RequestUrlUtils.buildBaseUrl(request));

        return "redirect:/login?pendingVerification=true";
    }
}
