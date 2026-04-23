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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;

@Controller
public class LoginController {

    private final UserService userService;
    private final UserDetailsService userDetailsService;
    private final EmailVerificationTokenService emailVerificationTokenService;
    private final RegisterFormValidator registerFormValidator;

    @Autowired
    public LoginController(final UserService userService, final UserDetailsService userDetailsService,
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

    @GetMapping("/login")
    public String login() {
        return "login/loginView";
    }

    @GetMapping("/register")
    public ModelAndView showForm() {
        final ModelAndView mav = new ModelAndView("login/register");
        mav.addObject("registerForm", new RegisterForm());
        return mav;
    }

    @PostMapping("/register")
    public ModelAndView create(@Valid @ModelAttribute("registerForm") final RegisterForm registerForm,
            final BindingResult bindingResult) {
        final String rawRole = registerForm.getRole();
        final UserCredentialsForm credentials = registerForm.getCredentials();

        if (bindingResult.hasErrors()) {
            final ModelAndView mav = new ModelAndView("login/register");
            mav.addObject("registerForm", registerForm);
            return mav;
        }

        final User.Role role = User.Role.valueOf(rawRole.trim().toUpperCase());
        final String userName = role == User.Role.CLIENT
                ? registerForm.getClientProfile().getFirstName() + " " + registerForm.getClientProfile().getLastName()
                : registerForm.getCommerceProfile().getCommercialName();
        final String phone = credentials.getPhone();

        final User userToCreate = new User(
                null,
                credentials.getEmail(),
                credentials.getPassword(),
                userName,
                phone,
                role,
                false);
        final Client clientProfile = role == User.Role.CLIENT
                ? new Client(
                null,
                registerForm.getClientProfile().getFirstName(),
                registerForm.getClientProfile().getLastName(),
                registerForm.getClientProfile().getNotificationsVisibilityPreferences())
                : null;
        final Commerce commerceProfile = role == User.Role.COMMERCE
                ? new Commerce(
                null,
                registerForm.getCommerceProfile().getCommercialName(),
                registerForm.getCommerceProfile().getCategory(),
                registerForm.getCommerceProfile().getStreet(),
                registerForm.getCommerceProfile().getStreetNumber(),
                registerForm.getCommerceProfile().getCity(),
                registerForm.getCommerceProfile().getProvince(),
                registerForm.getCommerceProfile().getPostalCode(),
                registerForm.getCommerceProfile().getOpeningTime(),
                registerForm.getCommerceProfile().getClosingTime())
                : null;

        final RegisterResult result = userService.tryRegister(userToCreate, clientProfile, commerceProfile);
        if (result.getOutcome() == RegisterResult.Outcome.DUPLICATE_EMAIL) {
            bindingResult.rejectValue("credentials.email", "user.email.duplicate");
            final ModelAndView mav = new ModelAndView("login/register");
            mav.addObject("registerForm", registerForm);
            return mav;
        }
        if (result.getOutcome() == RegisterResult.Outcome.CREATED_PENDING_VERIFICATION) {
            final User u = result.getUser().orElseThrow(IllegalStateException::new);
            final String appBaseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .build()
                    .toUriString();
            emailVerificationTokenService.sendVerificationMail(u.getId(), u.getEmail(), appBaseUrl);
            return new ModelAndView("redirect:/login?pendingVerification=true");
        }

        final User user = result.getUser().orElseThrow(IllegalStateException::new);
        final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        final Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, credentials.getPassword(),
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        return new ModelAndView("redirect:/");
    }
}
