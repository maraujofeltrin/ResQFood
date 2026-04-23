package ar.edu.itba.paw.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Optional;

import ar.edu.itba.paw.services.user.UserService;
import ar.edu.itba.paw.models.user.User;

@Controller
public class HelloWorldController {
    private final UserService userService;

    @Autowired
    public HelloWorldController(final UserService userService) {
        this.userService = userService;
    }

    // POST / removed: was an unauthenticated dev-only user-creation endpoint.
    @RequestMapping(value = "/profile/{id}", method = RequestMethod.GET)
    public ModelAndView getUser(@PathVariable(name = "id") final Long id) {
        final ModelAndView mav = new ModelAndView("helloworld/index");
        Optional<User> user = userService.findById(id);
        mav.addObject("message", "Usuario encontrado: " + user.toString());
        return mav;
    }

}
