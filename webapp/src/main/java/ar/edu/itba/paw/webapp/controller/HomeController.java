package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackSortOption;
import ar.edu.itba.paw.services.pack.PackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private PackService packService;

    @GetMapping("/")
    public ModelAndView home() {
        ModelAndView mav = new ModelAndView("home/landingView");
        List<Pack> previewPacks = packService.filterPacks(null, null, null, null, PackSortOption.DATE_DESC, 1, 6);
        mav.addObject("previewPacks", previewPacks);
        return mav;
    }
}
