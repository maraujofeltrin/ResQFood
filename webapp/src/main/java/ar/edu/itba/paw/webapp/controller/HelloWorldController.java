package ar.edu.itba.paw.webapp.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.models.User;

@Controller
public class HelloWorldController {
    private final UserService userService;

    @Autowired
    public HelloWorldController(final UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ModelAndView createUser(@RequestParam(name = "email") final String email, @RequestParam(name = "password") final String password, @RequestParam(name = "name") final String name) {
        final ModelAndView mav = new ModelAndView("helloworld/index");
        User user = userService.createUser(email, password, name);
        mav.addObject("message", "Usuario creado: " + user.toString());
        return mav;
    }

    @GetMapping("/")
    public ModelAndView helloWorld() {
        final ModelAndView mav = buildBaseModel();
        mav.addObject("searchFieldValue", "");
        mav.addObject("emailFieldValue", "");
        mav.addObject("inputErrors", Map.of());
        return mav;
    }

    private ModelAndView buildBaseModel() {
        final ModelAndView mav = new ModelAndView("helloworld/index");
        mav.addObject("greeting", "pancho");

        mav.addObject("landingTitle", "PAW-2026a-03");

        mav.addObject("cardCategory", "Comida rápida");
        mav.addObject("cardHeading", "McDonald's");
        mav.addObject("cardRating", 3.7);
        mav.addObject("cardImageUrl",
                "https://images.rappi.com.ar/restaurants_background/mcdonaldscol-1660251198623.jpg");

        mav.addObject("cardCategory2", "Restaurante");
        mav.addObject("cardHeading2", "Kansas");
        mav.addObject("cardRating2", 4.5);

        mav.addObject("modalTitle", "Pack sorpresa");
        mav.addObject("modalContent", "Puede incluir: hamburguesa, papas y bebida. Por $5000");
        return mav;
    }
}
