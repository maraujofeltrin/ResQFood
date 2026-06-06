package ar.edu.itba.paw.webapp.config;

import ar.edu.itba.paw.services.user.UserService;
import ar.edu.itba.paw.webapp.auth.AuthUser;
import ar.edu.itba.paw.webapp.auth.AuthUserLocaleSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * Locale resolver that:
 * - Uses the user's preferred locale from {@link ar.edu.itba.paw.webapp.auth.AuthUser} if authenticated
 * - Falls back to Accept-Language header if not authenticated
 * - Defaults to Spanish if neither is available
 */
@Component
public class DatabaseAwareLocaleResolver implements LocaleResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseAwareLocaleResolver.class);

    private final AcceptHeaderLocaleResolver acceptHeaderResolver;
    private final UserService userService;

    @Autowired
    public DatabaseAwareLocaleResolver(final UserService userService) {
        this.userService = userService;
        this.acceptHeaderResolver = new AcceptHeaderLocaleResolver();
        this.acceptHeaderResolver.setDefaultLocale(Locale.forLanguageTag("es"));
    }

    @Override
    public Locale resolveLocale(final HttpServletRequest request) {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        final Locale preferred = AuthUserLocaleSupport.authUserFrom(auth)
                .map(AuthUser::getLocale)
                .orElse(null);
        if (preferred != null) {
            return preferred;
        }
        return acceptHeaderResolver.resolveLocale(request);
    }

    @Override
    public void setLocale(final HttpServletRequest request, final HttpServletResponse response,
            final Locale locale) {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AuthUserLocaleSupport.authUserFrom(auth).ifPresent(authUser -> {
            try {
                userService.updatePreferredLocale(authUser.getId(), locale);
                authUser.setLocale(locale);
            } catch (final RuntimeException e) {
                LOGGER.warn("Could not persist preferred locale for authenticated user", e);
            }
        });
    }
}
