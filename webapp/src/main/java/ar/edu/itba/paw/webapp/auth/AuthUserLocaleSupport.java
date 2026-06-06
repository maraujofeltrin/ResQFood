package ar.edu.itba.paw.webapp.auth;

import org.springframework.security.core.Authentication;

import java.util.Locale;
import java.util.Optional;

/**
 * Keeps {@link AuthUser#getLocale()} aligned with persisted preference after profile or interceptor updates.
 */
public final class AuthUserLocaleSupport {

    private AuthUserLocaleSupport() {
    }

    public static Optional<AuthUser> authUserFrom(final Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        final Object principal = authentication.getPrincipal();
        if (principal instanceof AuthUser authUser) {
            return Optional.of(authUser);
        }
        return Optional.empty();
    }

    public static void updateSessionLocale(final Authentication authentication, final Locale locale) {
        authUserFrom(authentication).ifPresent(authUser -> authUser.setLocale(locale));
    }
}
