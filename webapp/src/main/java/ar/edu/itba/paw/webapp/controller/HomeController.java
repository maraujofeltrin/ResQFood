package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.models.Commerce;
import ar.edu.itba.paw.models.Pack;
import ar.edu.itba.paw.services.CommerceService;
import ar.edu.itba.paw.services.PackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @GetMapping("/")
    public ModelAndView home() {
        return new ModelAndView("home/landingView");
    }
}
