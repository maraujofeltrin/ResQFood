package ar.edu.itba.paw.webapp.controller.advice;

import ar.edu.itba.paw.services.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class NavProfileModelAdvice {

    private final UserService userService;

    @Autowired
    public NavProfileModelAdvice(final UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("navProfileImageId")
    public Long navProfileImageId(final Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return userService.findByEmail(authentication.getName())
                .map(u -> u.getProfileImageId())
                .orElse(null);
    }
}
