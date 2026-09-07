package ar.edu.itba.paw.webapp.controller.advice;

import ar.edu.itba.paw.webapp.auth.AuthUser;
import ar.edu.itba.paw.webapp.auth.AuthUserLocaleSupport;
import org.springframework.security.core.Authentication;
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

    @ModelAttribute("navProfileImageId")
    public Long navProfileImageId(final Authentication authentication) {
        return AuthUserLocaleSupport.authUserFrom(authentication)
                .map(AuthUser::getProfileImageId)
                .orElse(null);
    }
}
