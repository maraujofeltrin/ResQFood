package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.models.Pack;
import ar.edu.itba.paw.services.PackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class HomeController {

    private final PackService packService;

    @Autowired
    public HomeController(final PackService packService) {
        this.packService = packService;
    }

    @GetMapping("/")
    public ModelAndView home() {
        final ModelAndView mav = new ModelAndView("home/index");
        final List<Pack> packs = packService.findActive();
        mav.addObject("packs", packs);
        return mav;
    }
}
