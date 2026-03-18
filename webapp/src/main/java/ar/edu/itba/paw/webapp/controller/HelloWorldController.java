package ar.edu.itba.paw.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HelloWorldController {

    @RequestMapping("/")
    public ModelAndView helloWorld() {
        final ModelAndView mav = new ModelAndView("index");
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
