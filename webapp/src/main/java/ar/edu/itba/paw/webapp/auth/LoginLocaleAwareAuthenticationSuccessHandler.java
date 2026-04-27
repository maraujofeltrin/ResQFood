package ar.edu.itba.paw.webapp.auth;

import ar.edu.itba.paw.services.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class LoginLocaleAwareAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UserService userService;
    private final LocaleResolver localeResolver;

    @Autowired
    public LoginLocaleAwareAuthenticationSuccessHandler(final UserService userService,
            final LocaleResolver localeResolver) {
        this.userService = userService;
        this.localeResolver = localeResolver;
        setDefaultTargetUrl("/");
        setAlwaysUseDefaultTargetUrl(false);
    }

    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response,
            final Authentication authentication) throws IOException, ServletException {
        userService.findByEmail(authentication.getName())
                .ifPresent(user -> localeResolver.setLocale(request, response, user.getLocale()));
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
