package ar.edu.itba.paw.webapp.controller.advice;

import ar.edu.itba.paw.webapp.controller.utils.AuthenticatedUserResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
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
