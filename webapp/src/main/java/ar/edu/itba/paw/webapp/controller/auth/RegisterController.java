package ar.edu.itba.paw.webapp.controller.auth;

import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.user.RegisterResult;
import ar.edu.itba.paw.services.user.UserService;
import ar.edu.itba.paw.webapp.assembler.RegisterFormAssembler;
import ar.edu.itba.paw.webapp.auth.AuthenticationHelper;
import ar.edu.itba.paw.webapp.form.RegisterForm;
import ar.edu.itba.paw.webapp.validation.RegisterFormValidator;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Locale;

@Controller
@RequestMapping("/register")
public class RegisterController {

    private static final String REGISTER_VIEW = "login/register";

    private final UserService userService;
    private final AuthenticationHelper authenticationHelper;
    private final RegisterFormAssembler assembler;
    private final RegisterFormValidator registerFormValidator;

    @Autowired
    public RegisterController(
            final UserService userService,
            final AuthenticationHelper authenticationHelper,
            final RegisterFormAssembler assembler,
            final RegisterFormValidator registerFormValidator) {
        this.userService = userService;
        this.authenticationHelper = authenticationHelper;
        this.assembler = assembler;
        this.registerFormValidator = registerFormValidator;
    }

    @InitBinder("registerForm")
    public void registerFormBinder(final WebDataBinder binder) {
        binder.addValidators(registerFormValidator);
        binder.setDisallowedFields("commerceProfile.province");
    }

    @ModelAttribute("registerForm")
    public RegisterForm registerForm() {
        final RegisterForm registerForm = new RegisterForm();
        registerForm.getCommerceProfile().setProvince(Commerce.PROVINCE_BUENOS_AIRES);
        return registerForm;
    }

    @GetMapping
    public ModelAndView showForm() {
        final RegisterForm registerForm = new RegisterForm();
        registerForm.getCommerceProfile().setProvince(Commerce.PROVINCE_BUENOS_AIRES);
        return registerView(registerForm);
    }

    @PostMapping
    public ModelAndView create(
            @Valid @ModelAttribute("registerForm") final RegisterForm registerForm,
            final BindingResult bindingResult,
            final HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return registerView(registerForm);
        }
        final Locale locale = RequestContextUtils.getLocale(request);
        final User user = assembler.toUser(registerForm, locale);
        final Client clientProfile = assembler.toClientProfile(registerForm);
        final Commerce commerceProfile = assembler.toCommerceProfile(registerForm);
        final RegisterResult result = userService.tryRegister(user, clientProfile, commerceProfile);
        switch (result.getOutcome()) {
            case DUPLICATE_EMAIL:
                bindingResult.reject("user.email.duplicate");
                return registerView(registerForm);
            case CREATED_PENDING_VERIFICATION:
                return new ModelAndView("redirect:/login?pendingVerification=true");
            default:
                final User created = result.getUser().orElseThrow(IllegalStateException::new);
                authenticationHelper.autoLogin(created.getEmail(), request);
                return new ModelAndView("redirect:/");
        }
    }

    private ModelAndView registerView(final RegisterForm form) {
        final ModelAndView mav = new ModelAndView(REGISTER_VIEW);
        mav.addObject("registerForm", form);
        mav.addObject("availableMunicipalities", Municipality.values());
        return mav;
    }
}
