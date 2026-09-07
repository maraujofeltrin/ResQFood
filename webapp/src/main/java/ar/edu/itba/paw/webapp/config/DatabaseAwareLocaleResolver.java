package ar.edu.itba.paw.webapp.config;

import ar.edu.itba.paw.webapp.auth.AuthUser;
import ar.edu.itba.paw.webapp.auth.AuthUserLocaleSupport;
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
 * <p>
 * Persisting locale to the database is handled by profile settings ({@code ProfileController});
 * {@link #setLocale} only updates the authenticated session via {@link AuthUserLocaleSupport}.
 */
@Component
public class DatabaseAwareLocaleResolver implements LocaleResolver {

    private final AcceptHeaderLocaleResolver acceptHeaderResolver;

    public DatabaseAwareLocaleResolver() {
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
        AuthUserLocaleSupport.updateSessionLocale(auth, locale);
    }
}
