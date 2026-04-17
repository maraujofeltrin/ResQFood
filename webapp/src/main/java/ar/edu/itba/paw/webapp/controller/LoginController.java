package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.models.Client;
import ar.edu.itba.paw.models.Commerce;
import ar.edu.itba.paw.webapp.form.RegisterForm;
import ar.edu.itba.paw.webapp.form.UserCredentialsForm;
import ar.edu.itba.paw.services.EmailVerificationTokenService;
import ar.edu.itba.paw.webapp.util.RequestUrlUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.services.UserService;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.ConstraintViolation;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Controller
public class LoginController {

    private final UserService userService;
    private final UserDetailsService userDetailsService;
    private final Validator validator;
    private final EmailVerificationTokenService emailVerificationTokenService;

    @Autowired
    public LoginController(final UserService userService, final UserDetailsService userDetailsService,
            final Validator validator, final EmailVerificationTokenService emailVerificationTokenService) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.validator = validator;
        this.emailVerificationTokenService = emailVerificationTokenService;
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
                               final BindingResult bindingResult,
                               final HttpServletRequest request) {
        final String rawRole = registerForm.getRole();
        final UserCredentialsForm credentials = registerForm.getCredentials();

        if (!Objects.equals(credentials.getPassword(), credentials.getRepeatPassword())) {
            bindingResult.rejectValue("credentials.repeatPassword", "user.password.mismatch");
        }

        final Optional<User.Role> parsedRole = parseRole(rawRole);
        if (!parsedRole.isPresent()) {
            bindingResult.rejectValue("role", "register.validation.role.notNull");
        }

        if (parsedRole.isPresent()) {
            validateConditionalProfile(registerForm, parsedRole.get(), bindingResult);
        }

        if (bindingResult.hasErrors()) {
            final ModelAndView mav = new ModelAndView("login/register");
            mav.addObject("registerForm", registerForm);
            return mav;
        }

        final User.Role role = parsedRole.get();
        final Optional<User> existingUser = userService.findByEmail(credentials.getEmail());
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

        final User user;
        if (existingUser.isPresent()) {
            final Optional<User> upgradedUser = userService.upgradeProvisionalUser(userToCreate, clientProfile,
                commerceProfile);

            if (upgradedUser.isPresent()) {
                user = upgradedUser.get();
            } else {
                bindingResult.rejectValue("credentials.email", "user.email.duplicate");
                final ModelAndView mav = new ModelAndView("login/register");
                mav.addObject("registerForm", registerForm);
                return mav;
            }
        } else {
            user = userService.createUser(userToCreate, clientProfile, commerceProfile);
            emailVerificationTokenService.sendVerificationMail(user.getId(), user.getEmail(),
                    RequestUrlUtils.buildBaseUrl(request));
            return new ModelAndView("redirect:/login?pendingVerification=true");
        }

        // Perform auto-login
        final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        final Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, credentials.getPassword(),
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        return new ModelAndView("redirect:/");
    }

    private void validateConditionalProfile(final RegisterForm registerForm, final User.Role role,
            final BindingResult bindingResult) {
        if (role == User.Role.CLIENT) {
            validateProfile(registerForm.getClientProfile(), "clientProfile", bindingResult);
            return;
        }

        if (role == User.Role.COMMERCE) {
            validateProfile(registerForm.getCommerceProfile(), "commerceProfile", bindingResult);
        }
    }

    private <T> void validateProfile(final T profile, final String fieldPrefix, final BindingResult bindingResult) {
        final Set<ConstraintViolation<T>> violations = validator.validate(profile);
        for (final ConstraintViolation<T> violation : violations) {
            final String property = violation.getPropertyPath() == null ? "" : violation.getPropertyPath().toString();
            final String field = property.isEmpty() ? fieldPrefix : fieldPrefix + "." + property;
            bindingResult.rejectValue(field, null, violation.getMessage());
        }
    }

    private static Optional<User.Role> parseRole(final String role) {
        if (role == null || role.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(User.Role.valueOf(role.trim().toUpperCase()));
        } catch (final IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}

