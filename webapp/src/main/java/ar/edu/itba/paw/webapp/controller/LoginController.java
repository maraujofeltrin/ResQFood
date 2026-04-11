package ar.edu.itba.paw.webapp.controller;

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
import ar.edu.itba.paw.webapp.form.UserForm;
import javax.validation.Valid;
import java.util.Objects;

@Controller
public class LoginController {

    private final UserService userService;
    private final UserDetailsService userDetailsService;

    @Autowired
    public LoginController(final UserService userService, final UserDetailsService userDetailsService) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
    }
    
    @GetMapping("/login")
    public String login() {
        return "login/loginView";
    }

    @GetMapping("/register")
    public ModelAndView showForm() {
        final ModelAndView mav = new ModelAndView("login/register");
        mav.addObject("registerForm", new UserForm());
        return mav;
    }

    @PostMapping("/create")
    public ModelAndView create(@Valid @ModelAttribute("registerForm") final UserForm registerForm,
                               final BindingResult bindingResult) {
        if (!Objects.equals(registerForm.getPassword(), registerForm.getRepeatPassword())) {
            bindingResult.rejectValue("repeatPassword", "user.password.mismatch");
        }
        if (bindingResult.hasErrors()) {
            final ModelAndView mav = new ModelAndView("login/register");
            mav.addObject("registerForm", registerForm);
            return mav;
        }

        final User user = userService.createUser(
                registerForm.getEmail(),
                registerForm.getPassword(),
                registerForm.getName());

        // Perform auto-login
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, registerForm.getPassword(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        return new ModelAndView("redirect:/");
    }
}

