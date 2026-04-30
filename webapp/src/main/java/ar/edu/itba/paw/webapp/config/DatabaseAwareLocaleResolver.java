package ar.edu.itba.paw.webapp.config;

import ar.edu.itba.paw.models.user.User;
import ar.edu.itba.paw.services.user.UserService;
import ar.edu.itba.paw.webapp.controller.utils.AuthenticatedUserResolver;
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
 * - Uses the user's preferred locale (from DB) if authenticated
 * - Falls back to Accept-Language header if not authenticated
 * - Defaults to Spanish if neither is available
 */
@Component
public class DatabaseAwareLocaleResolver implements LocaleResolver {

    private final AcceptHeaderLocaleResolver acceptHeaderResolver;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final UserService userService;

    @Autowired
    public DatabaseAwareLocaleResolver(
            final AuthenticatedUserResolver authenticatedUserResolver,
            final UserService userService) {
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.userService = userService;
        this.acceptHeaderResolver = new AcceptHeaderLocaleResolver();
        this.acceptHeaderResolver.setDefaultLocale(Locale.forLanguageTag("es"));
    }

    @Override
    public Locale resolveLocale(final HttpServletRequest request) {
        try {
            final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            final User user = authenticatedUserResolver.resolveUser(auth);
            if (user != null && user.getLocale() != null) {
                return user.getLocale();
            }
        } catch (final Exception e) {
            // User not authenticated or error retrieving user - fall through
        }

        // Fall back to Accept-Language header
        return acceptHeaderResolver.resolveLocale(request);
    }

    @Override
    public void setLocale(final HttpServletRequest request, final HttpServletResponse response,
            final Locale locale) {
        // Update DB if user is authenticated
        try {
            final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            final User user = authenticatedUserResolver.resolveUser(auth);
            if (user != null) {
                userService.updatePreferredLocale(user.getId(), locale);
            }
        } catch (final Exception e) {
            // User not authenticated - do nothing
        }
    }
}
