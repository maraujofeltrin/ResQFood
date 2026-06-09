package ar.edu.itba.paw.webapp.controller.commerce;

import ar.edu.itba.paw.webapp.controller.helpers.CommerceProfileModelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class CommerceProfileController {

    private final CommerceProfileModelBuilder commerceProfileModelBuilder;

    @Autowired
    public CommerceProfileController(final CommerceProfileModelBuilder commerceProfileModelBuilder) {
        this.commerceProfileModelBuilder = commerceProfileModelBuilder;
    }

    @GetMapping("/commerces/{commerceUserId}")
    public ModelAndView commerceProfile(@PathVariable final long commerceUserId,
            @RequestParam(defaultValue = "1") final int page,
            @RequestParam(defaultValue = "1") final int reviewPage) {
        return commerceProfileModelBuilder.buildProfileModel(commerceUserId, page, reviewPage)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
