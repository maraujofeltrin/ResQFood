package ar.edu.itba.paw.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Optional;

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
    @RequestMapping(value = "/profile/{id}", method = RequestMethod.GET)
    public ModelAndView getUser(@PathVariable(name = "id") final Long id) {
        final ModelAndView mav = new ModelAndView("helloworld/index");
        Optional<User> user = userService.findById(id);
        mav.addObject("message", "Usuario encontrado: " + user.toString());
        return mav;
    }

    @GetMapping("/")
    public ModelAndView home() {
        final ModelAndView mav = new ModelAndView("home/index");
        mav.addObject("pageTitle", "Inicio");
        mav.addObject("viewName", "home");
        mav.addObject("cardTitle", "La Esquina Verde");
        mav.addObject("cardRating", 4.8);
        mav.addObject("cardSubtitle", "Comida Gourmet");

        mav.addObject("cardImageUrl", "https://lh3.googleusercontent.com/aida-public/AB6AXuDb9hqJJAJNKmO3vDzg7EtSwBaD2qDwByCk6_I-bar41vMvOr6ClV2eSjSKxqDojQWHI3eO8zB1BKkl1ntlGvi8EPZkbXBgzSMu9RiO7poHlFUWWEtzs2P9dfXj4foOTOoEKcnfHrLmCVCpUzdxvrhdZY2EOe0lyz4EURrPh7ee3TGa91znbF11iBDn0K7YO13wkdfJVec0vZk1h0jWNtouqj8Agx98aCT_Kuja_RcUDd3H-EaFaamyPYAagjr_yRloaXoZRO7aLVtd");
        mav.addObject("cardImageAlt", "Pollo grillado y vegetales");

        mav.addObject("cardBadgeText", "Opción Vegetariana");

        mav.addObject("cardIsFavorite", Boolean.FALSE);
        mav.addObject("cardFavoriteAriaLabel", "Agregar a favoritos");

        mav.addObject("cardRescueLabel", "RESERVA POR");
        mav.addObject("cardPrice", "$11000");
        mav.addObject("cardOldPrice", "$17000");

        mav.addObject("cardDistanceLabel", "Distancia");
        mav.addObject("cardDistanceText", "1.2 kilometros");
        return mav;
    }

    /*
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
    */


}
