package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.security.EmailVerificationTokenService;
import ar.edu.itba.paw.webapp.form.EmailVerificationResendForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
@RequestMapping("/verify-email")
public class EmailVerificationController {

    private final EmailVerificationTokenService emailVerificationTokenService;
    private final UserDetailsService userDetailsService;

    @Autowired
    public EmailVerificationController(final EmailVerificationTokenService emailVerificationTokenService,
            final UserDetailsService userDetailsService) {
        this.emailVerificationTokenService = emailVerificationTokenService;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping
    public String verify(@RequestParam final String token, final HttpServletRequest request) {
        final User verifiedUser = emailVerificationTokenService.verifyEmailAndGetUser(token)
                .orElse(null);
        if (verifiedUser == null) {
            return "redirect:/verify-email/expired";
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(verifiedUser.getEmail());
        final Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        final SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        request.getSession(true).setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                securityContext);

        return "redirect:/?verified=true";
    }

    @GetMapping("/expired")
    public String expired(final Model model) {
        model.addAttribute("resendForm", new EmailVerificationResendForm());
        return "verify-email/expired";
    }

    @PostMapping("/resend")
    public String resend(@Valid @ModelAttribute("resendForm") final EmailVerificationResendForm form,
            final BindingResult errors) {
        if (errors.hasErrors()) {
            return "verify-email/expired";
        }

        final String appBaseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();
        emailVerificationTokenService.resendVerificationMail(form.getEmail(), appBaseUrl);

        return "redirect:/login?pendingVerification=true";
    }
}
