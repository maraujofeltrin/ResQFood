package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.security.EmailVerificationTokenService;
import ar.edu.itba.paw.services.user.RegisterResult;
import ar.edu.itba.paw.services.user.UserService;
import ar.edu.itba.paw.webapp.form.RegisterForm;
import ar.edu.itba.paw.webapp.form.UserCredentialsForm;
import ar.edu.itba.paw.webapp.validation.RegisterFormValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;

@Controller
@RequestMapping("/register")
public class RegisterController {

    private static final String REGISTER_VIEW = "login/register";

    private final UserService userService;
    private final UserDetailsService userDetailsService;
    private final EmailVerificationTokenService emailVerificationTokenService;
    private final RegisterFormValidator registerFormValidator;

    @Autowired
    public RegisterController(
            final UserService userService,
            final UserDetailsService userDetailsService,
            final EmailVerificationTokenService emailVerificationTokenService,
            final RegisterFormValidator registerFormValidator) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.emailVerificationTokenService = emailVerificationTokenService;
        this.registerFormValidator = registerFormValidator;
    }

    @InitBinder("registerForm")
    public void registerFormBinder(final WebDataBinder binder) {
        binder.addValidators(registerFormValidator);
    }

    @GetMapping
    public ModelAndView showForm() {
        final ModelAndView mav = new ModelAndView(REGISTER_VIEW);
        mav.addObject("registerForm", new RegisterForm());
        return mav;
    }

    @PostMapping
    public ModelAndView create(
            @Valid @ModelAttribute("registerForm") final RegisterForm registerForm,
            final BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return registerView(registerForm);
        }

        final UserCredentialsForm credentials = registerForm.getCredentials();
        final User.Role role = User.Role.valueOf(registerForm.getRole().trim().toUpperCase());

        final User userToCreate = buildUser(credentials, role, registerForm);
        final Client clientProfile = buildClientProfile(role, registerForm);
        final Commerce commerceProfile = buildCommerceProfile(role, registerForm);

        final RegisterResult result = userService.tryRegister(userToCreate, clientProfile, commerceProfile);

        switch (result.getOutcome()) {
            case DUPLICATE_EMAIL:
                bindingResult.rejectValue("credentials.email", "user.email.duplicate");
                return registerView(registerForm);

            case CREATED_PENDING_VERIFICATION:
                sendVerificationMail(result);
                return new ModelAndView("redirect:/login?pendingVerification=true");

            default:
                // CREATED — auto-login the new user so they land directly on the home page
                autoLogin(result, credentials.getPassword());
                return new ModelAndView("redirect:/");
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private ModelAndView registerView(final RegisterForm form) {
        final ModelAndView mav = new ModelAndView(REGISTER_VIEW);
        mav.addObject("registerForm", form);
        return mav;
    }

    private User buildUser(final UserCredentialsForm credentials, final User.Role role,
            final RegisterForm registerForm) {
        final String userName = role == User.Role.CLIENT
                ? registerForm.getClientProfile().getFirstName() + " " + registerForm.getClientProfile().getLastName()
                : registerForm.getCommerceProfile().getCommercialName();

        return new User(
                null,
                credentials.getEmail(),
                credentials.getPassword(),
                userName,
                credentials.getPhone(),
                role,
                false);
    }

    private Client buildClientProfile(final User.Role role, final RegisterForm registerForm) {
        if (role != User.Role.CLIENT) {
            return null;
        }
        return new Client(
                null,
                registerForm.getClientProfile().getFirstName(),
                registerForm.getClientProfile().getLastName(),
                registerForm.getClientProfile().getNotificationsVisibilityPreferences());
    }

    private Commerce buildCommerceProfile(final User.Role role, final RegisterForm registerForm) {
        if (role != User.Role.COMMERCE) {
            return null;
        }
        return new Commerce(
                null,
                registerForm.getCommerceProfile().getCommercialName(),
                registerForm.getCommerceProfile().getCategory(),
                registerForm.getCommerceProfile().getStreet(),
                registerForm.getCommerceProfile().getStreetNumber(),
                registerForm.getCommerceProfile().getCity(),
                registerForm.getCommerceProfile().getProvince(),
                registerForm.getCommerceProfile().getPostalCode(),
                registerForm.getCommerceProfile().getOpeningTime(),
                registerForm.getCommerceProfile().getClosingTime());
    }

    private void sendVerificationMail(final RegisterResult result) {
        final User u = result.getUser().orElseThrow(IllegalStateException::new);
        final String appBaseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();
        emailVerificationTokenService.sendVerificationMail(u.getId(), u.getEmail(), appBaseUrl);
    }

    /**
     * Programmatic login after successful registration (no email verification required).
     * Follows the same pattern as the existing controller flow;
     */
    private void autoLogin(final RegisterResult result, final String rawPassword) {
        final User user = result.getUser().orElseThrow(IllegalStateException::new);
        final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        final Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, rawPassword, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
