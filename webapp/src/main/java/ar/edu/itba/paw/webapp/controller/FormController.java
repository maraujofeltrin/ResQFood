package ar.edu.itba.paw.webapp.controller;

import java.util.Objects;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.webapp.form.UserForm;

@Controller
public class FormController {

    private final UserService userService;

    @Autowired
    public FormController(final UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public ModelAndView showForm() {
        final ModelAndView mav = new ModelAndView("form/index");
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
            final ModelAndView mav = new ModelAndView("form/index");
            mav.addObject("registerForm", registerForm);
            return mav;
        }
        final User user = userService.createUser(
                registerForm.getUsername(),
                registerForm.getPassword(),
                registerForm.getUsername());
        final ModelAndView mav = new ModelAndView("form/index");
        mav.addObject("registerForm", new UserForm());
        mav.addObject("user", user);
        mav.addObject("successMessage", "Usuario registrado correctamente.");
        return mav;
    }
}
