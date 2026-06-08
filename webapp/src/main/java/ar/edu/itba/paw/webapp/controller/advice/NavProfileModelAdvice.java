package ar.edu.itba.paw.webapp.controller.advice;

import ar.edu.itba.paw.webapp.controller.helpers.AuthenticatedUserResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = {
    "ar.edu.itba.paw.webapp.controller.auth",
    "ar.edu.itba.paw.webapp.controller.commerce",
    "ar.edu.itba.paw.webapp.controller.error",
    "ar.edu.itba.paw.webapp.controller.home",
    "ar.edu.itba.paw.webapp.controller.notification",
    "ar.edu.itba.paw.webapp.controller.pack",
    "ar.edu.itba.paw.webapp.controller.reservation",
    "ar.edu.itba.paw.webapp.controller.user"
})
public class NavProfileModelAdvice {

    private final AuthenticatedUserResolver authResolver;

    @Autowired
    public NavProfileModelAdvice(final AuthenticatedUserResolver authResolver) {
        this.authResolver = authResolver;
    }

    @ModelAttribute("navProfileImageId")
    public Long navProfileImageId() {
        return authResolver.resolveUserOrEmpty()
                .map(u -> u.getProfileImageId())
                .orElse(null);
    }
}
