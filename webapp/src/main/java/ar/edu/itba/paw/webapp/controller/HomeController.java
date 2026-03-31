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

    private final PackService packService;
    private final CommerceService commerceService;

    @Autowired
    public HomeController(final PackService packService, final CommerceService commerceService) {
        this.packService = packService;
        this.commerceService = commerceService;
    }

    @GetMapping("/")
    public ModelAndView home(@RequestParam(value = "q", required = false) final String query) {
        final ModelAndView mav = new ModelAndView("home/index");
        final List<Pack> packs;
        if (query != null && !query.trim().isEmpty()) {
            packs = packService.searchPacks(query.trim());
        } else {
            packs = packService.findActive();
        }
        
        final Map<Long, String> commerceNames = new HashMap<>();
        for (Pack pack : packs) {
            commerceNames.put(pack.getId(), commerceService.findByUserId(pack.getCommerceId())
                    .map(Commerce::getCommercialName).orElse("—"));
        }
        
        mav.addObject("packs", packs);
        mav.addObject("commerceNames", commerceNames);
        return mav;
    }
}
